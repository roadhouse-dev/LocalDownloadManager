package au.com.roadhouse.localdownloadmanager;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class UrlDownloadHttpStackTest {
    @Test
    public void testFileSize() throws Exception {
        DownloadHttpStack downloadStack = new UrlDownloadStack();
        long fileSize = downloadStack.requestFileSize("https://upload.wikimedia.org/wikipedia/commons/0/06/Google-apps-training-logo.png");
        assertTrue(fileSize > 0);
    }

    @Test
    public void testDownloadWithDir() throws Exception {
        DownloadHttpStack downloadStack = new UrlDownloadStack();
        File file = downloadStack.downloadFile(
                InstrumentationRegistry.getTargetContext().getCacheDir(),
                "https://upload.wikimedia.org/wikipedia/commons/0/06/Google-apps-training-logo.png",
                new OnDownloadProgressListener() {
                    @Override
                    public void onFileProgress(File file, String url, long bytesDownloaded, long totalBytes) {

                    }

                    @Override
                    public void onFileComplete(String url, File tempFile) {

                    }

                    @Override
                    public void onFileError(String url) {

                    }
                });


        assertNotNull(file);

        Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
        assertNotNull(bitmap);

        //noinspection ResultOfMethodCallIgnored
        file.delete();
    }

    @Test
    public void testDownloadWithAlreadyCompleteFile() throws Exception {

        DownloadHttpStack downloadStack = new UrlDownloadStack();
        final File file = downloadStack.downloadFile(
                InstrumentationRegistry.getTargetContext().getCacheDir(),
                "https://upload.wikimedia.org/wikipedia/commons/0/06/Google-apps-training-logo.png",
                new OnDownloadProgressListener() {
                    @Override
                    public void onFileProgress(File file, String url, long bytesDownloaded, long totalBytes) {

                    }

                    @Override
                    public void onFileComplete(String url, File tempFile) {

                    }

                    @Override
                    public void onFileError(String url) {

                    }
                });

        File newFile = downloadStack.downloadFile(
                file,
                "https://upload.wikimedia.org/wikipedia/commons/0/06/Google-apps-training-logo.png",
                new OnDownloadProgressListener() {
                    @Override
                    public void onFileProgress(File file, String url, long bytesDownloaded, long totalBytes) {
                        //Should not have gotten any progress
                        assertFalse(true);
                    }

                    @Override
                    public void onFileComplete(String url, File tempFile) {
                        assertEquals(file, tempFile);
                    }

                    @Override
                    public void onFileError(String url) {

                    }
                });

        assertEquals(file, newFile);


        Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
        assertNotNull(bitmap);

        //noinspection ResultOfMethodCallIgnored
        file.delete();

        //noinspection ResultOfMethodCallIgnored
        newFile.delete();
    }

    @Test
    public void testResume() throws Exception {
        //Setup for test

        DownloadHttpStack downloadStack = new UrlDownloadStack();
        final File file = downloadStack.downloadFile(
                InstrumentationRegistry.getTargetContext().getCacheDir(),
                "https://upload.wikimedia.org/wikipedia/commons/0/06/Google-apps-training-logo.png",
                new OnDownloadProgressListener() {
                    @Override
                    public void onFileProgress(File file, String url, long bytesDownloaded, long totalBytes) {

                    }

                    @Override
                    public void onFileComplete(String url, File tempFile) {

                    }

                    @Override
                    public void onFileError(String url) {

                    }
                });

        File testFile = new File(file.getParent(), "mytestfile.tmp");
        //Write half a file then attempt to resume
        long bytesToWrite = file.length()/2;

        FileInputStream fileInputStream = new FileInputStream(file);
        FileOutputStream fileOutputStream = new FileOutputStream(testFile);

        int totalBytesRead = 0;
        int bufferBytesRead = -1;
        byte[] buffer = new byte[1024];

        do{
            bufferBytesRead = fileInputStream.read(buffer, 0, 1024);
            totalBytesRead += bufferBytesRead;
            if(bufferBytesRead != -1) {
                fileOutputStream.write(buffer, 0, bufferBytesRead);
            }
        } while (bufferBytesRead != -1 && totalBytesRead <= bytesToWrite);

        fileInputStream.close();
        fileOutputStream.close();

        //Begin test
        final long testFileSize = testFile.length();

        File newFile = downloadStack.downloadFile(
                testFile,
                "https://upload.wikimedia.org/wikipedia/commons/0/06/Google-apps-training-logo.png",
                new OnDownloadProgressListener() {
                    @Override
                    public void onFileProgress(File file, String url, long bytesDownloaded, long totalBytes) {
                        //Should not have gotten any progress
                        assertTrue(bytesDownloaded > testFileSize);
                    }

                    @Override
                    public void onFileComplete(String url, File tempFile) {
                        assertEquals(file.length(), tempFile.length());
                    }

                    @Override
                    public void onFileError(String url) {

                    }
                });

        assertEquals(testFile, newFile);


        Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
        assertNotNull(bitmap);
    }
}

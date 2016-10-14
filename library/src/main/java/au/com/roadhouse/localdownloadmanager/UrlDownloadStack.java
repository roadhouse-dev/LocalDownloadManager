package au.com.roadhouse.localdownloadmanager;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import timber.log.Timber;

/**
 * An implementation of DownloadHttpStack which uses a UrlConnection to download a file. This
 * stack supports file resuming if paused.
 */
public class UrlDownloadStack implements DownloadHttpStack {

    private static final int BUFFER_SIZE = 8192;
    private boolean mContinueDownload = false;

    /**
     * {@inheritDoc}
     */
    @Override
    public long requestFileSize(String url) {
        long length = -1;
        InputStream inputStream = null;

        try {
            URL fileUrl = new URL(url);
            HttpURLConnection urlConnection = (HttpURLConnection) fileUrl.openConnection();
            urlConnection.setRequestMethod("HEAD");
            inputStream = urlConnection.getInputStream();
            String contentLength = urlConnection.getHeaderField("content-length");

            if(contentLength != null){
                length = Long.valueOf(contentLength);
            }
        } catch (IOException e) {
            Timber.e("requestFileSize: Failed to receive file size", e);
            e.printStackTrace();
        } finally {
            if(inputStream != null) {
                closeInputStream(inputStream);
            }
        }

        return length;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public File downloadFile(File file, String url, OnDownloadProgressListener listener){

        BufferedInputStream bufferedInputStream = null;
        FileOutputStream fileOutputStream = null;

        mContinueDownload = true;

        try {
            long startSize = 0;

            if (file.isDirectory()) {
                file = File.createTempFile("fds", "tmp", file);
            } else if (file.exists()) {
                startSize = file.length();
            }

            URL fileUrl = new URL(url);
            HttpURLConnection urlConnection = (HttpURLConnection) fileUrl.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.setRequestProperty("Range", "bytes="+startSize+"-");
            if(urlConnection.getResponseCode() == 416){
                //File is likely already finished
                if(listener != null){
                    listener.onFileComplete(url, file);
                }
                return file;
            }
            InputStream inputStream = urlConnection.getInputStream();
            String contentLength = urlConnection.getHeaderField("content-length");
            long totalBytes = contentLength != null? Long.valueOf(contentLength) : -1;


            //Resume not supported, delete file and starting again
            if(doesAcceptResume(startSize, urlConnection)) {
                Timber.w("downloadFile: File resume not supported, deleting and redownloading");
                //noinspection ResultOfMethodCallIgnored
                file.delete();
                file = File.createTempFile("fds", "tmp", file.getParentFile());
            }

            byte[] buffer = new byte[BUFFER_SIZE];

            bufferedInputStream = new BufferedInputStream(inputStream);
            fileOutputStream= new FileOutputStream(file, true);

            int bytesRead = -1;
            long totalBytesRead = 0;
            do{
                bytesRead = bufferedInputStream.read(buffer);
                if(bytesRead >= 0){
                    totalBytesRead += bytesRead;
                    fileOutputStream.write(buffer, 0, bytesRead);
                    if(!mContinueDownload){
                        break;
                    }
                    if(listener != null){
                        listener.onFileProgress(file, url, totalBytesRead + startSize, totalBytes + startSize);
                    }
                }
            } while (bytesRead != -1);

        } catch (IOException ioe){
            Timber.e("downloadFile: Error downloading file", ioe);
            if(listener != null){
                listener.onFileError(url);
            }
        } finally {
            closeInputStream(bufferedInputStream);
            closeOutputStream(fileOutputStream);
        }

        if(listener != null && mContinueDownload){
            listener.onFileComplete(url, file);
        }

        return file;
    }

    private boolean doesAcceptResume(long startSize, HttpURLConnection urlConnection) {
        return urlConnection.getHeaderField("Accept-Ranges") != null &&
         urlConnection.getHeaderField("Accept-Ranges").equals("none") && startSize > 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void stopDownload() {
        mContinueDownload = false;
    }

    private void closeInputStream(InputStream inputStream){
        try {
            if(inputStream != null) {
                inputStream.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void closeOutputStream(OutputStream outputStream){
        try {
            if(outputStream != null) {
                outputStream.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

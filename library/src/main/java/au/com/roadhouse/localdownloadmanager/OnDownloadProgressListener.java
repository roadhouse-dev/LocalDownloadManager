package au.com.roadhouse.localdownloadmanager;

import java.io.File;

/**
 * A callback which provides download status updates for a download
 */
public interface OnDownloadProgressListener {

    /**
     * Triggers when download progress has increased.
     * @param file The download file
     * @param url The url being downloaded
     * @param bytesDownloaded The total bytes downloaded
     * @param totalBytes The total file size
     */
    void onFileProgress(File file, String url, long bytesDownloaded, long totalBytes);

    /**
     * Triggers when a file has finished downloading
     * @param url The download url
     * @param tempFile The download file
     */
    void onFileComplete(String url, File tempFile);

    /**
     * Triggers when an error occurred while attempting to download a file
     * @param url The url that was being downloaded
     */
    void onFileError(String url);
}

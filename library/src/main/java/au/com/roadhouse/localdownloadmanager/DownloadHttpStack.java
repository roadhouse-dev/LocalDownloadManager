package au.com.roadhouse.localdownloadmanager;

import java.io.File;

/**
 * Provides a common interface for web stacks used by the DownloadService
 */
public interface DownloadHttpStack {

    /**
     * Retrieves the size of the download file
     * @param url The url pointing to the download file
     * @return The size of the download in bytes
     */
    long requestFileSize(String url);

    /**
     * Begins the file download synchronously
     * @param file The file containing the path to store the file
     * @param url The url to download
     * @param listener A listener which will receive progress and status updates
     * @return The file object which contains the completed file.
     */
    File downloadFile(File file, String url, OnDownloadProgressListener listener);

    /**
     * Cancels the download immediately, but does not remove the stored data.
     */
    void stopDownload();
}

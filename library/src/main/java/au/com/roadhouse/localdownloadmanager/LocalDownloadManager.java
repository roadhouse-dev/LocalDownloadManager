package au.com.roadhouse.localdownloadmanager;

import android.content.Context;
import android.content.Intent;

import java.io.Serializable;
import java.util.UUID;

import au.com.roadhouse.localdownloadmanager.model.DownloadTask;
import au.com.roadhouse.localdownloadmanager.service.DownloadService;
import timber.log.Timber;

import static au.com.roadhouse.localdownloadmanager.service.DownloadService.ACTION_PAUSE_DOWNLOAD;
import static au.com.roadhouse.localdownloadmanager.service.DownloadService.ACTION_QUEUE_DOWNLOAD;
import static au.com.roadhouse.localdownloadmanager.service.DownloadService.ACTION_REMOVE_ALL_DOWNLOAD;
import static au.com.roadhouse.localdownloadmanager.service.DownloadService.ACTION_REMOVE_DOWNLOAD;
import static au.com.roadhouse.localdownloadmanager.service.DownloadService.ACTION_RESUME_DOWNLOAD;
import static au.com.roadhouse.localdownloadmanager.service.DownloadService.ACTION_SETTINGS_NETWORK_TYPE;
import static au.com.roadhouse.localdownloadmanager.service.DownloadService.EXTRA_DOWNLOAD_TAG;
import static au.com.roadhouse.localdownloadmanager.service.DownloadService.EXTRA_DOWNLOAD_TASK;
import static au.com.roadhouse.localdownloadmanager.service.DownloadService.EXTRA_WIFI_ONLY;

/**
 * The main entry for communicating with the DownloadService. Provides methods for adding, removing,
 * pausing the DownloadService. Also provides a way to modify the DownloadManager settings.
 */
public class LocalDownloadManager {

    private final Context mContext;

    /**
     * Creates a new LocalDownloadManager instance
     * @param context A valid context
     */
    public LocalDownloadManager(Context context){
        mContext = context.getApplicationContext();
    }

    /**
     * Adds a new download task to the DownloadService
     * @param downloadTask The download task to add to the queue
     * @return A unique key which can be used to track the download task throughout it's lifecycle
     */
    public String addDownloadToQueue( DownloadTask downloadTask) {
        String tag = UUID.randomUUID().toString();
        downloadTask.setTag(tag);
        Intent intent = new Intent(mContext, DownloadService.class);
        intent.setAction(ACTION_QUEUE_DOWNLOAD);
        intent.putExtra(EXTRA_DOWNLOAD_TASK, (Serializable) downloadTask);
        mContext.startService(intent);

        return tag;
    }

    /**
     * Pauses all current and queued downloads
     */
    public void pauseDownloads() {
        Intent intent = new Intent(mContext, DownloadService.class);
        intent.setAction(ACTION_PAUSE_DOWNLOAD);
        mContext.startService(intent);
        Timber.d("pauseDownloads: pausing downloads");
    }

    /**
     * Resumes downloading.
     */
    public void resumeDownloads() {
        Intent intent = new Intent(mContext, DownloadService.class);
        intent.setAction(ACTION_RESUME_DOWNLOAD);
        mContext.startService(intent);
        Timber.d("resumeDownloads: resuming downloads");
    }

    /**
     * Removes a download based on it's unique tag
     * @param tag The unique tag received when calling {@link #addDownloadToQueue(DownloadTask)}
     */
    public void removeDownload(String tag) {
        Intent intent = new Intent(mContext, DownloadService.class);
        intent.setAction(ACTION_REMOVE_DOWNLOAD);
        intent.putExtra(EXTRA_DOWNLOAD_TAG, tag);
        mContext.startService(intent);
        Timber.d("removeDownload: removing downloads with tag %s", tag);
    }

    /**
     * Removes the current download task in progress, and clears any pending downloads.
     */
    public void removeAllDownloads(){
        Intent intent = new Intent(mContext, DownloadService.class);
        intent.setAction(ACTION_REMOVE_ALL_DOWNLOAD);
        mContext.startService(intent);
        Timber.d("removeAllDownloads: All pending and inprogress downloads have been removed");
    }

    /**
     * Sets the download manager to only download when the device has a WiFi connection
     * @param wifiOnlyDownload True to restrict downloads to WiFi, false otherwise
     */
    public void setWifiOnlyDownload(boolean wifiOnlyDownload) {
        Intent intent = new Intent(mContext, DownloadService.class);
        intent.setAction(ACTION_SETTINGS_NETWORK_TYPE);
        intent.putExtra(EXTRA_WIFI_ONLY, wifiOnlyDownload);
        mContext.startService(intent);
        Timber.d("setWifiOnlyDownload: " + (wifiOnlyDownload ? "Restricting download to wifi" : "Unrestricting download"));
    }
}

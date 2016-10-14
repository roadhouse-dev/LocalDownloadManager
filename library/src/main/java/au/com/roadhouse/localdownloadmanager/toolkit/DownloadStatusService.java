package au.com.roadhouse.localdownloadmanager.toolkit;

import android.app.IntentService;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;

import au.com.roadhouse.localdownloadmanager.model.DownloadItem;
import au.com.roadhouse.localdownloadmanager.model.DownloadTask;
import au.com.roadhouse.localdownloadmanager.service.DownloadService;

/**
 * A base service class which provides an easy way to implement file status logic outside of an active
 * activity/fragment.
 */
public abstract class DownloadStatusService extends IntentService {

    public static final String ACTION_DOWNLOAD_STATUS_UPDATE = "au.com.roadhouse.localdownloadmanager.toolkit.DownloadStatusService.ACTION_DOWNLOAD_STATUS_UPDATE";
    public static final String EXTRA_DOWNLOAD_STATUS = "au.com.roadhouse.localdownloadmanager.toolkit.DownloadStatusService.EXTRA_DOWNLOAD_STATUS";

    public DownloadStatusService(String name) {
        super(name);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if(DownloadService.ACTION_DOWNLOAD_QUEUED.equals(intent.getStringExtra(EXTRA_DOWNLOAD_STATUS))){
            onDownloadTaskQueued((DownloadTask) intent.getSerializableExtra(DownloadService.EXTRA_DOWNLOAD_TASK));
        } else if (DownloadService.ACTION_DOWNLOAD_PROGRESS.equals(intent.getStringExtra(EXTRA_DOWNLOAD_STATUS))){
            onDownloadTaskProgress(
                    (DownloadTask) intent.getSerializableExtra(DownloadService.EXTRA_DOWNLOAD_TASK),
                    intent.getLongExtra(DownloadService.EXTRA_BYTES_DOWNLOADED, 0),
                    intent.getLongExtra(DownloadService.EXTRA_TOTAL_SIZE, 0));
        } else if (DownloadService.ACTION_DOWNLOAD_CANCELLED.equals(intent.getStringExtra(EXTRA_DOWNLOAD_STATUS))){
            onDownloadTaskCancelled((DownloadTask) intent.getSerializableExtra(DownloadService.EXTRA_DOWNLOAD_TASK));
        } else if (DownloadService.ACTION_DOWNLOAD_ERROR.equals(intent.getStringExtra(EXTRA_DOWNLOAD_STATUS))){
            onDownloadTaskError((DownloadTask) intent.getSerializableExtra(DownloadService.EXTRA_DOWNLOAD_TASK));
        } else if (DownloadService.ACTION_DOWNLOAD_ITEM_COMPLETE.equals(intent.getStringExtra(EXTRA_DOWNLOAD_STATUS))){
            onDownloadItemComplete(
                    (DownloadTask) intent.getSerializableExtra(DownloadService.EXTRA_DOWNLOAD_TASK),
                    (DownloadItem) intent.getSerializableExtra(DownloadService.EXTRA_DOWNLOAD_ITEM));
        } else if (DownloadService.ACTION_DOWNLOAD_COMPLETE.equals(intent.getStringExtra(EXTRA_DOWNLOAD_STATUS))){
            onDownloadTaskComplete((DownloadTask) intent.getSerializableExtra(DownloadService.EXTRA_DOWNLOAD_TASK));
        }

        WakefulBroadcastReceiver.completeWakefulIntent(intent);
    }

    /**
     * Triggered when a download task has been queued by the DownloadService
     * @param downloadTask The download task that was queued
     */
    protected abstract void onDownloadTaskQueued(DownloadTask downloadTask);

    /**
     * Triggered when a data has been downloaded for a downloadTask but the download is not yet finished
     * @param downloadTask The download task that is currently in progress
     * @param bytesDownloaded The total data in bytes that has currently been downloaded for this task
     * @param totalBytes The total size in bytes of the download task
     */
    protected abstract void onDownloadTaskProgress(DownloadTask downloadTask, long bytesDownloaded, long totalBytes);

    /**
     * Triggered when a download task has been explicitly cancelled.
     * @param downloadTask The download task that was cancelled.
     */
    protected abstract void onDownloadTaskCancelled(DownloadTask downloadTask);

    /**
     * Triggered when one or more download urls have failed to download. This may be called multiple times
     * one time for each url that failed. DownloadItems which have failed have a status of {@link DownloadItem#STATUS_ERROR}
     * @param downloadTask The download task which contains the failed download items.
     */
    protected abstract void onDownloadTaskError(DownloadTask downloadTask);

    /**
     * Triggers when a download url has completed. The download task status may or may not be completed
     * at this time.
     * @param downloadTask The owner of the download item that was completed
     * @param downloadItem The download item that was completed
     */
    protected abstract void onDownloadItemComplete(DownloadTask downloadTask, DownloadItem downloadItem);


    /**
     * Triggers when a download task has completed. This is the last call in the download task lifecycle,
     * but is not an indication of success. It only indicates that all download items have been attempted and
     * have their final status set. As such it's advisable to check the status of each download item
     * prior to performing any operations on the cache data they're connected too.
     *
     * @param downloadTask The download task that completed
     */
    protected abstract void onDownloadTaskComplete(DownloadTask downloadTask);

}

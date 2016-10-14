package au.com.roadhouse.localdownloadmanager.toolkit;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import au.com.roadhouse.localdownloadmanager.model.DownloadItem;
import au.com.roadhouse.localdownloadmanager.model.DownloadTask;
import au.com.roadhouse.localdownloadmanager.service.DownloadService;

/**
 * A base broadcast receiver which provides an easy way for a user to implement file status update
 * functionality within an activity/fragment. Calls to this broadcast receiver are sent via
 * {@link android.support.v4.content.LocalBroadcastManager} and therefore will not be triggered by a manifest entry.
 * If you require this functionality please extend the {@link }DownloadStatusService}
 */
public abstract class DownloadBroadcastReceiver extends BroadcastReceiver {

    public IntentFilter createBasicIntentFilter() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addCategory(Intent.CATEGORY_DEFAULT);

        intentFilter.addAction(DownloadService.ACTION_DOWNLOAD_QUEUED);
        intentFilter.addAction(DownloadService.ACTION_DOWNLOAD_PROGRESS);
        intentFilter.addAction(DownloadService.ACTION_DOWNLOAD_CANCELLED);
        intentFilter.addAction(DownloadService.ACTION_DOWNLOAD_ERROR);
        intentFilter.addAction(DownloadService.ACTION_DOWNLOAD_ITEM_COMPLETE);
        intentFilter.addAction(DownloadService.ACTION_DOWNLOAD_COMPLETE);

        return intentFilter;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (DownloadService.ACTION_DOWNLOAD_QUEUED.equals(intent.getAction())) {
            onDownloadTaskQueuedReceived((DownloadTask) intent.getSerializableExtra(DownloadService.EXTRA_DOWNLOAD_TASK));
        } else if (DownloadService.ACTION_DOWNLOAD_PROGRESS.equals(intent.getAction())) {
            onDownloadTaskProgressReceived(
                    (DownloadTask) intent.getSerializableExtra(DownloadService.EXTRA_DOWNLOAD_TASK),
                    intent.getLongExtra(DownloadService.EXTRA_BYTES_DOWNLOADED, 0),
                    intent.getLongExtra(DownloadService.EXTRA_TOTAL_SIZE, 0));
        } else if (DownloadService.ACTION_DOWNLOAD_CANCELLED.equals(intent.getAction())) {
            onDownloadTaskCancelledReceived((DownloadTask) intent.getSerializableExtra(DownloadService.EXTRA_DOWNLOAD_TASK));
        } else if (DownloadService.ACTION_DOWNLOAD_ERROR.equals(intent.getAction())) {
            onDownloadTaskErrorReceived((DownloadTask) intent.getSerializableExtra(DownloadService.EXTRA_DOWNLOAD_TASK));
        } else if (DownloadService.ACTION_DOWNLOAD_ITEM_COMPLETE.equals(intent.getAction())) {
            onDownloadItemCompleteReceived(
                    (DownloadTask) intent.getSerializableExtra(DownloadService.EXTRA_DOWNLOAD_TASK),
                    (DownloadItem) intent.getSerializableExtra(DownloadService.EXTRA_DOWNLOAD_ITEM));
        } else if (DownloadService.ACTION_DOWNLOAD_COMPLETE.equals(intent.getAction())) {
            onDownloadTaskCompleteReceived((DownloadTask) intent.getSerializableExtra(DownloadService.EXTRA_DOWNLOAD_TASK));
        }

    }

    /**
     * Triggered when a download task has been queued by the DownloadService
     * @param downloadTask The download task that was queued
     */
    protected abstract void onDownloadTaskQueuedReceived(DownloadTask downloadTask);

    /**
     * Triggered when a data has been downloaded for a downloadTask but the download is not yet finished
     * @param downloadTask The download task that is currently in progress
     * @param bytesDownloaded The total data in bytes that has currently been downloaded for this task
     * @param totalBytes The total size in bytes of the download task
     */
    protected abstract void onDownloadTaskProgressReceived(DownloadTask downloadTask, long bytesDownloaded, long totalBytes);

    /**
     * Triggered when a download task has been explicitly cancelled.
     * @param downloadTask The download task that was cancelled.
     */
    protected abstract void onDownloadTaskCancelledReceived(DownloadTask downloadTask);

    /**
     * Triggered when one or more download urls have failed to download. This may be called multiple times
     * one time for each url that failed. DownloadItems which have failed have a status of {@link DownloadItem#STATUS_ERROR}
     * @param downloadTask The download task which contains the failed download items.
     */
    protected abstract void onDownloadTaskErrorReceived(DownloadTask downloadTask);

    /**
     * Triggers when a download url has completed. The download task status may or may not be completed
     * at this time.
     * @param downloadTask The owner of the download item that was completed
     * @param downloadItem The download item that was completed
     */
    protected abstract void onDownloadItemCompleteReceived(DownloadTask downloadTask, DownloadItem downloadItem);

    /**
     * Triggers when a download task has completed. This is the last call in the download task lifecycle,
     * but is not an indication of success, but rather that all download items have been attempted and
     * have their final status set. As such it's advisable to check the status of each download item
     * prior to performing any operations on the cache data they're connected too.
     *
     * @param downloadTask The download task that completed
     */
    protected abstract void onDownloadTaskCompleteReceived(DownloadTask downloadTask);
}

package au.com.roadhouse.localdownloadmanager.toolkit;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import au.com.roadhouse.localdownloadmanager.model.DownloadItem;
import au.com.roadhouse.localdownloadmanager.model.DownloadTask;
import au.com.roadhouse.localdownloadmanager.service.DownloadService;

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

    protected abstract void onDownloadTaskQueuedReceived(DownloadTask downloadTask);

    protected abstract void onDownloadTaskProgressReceived(DownloadTask downloadTask, long bytesDownloaded, long totalBytes);

    protected abstract void onDownloadTaskCancelledReceived(DownloadTask downloadTask);

    protected abstract void onDownloadTaskErrorReceived(DownloadTask downloadTask);

    protected abstract void onDownloadItemCompleteReceived(DownloadTask downloadTask, DownloadItem downloadItem);

    protected abstract void onDownloadTaskCompleteReceived(DownloadTask downloadTask);
}

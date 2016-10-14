package au.com.roadhouse.localdownloadmanager.toolkit;

import android.app.IntentService;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;

import au.com.roadhouse.localdownloadmanager.model.DownloadItem;
import au.com.roadhouse.localdownloadmanager.model.DownloadTask;
import au.com.roadhouse.localdownloadmanager.service.DownloadService;


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

    protected abstract void onDownloadTaskQueued(DownloadTask downloadTask);

    protected abstract void onDownloadTaskProgress(DownloadTask downloadTask, long bytesDownloaded, long totalBytes);

    protected abstract void onDownloadTaskCancelled(DownloadTask downloadTask);

    protected abstract void onDownloadTaskError(DownloadTask downloadTask);

    protected abstract void onDownloadItemComplete(DownloadTask downloadTask, DownloadItem downloadItem);

    protected abstract void onDownloadTaskComplete(DownloadTask downloadTask);

}

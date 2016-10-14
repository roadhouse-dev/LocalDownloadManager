package au.com.roadhouse.localdownloadmanager.service;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;

import au.com.roadhouse.localdownloadmanager.toolkit.DownloadStatusService;

public class DownloadStateBroadcastReceiver extends WakefulBroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Intent serviceIntent = new Intent(DownloadStatusService.ACTION_DOWNLOAD_STATUS_UPDATE);
        serviceIntent.setPackage(context.getPackageName());
        serviceIntent.putExtras(intent.getExtras());
        serviceIntent.putExtra(DownloadStatusService.EXTRA_DOWNLOAD_STATUS, intent.getAction());
        startWakefulService(context, serviceIntent);
    }
}


package au.com.roadhouse.localdownloadmanager.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import au.com.roadhouse.localdownloadmanager.LocalDownloadManager;

/**
 * A broadcast receiver that receives network changes and restarts the download service. This
 * is used on API 6.0 and below. As of API 7.0 the job scheduler is used.
 */
public class NetworkStateChangeReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        ConnectivityManager cm =
                (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();

        if(isConnected) {
            //Restart the service which will determine if it can run
            LocalDownloadManager localDownloadManager = new LocalDownloadManager(context);
            localDownloadManager.resumeDownloads();
        }
    }
}

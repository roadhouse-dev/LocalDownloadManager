package au.com.roadhouse.example.localdownloadmanager;

import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import au.com.roadhouse.localdownloadmanager.LocalDownloadManager;
import au.com.roadhouse.localdownloadmanager.model.DownloadItem;
import au.com.roadhouse.localdownloadmanager.model.DownloadTask;
import au.com.roadhouse.localdownloadmanager.toolkit.DownloadBroadcastReceiver;
import timber.log.Timber;

public class MainActivity extends AppCompatActivity {

    private LocalDownloadManager mLocalDownloadManager;
    private DownloadBroadcastReceiver mDownloadBroadcastReceiver;
    private TextView mLogView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Timber.plant(new Timber.DebugTree());
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mLocalDownloadManager = new LocalDownloadManager(this);
        mLocalDownloadManager.setWifiOnlyDownload(false);
        mDownloadBroadcastReceiver = new MyDownloadBroadcastReceiver();
        mLogView = (TextView)findViewById(R.id.textViewLog) ;

        findViewById(R.id.buttonDownload).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onQueueDownload();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        LocalBroadcastManager.getInstance(this).registerReceiver(mDownloadBroadcastReceiver, mDownloadBroadcastReceiver.createBasicIntentFilter());
    }

    private void onQueueDownload() {
        mLogView.setText("");
        String url = ((EditText)findViewById(R.id.editTextUrl)).getText().toString();
        DownloadTask downloadTask =
                new DownloadTask.Builder(url)
                .addDownloadUrl(url)
                .setNotificationIcon(R.mipmap.ic_launcher)
                .build();

        mLocalDownloadManager.addDownloadToQueue(downloadTask);

    }

    @Override
    protected void onStop() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mDownloadBroadcastReceiver);
        super.onStop();
    }

    private class MyDownloadBroadcastReceiver extends DownloadBroadcastReceiver {
        @Override
        protected void onDownloadTaskQueuedReceived(DownloadTask downloadTask) {
            mLogView.append("\n\n"+ "Queued " +downloadTask.getTag() +"\n" + downloadTask.getLabel());
        }

        @Override
        protected void onDownloadTaskProgressReceived(DownloadTask downloadTask, long bytesDownloaded, long totalBytes) {
            mLogView.append("\n\n"+ "Progress " + downloadTask.getLabel() + " " + bytesDownloaded +"/" + totalBytes);
        }

        @Override
        protected void onDownloadTaskCancelledReceived(DownloadTask downloadTask) {
            mLogView.append("\n\n"+ "Cancelled " + downloadTask.getLabel());
        }

        @Override
        protected void onDownloadTaskErrorReceived(DownloadTask downloadTask) {
            mLogView.append("\n\n"+ "Error " + downloadTask.getLabel());
        }

        @Override
        protected void onDownloadItemCompleteReceived(DownloadTask downloadTask, DownloadItem downloadItem) {
            mLogView.append("\n\n"+ "Download item complete " + downloadItem.getUrl());
        }

        @Override
        protected void onDownloadTaskCompleteReceived(DownloadTask downloadTask) {
            mLogView.append("\n\n"+ "Download complete - " +downloadTask.getTag() +"\n" + downloadTask.getLabel());
        }
    }
}

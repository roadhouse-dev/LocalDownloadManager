package au.com.roadhouse.example.localdownloadmanager;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;

import au.com.roadhouse.localdownloadmanager.LocalDownloadManager;
import au.com.roadhouse.localdownloadmanager.model.DownloadTask;

public class MainActivity extends AppCompatActivity {

    private LocalDownloadManager mLocalDownloadManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mLocalDownloadManager = new LocalDownloadManager(this);

        findViewById(R.id.buttonDownload).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onQueueDownload();
            }
        });
    }

    private void onQueueDownload() {
        String url = ((EditText)findViewById(R.id.editTextUrl)).getText().toString();
        DownloadTask downloadTask =
                new DownloadTask.Builder(url)
                .addDownloadUrl(url)
                .setNotificationIcon(R.mipmap.ic_launcher)
                .build();

        mLocalDownloadManager.addDownloadToQueue(downloadTask);

    }

}

package au.com.roadhouse.localdownloadmanager.service;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.os.Build;
import android.support.annotation.RequiresApi;

import au.com.roadhouse.localdownloadmanager.LocalDownloadManager;

/**
 * A job scheduler service which restarts the download manager once right network conditions
 * are discovered
 */
@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class DownloadRestartService extends JobService {

    @Override
    public boolean onStartJob(JobParameters params) {
        LocalDownloadManager localDownloadManager = new LocalDownloadManager(this);
        localDownloadManager.resumeDownloads();
        return false;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        return false;
    }
}

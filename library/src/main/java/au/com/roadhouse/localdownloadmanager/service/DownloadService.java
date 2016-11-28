package au.com.roadhouse.localdownloadmanager.service;

import android.app.NotificationManager;
import android.app.Service;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.List;

import au.com.roadhouse.localdownloadmanager.DownloadHttpStack;
import au.com.roadhouse.localdownloadmanager.OnDownloadProgressListener;
import au.com.roadhouse.localdownloadmanager.PersistentQueue;
import au.com.roadhouse.localdownloadmanager.R;
import au.com.roadhouse.localdownloadmanager.UrlDownloadStack;
import au.com.roadhouse.localdownloadmanager.model.DownloadItem;
import au.com.roadhouse.localdownloadmanager.model.DownloadTask;
import au.com.roadhouse.localdownloadmanager.model.NetworkHelper;
import timber.log.Timber;

/**
 * The main service for the LocalDownloadManager, manages downloads, notifications, and download state
 * broadcasts.
 */
public class DownloadService extends Service implements NetworkHelper.OnNetworkStateChangeListener {

    private static final String TAG = "DownloadService";
    private static final int NOTIFICATION_ID = 101;

    //Command Actions
    public static final String ACTION_QUEUE_DOWNLOAD = "au.com.roadhouse.filedownloaderservice.DownloadService.ACTION_QUEUE_DOWNLOAD";
    public static final String ACTION_PAUSE_DOWNLOAD = "au.com.roadhouse.filedownloaderservice.DownloadService.ACTION_PAUSE_DOWNLOAD";
    public static final String ACTION_RESUME_DOWNLOAD = "au.com.roadhouse.filedownloaderservice.DownloadService.ACTION_RESUME_DOWNLOAD";
    public static final String ACTION_REMOVE_DOWNLOAD = "au.com.roadhouse.filedownloaderservice.DownloadService.ACTION_REMOVE_DOWNLOAD";
    public static final String ACTION_REMOVE_ALL_DOWNLOAD = "au.com.roadhouse.filedownloaderservice.DownloadService.ACTION_REMOVE_ALL_DOWNLOAD";
    public static final String ACTION_SETTINGS_NETWORK_TYPE = "au.com.roadhouse.filedownloaderservice.DownloadService.ACTION_SETTINGS_NETWORK_TYPE";

    //Broadcast Actions
    public static final String ACTION_DOWNLOAD_QUEUED = "au.com.roadhouse.filedownloaderservice.DownloadService.ACTION_DOWNLOAD_QUEUED";
    public static final String ACTION_DOWNLOAD_PROGRESS = "au.com.roadhouse.filedownloaderservice.DownloadService.ACTION_DOWNLOAD_PROGRESS";
    public static final String ACTION_DOWNLOAD_COMPLETE = "au.com.roadhouse.filedownloaderservice.DownloadService.ACTION_DOWNLOAD_COMPLETE";
    public static final String ACTION_DOWNLOAD_ITEM_COMPLETE = "au.com.roadhouse.filedownloaderservice.DownloadService.ACTION_DOWNLOAD_ITEM_COMPLETE";
    public static final String ACTION_DOWNLOAD_CANCELLED = "au.com.roadhouse.filedownloaderservice.DownloadService.ACTION_DOWNLOAD_CANCELLED";
    public static final String ACTION_DOWNLOAD_ERROR = "au.com.roadhouse.filedownloaderservice.DownloadService.ACTION_DOWNLOAD_ERROR";

    //Extras
    public static final String EXTRA_DOWNLOAD_TASK = "au.com.roadhouse.filedownloaderservice.DownloadService.EXTRA_DOWNLOAD_TASK";
    public static final String EXTRA_DOWNLOAD_TAG = "au.com.roadhouse.filedownloaderservice.DownloadService.EXTRA_DOWNLOAD_TAG";
    public static final String EXTRA_BYTES_DOWNLOADED = "au.com.roadhouse.filedownloaderservice.DownloadService.EXTRA_BYTES_DOWNLOADED";
    public static final String EXTRA_TOTAL_SIZE = "au.com.roadhouse.filedownloaderservice.DownloadService.EXTRA_TOTAL_SIZE";
    public static final String EXTRA_WIFI_ONLY = "au.com.roadhouse.localdownloadmanager.DownloadService.EXTRA_WIFI_ONLY";
    public static final String EXTRA_DOWNLOAD_ITEM = "au.com.roadhouse.filedownloaderservice.DownloadService.EXTRA_DOWNLOAD_ITEM";

    private ServiceHandler mServiceHandler;
    private DownloadWorker mDownloadWorker;
    private PersistentQueue<DownloadTask> mDownloadQueue;
    private DownloadHttpStack mDownloadStack;
    private NetworkHelper mNetworkHelper;
    private int mTotalDownloadsQueued;
    private NotificationManager mNotifyManager;
    private NotificationCompat.Builder mNotificationBuilder;
    private SharedPreferences mDownloadPreferences;

    @Override
    public void onCreate() {
        super.onCreate();
        mDownloadPreferences = getSharedPreferences("download_settings", MODE_PRIVATE);
        HandlerThread thread = new HandlerThread("Service[" + TAG + "]");
        thread.start();
        mDownloadQueue = new PersistentQueue<>(new File(ContextCompat.getDataDir(this), "download.que"));
        mTotalDownloadsQueued = mDownloadQueue.size();
        Looper serviceLooper = thread.getLooper();
        mServiceHandler = new ServiceHandler(serviceLooper);
        mDownloadStack = new UrlDownloadStack();
        mNetworkHelper = new NetworkHelper(this);
        mNetworkHelper.registerForNetworkChangeEvents(this);
        mNotifyManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationBuilder = new NotificationCompat.Builder(this);
        mNotificationBuilder.setContentTitle("Picture Download")
                .setContentText("Download in progress");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Message msg = mServiceHandler.obtainMessage();
        msg.arg1 = startId;
        msg.obj = intent;
        mServiceHandler.sendMessage(msg);

        return START_STICKY;
    }

    private void onHandleIntent(Intent intent) {
        if (intent == null) {
            onResumeDownloads();
            return;
        }

        if (ACTION_QUEUE_DOWNLOAD.equals(intent.getAction())) {
            onQueueDownload(intent);
        } else if (ACTION_PAUSE_DOWNLOAD.equals(intent.getAction())) {
            onPauseDownloads();
        } else if (ACTION_RESUME_DOWNLOAD.equals(intent.getAction())) {
            onResumeDownloads();
        } else if (ACTION_REMOVE_DOWNLOAD.equals(intent.getAction())) {
            onRemoveDownload(intent);
        } else if (ACTION_REMOVE_ALL_DOWNLOAD.equals(intent.getAction())){
            onRemoveAllDownloads();
        } else if (ACTION_SETTINGS_NETWORK_TYPE.equals(intent.getAction())) {
            onUpdateSetting(intent);
        }
    }


    private void onUpdateSetting(Intent intent) {
        mDownloadPreferences.edit()
                .putBoolean(EXTRA_WIFI_ONLY, intent.getBooleanExtra(EXTRA_WIFI_ONLY, true))
                .apply();
        if (isRequestedNetworkConnectionAvailable()) {
            mDownloadWorker = new DownloadWorker();
            mDownloadWorker.start();
        } else {
            stopSelf();
        }
    }

    private void onResumeDownloads() {
        if (isRequestedNetworkConnectionAvailable()) {
            if (mDownloadWorker == null || !mDownloadWorker.isRunning()) {
                mDownloadWorker = new DownloadWorker();
                mDownloadWorker.start();
            }
        }
    }

    private boolean isDownloadRestrictedToWifi() {
        return mDownloadPreferences.getBoolean(EXTRA_WIFI_ONLY, true);
    }

    private boolean isRequestedNetworkConnectionAvailable() {
        return (!isDownloadRestrictedToWifi() || mNetworkHelper.getCurrentConnection() == NetworkHelper.TYPE_WIFI) &&
                mNetworkHelper.getCurrentConnection() != NetworkHelper.TYPE_NO_CONNECTION;
    }

    private void onQueueDownload(Intent intent) {
        DownloadTask downloadTask = (DownloadTask) intent.getSerializableExtra(EXTRA_DOWNLOAD_TASK);
        downloadTask.setStatus(DownloadTask.PENDING);
        initDownloadItems(downloadTask);
        mTotalDownloadsQueued++;
        addToQueue(downloadTask);
    }

    private void onPauseDownloads() {
        if (mDownloadWorker != null) {
            mDownloadWorker.stopWork();
        }
    }

    private void onRemoveDownload(Intent intent) {
        String tag = intent.getStringExtra(EXTRA_DOWNLOAD_TAG);
        DownloadTask downloadTask = new DownloadTask(tag);

        if (!mDownloadQueue.remove(downloadTask)) {
            if (mDownloadWorker != null && downloadTask.equals(mDownloadWorker.getCurrentDownloadTask())) {
                mDownloadWorker.cancelCurrentDownload();
                mTotalDownloadsQueued--;
            }
        } else {
            mTotalDownloadsQueued--;
        }
    }

    private void onRemoveAllDownloads() {
        mDownloadQueue.clear();
        if (mDownloadWorker != null) {
            mDownloadWorker.cancelCurrentDownload();
            mTotalDownloadsQueued = 0;
        }
    }

    private void addToQueue(DownloadTask downloadTask) {
        downloadTask.setStatus(DownloadTask.PENDING);
        mDownloadQueue.add(downloadTask);
        broadcastAddedToQueue(downloadTask);
        if (isRequestedNetworkConnectionAvailable()) {
            if (mDownloadWorker == null || !mDownloadWorker.isRunning()) {
                mDownloadWorker = new DownloadWorker();
                mDownloadWorker.start();
            }
        }
    }

    private void broadcastAddedToQueue(DownloadTask downloadTask) {
        Intent intent = new Intent(ACTION_DOWNLOAD_QUEUED);
        intent.setPackage(getPackageName());
        intent.putExtra("FROM", "DownloadService");
        intent.putExtra(EXTRA_DOWNLOAD_TASK, (Serializable) downloadTask);

        sendBroadcast(intent);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    private void broadcastTaskProgress(DownloadTask downloadTask, long bytesDownloaded, long totalBytes) {
        Intent intent = new Intent(ACTION_DOWNLOAD_PROGRESS);
        intent.putExtra(EXTRA_DOWNLOAD_TASK, (Serializable) downloadTask);
        intent.setPackage(getPackageName());
        intent.putExtra(EXTRA_BYTES_DOWNLOADED, bytesDownloaded);
        intent.putExtra(EXTRA_TOTAL_SIZE, totalBytes);

        sendBroadcast(intent);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    private void broadcastTaskItemComplete(DownloadTask downloadTask, DownloadItem downloadItem) {
        Timber.d("broadcastTaskItemComplete: Broadcasting downloadtask complete");
        Intent intent = new Intent(ACTION_DOWNLOAD_ITEM_COMPLETE);
        intent.putExtra(EXTRA_DOWNLOAD_TASK, (Serializable) downloadTask);
        intent.putExtra(EXTRA_DOWNLOAD_ITEM, (Serializable) downloadItem);

        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);

        intent.setPackage(getPackageName());
        sendBroadcast(intent);
    }

    private void broadcastTaskCancelled(DownloadTask downloadTask) {
        Timber.d("broadcastTaskItemComplete: Broadcasting downloadtask complete");
        Intent intent = new Intent(ACTION_DOWNLOAD_CANCELLED);
        sendBroadcast(intent);
        intent.putExtra(EXTRA_DOWNLOAD_TASK, (Serializable) downloadTask);

        intent.setPackage(getPackageName());
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    private void broadcastTaskError(DownloadTask downloadTask) {
        Timber.d("broadcastTaskError: Broadcasting download error");
        Intent intent = new Intent(ACTION_DOWNLOAD_ERROR);
        intent.setPackage(getPackageName());
        intent.putExtra(EXTRA_DOWNLOAD_TASK, (Serializable) downloadTask);

        sendBroadcast(intent);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    private void broadcastTaskComplete(DownloadTask downloadTask) {
        Timber.d("broadcastTaskItemComplete: Broadcasting downloadtask complete");
        Intent intent = new Intent(ACTION_DOWNLOAD_COMPLETE);
        intent.setPackage(getPackageName());
        intent.putExtra(EXTRA_DOWNLOAD_TASK, (Serializable) downloadTask);

        sendBroadcast(intent);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    private void initDownloadItems(DownloadTask downloadTask) {
        List<DownloadItem> downloadItemList = downloadTask.getDownloadItems();
        for (int i = 0; i < downloadItemList.size(); i++) {
            DownloadItem downloadItem = downloadItemList.get(i);
            downloadItem.setStatus(DownloadItem.STATUS_WAITING);
            if (downloadItem.getFile() == null) {
                downloadItem.setFile(createDownloadFile(getCacheDir()));
            }
        }
    }

    private File createDownloadFile(File directory) {
        try {
            return File.createTempFile("fds", "tmp", directory);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return directory;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void checkIfWorkComplete() {
        if (mDownloadQueue.size() == 0 && mServiceHandler.getMessageCount() == 0) {
            stopSelf();
        }
    }

    @Override
    public void onNetworkConnectionChange(int connectionType) {
        if ((connectionType != NetworkHelper.TYPE_WIFI && isDownloadRestrictedToWifi()) || connectionType == NetworkHelper.TYPE_NO_CONNECTION) {
            stopAndSetupResumeTask();
        }
    }

    private void stopAndSetupResumeTask() {
        //Devices running less than N will use the NetworkStateChangeReceiver
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            JobScheduler JobScheduler = (JobScheduler) getSystemService(Context.JOB_SCHEDULER_SERVICE);
            JobScheduler.schedule(
                    new JobInfo.Builder(1, new ComponentName(getPackageName(), DownloadRestartService.class.getName()))
                            .setRequiredNetworkType(JobInfo.NETWORK_TYPE_UNMETERED)
                            .setPersisted(true)
                            .build());
        }

        stopSelf();
    }

    @Override
    public void onDestroy() {
        Timber.d( "onDestroy: Stopping service");
        if (mDownloadWorker != null) {
            mDownloadWorker.stopWork();
        }
        mNetworkHelper.unregisterForNetworkChangeEvents();
        mNotifyManager.cancel(NOTIFICATION_ID);
        mDownloadQueue.flushUpdates();
        super.onDestroy();
    }

    private final class ServiceHandler extends Handler {
        private int messageCount = 0;

        ServiceHandler(Looper looper) {
            super(looper);
        }

        @Override
        public boolean sendMessageAtTime(Message msg, long uptimeMillis) {
            messageCount++;
            return super.sendMessageAtTime(msg, uptimeMillis);
        }

        @Override
        public void dispatchMessage(Message msg) {
            messageCount--;
            super.dispatchMessage(msg);
            checkIfWorkComplete();
        }

        @Override
        public void handleMessage(Message msg) {
            onHandleIntent((Intent) msg.obj);
        }

        int getMessageCount() {
            return messageCount;
        }
    }

    //Core worker thread
    private class DownloadWorker extends Thread implements OnDownloadProgressListener {
        private boolean mIsRunning = false;
        private boolean mIsCurrentDownloadCancelled = false;
        private DownloadTask mCurrentDownloadTask;
        private DownloadItem mCurrentDownloadItem;
        private long mLastNotificationTime;

        @Override
        public synchronized void start() {
            mIsRunning = true;
            super.start();
        }

        synchronized void stopWork() {
            mIsRunning = false;
        }

        boolean isRunning() {
            return mIsRunning;
        }

        DownloadTask getCurrentDownloadTask() {
            return mCurrentDownloadTask;
        }

        void cancelCurrentDownload() {
            mIsCurrentDownloadCancelled = true;
        }

        @Override
        public void run() {
            while (mIsRunning) {
                //We don't remove the current task from the queue as we want it to be persisted in case
                //the service is killed. It will be removed after it is complete
                mCurrentDownloadTask = mDownloadQueue.peek();
                mIsCurrentDownloadCancelled = false;

                if (mCurrentDownloadTask == null || mCurrentDownloadTask.getStatus() == DownloadTask.FINISHED) {
                    mIsRunning = false;
                    continue;
                }

                mNotificationBuilder.setContentTitle(mCurrentDownloadTask.getLabel());
                mNotificationBuilder.setSmallIcon(mCurrentDownloadTask.getNotificationIcon());
                if (mCurrentDownloadTask.getNotificationPendingIntent() != null) {
                    mNotificationBuilder.setContentIntent(mCurrentDownloadTask.getNotificationPendingIntent().buildPendingIntent(getApplicationContext()));
                }
                mNotificationBuilder.setContentText(getString(
                        R.string.format_download_queue_progress,
                        (mTotalDownloadsQueued - mDownloadQueue.size() + 1),
                        mTotalDownloadsQueued));
                mNotificationBuilder.setProgress(100, 0, false);
                mNotifyManager.notify(NOTIFICATION_ID, mNotificationBuilder.build());

                getTaskDownloadSize(mCurrentDownloadTask);
                mCurrentDownloadTask.setStatus(DownloadTask.DOWNLOADING);
                List<DownloadItem> downloadItems = mCurrentDownloadTask.getDownloadItems();

                for (int i = 0; i < downloadItems.size(); i++) {
                    mLastNotificationTime = System.currentTimeMillis();
                    if (mCurrentDownloadTask == null || !isRequestedNetworkConnectionAvailable()) {
                        break;
                    }

                    mCurrentDownloadItem = downloadItems.get(i);
                    mDownloadStack.downloadFile(mCurrentDownloadItem.getFile(), mCurrentDownloadItem.getUrl(), this);
                }

                if ((mCurrentDownloadTask != null && isRequestedNetworkConnectionAvailable()) || (mCurrentDownloadTask.getStatus() == DownloadTask.DOWNLOADING)) {
                    mDownloadQueue.poll();
                }

                if (mCurrentDownloadTask.getStatus() == DownloadTask.DOWNLOADING) {
                    mCurrentDownloadTask.setStatus(DownloadTask.FINISHED);
                    broadcastTaskComplete(mCurrentDownloadTask);
                }
            }

            mNotifyManager.cancel(NOTIFICATION_ID);
            mServiceHandler.post(new Runnable() {
                @Override
                public void run() {
                    checkIfWorkComplete();
                }
            });

        }

        @Override
        public void onFileProgress(File file, String url, long bytesDownloaded, long totalBytes) {
            mCurrentDownloadItem.setStatus(DownloadItem.STATUS_INCOMPLETE);
            mCurrentDownloadItem.setBytesDownloaded(bytesDownloaded);

            if (mIsCurrentDownloadCancelled) {
                Timber.d( "onFileProgress: Cancelling current download task");
                mDownloadStack.stopDownload();
                broadcastTaskCancelled(mCurrentDownloadTask);
                mCurrentDownloadTask = null;
            } else if (!isRunning()) {
                mDownloadStack.stopDownload();
                mCurrentDownloadTask = null;
                Timber.d( "onFileProgress: Stopping download ");
            } else if (System.currentTimeMillis() - mLastNotificationTime >= 16) { //Don't overload the System thread with notification requests
                double progressPercent =
                        (double) mCurrentDownloadTask.getBytesDownloaded() / (double) mCurrentDownloadTask.getDownloadSize();
                mNotificationBuilder.setContentText(getString(
                        R.string.format_download_queue_progress,
                        (mTotalDownloadsQueued - mDownloadQueue.size() + 1),
                        mTotalDownloadsQueued));
                mNotificationBuilder.setProgress(100, (int) Math.round(progressPercent * 100), false);
                mNotifyManager.notify(NOTIFICATION_ID, mNotificationBuilder.build());
                broadcastTaskProgress(
                        mCurrentDownloadTask,
                        mCurrentDownloadTask.getBytesDownloaded(),
                        mCurrentDownloadTask.getDownloadSize());
                mLastNotificationTime = System.currentTimeMillis();
            }
        }

        @Override
        public void onFileComplete(String url, File tempFile) {
            mCurrentDownloadItem.setStatus(DownloadItem.STATUS_COMPLETE);
            broadcastTaskItemComplete(mCurrentDownloadTask, mCurrentDownloadItem);
        }

        @Override
        public void onFileError(String url) {
            mCurrentDownloadTask.setStatus(DownloadTask.INCOMPLETE);
            mCurrentDownloadItem.setStatus(DownloadItem.STATUS_ERROR);
            broadcastTaskError(mCurrentDownloadTask);
        }

        private void getTaskDownloadSize(DownloadTask downloadTask) {
            List<DownloadItem> downloadItemList = downloadTask.getDownloadItems();
            for (int i = 0; i < downloadItemList.size(); i++) {
                DownloadItem downloadItem = downloadItemList.get(i);
                long size = mDownloadStack.requestFileSize(downloadItem.getUrl());
                downloadItem.setDownloadSize(size);
            }
        }

    }
}

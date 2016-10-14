package au.com.roadhouse.example.localdownloadmanager;

import java.util.List;

import au.com.roadhouse.localdownloadmanager.model.DownloadItem;
import au.com.roadhouse.localdownloadmanager.model.DownloadTask;
import au.com.roadhouse.localdownloadmanager.toolkit.DownloadStatusService;
import timber.log.Timber;

public class DownloadHandlerService extends DownloadStatusService {

    public DownloadHandlerService() {
        super("DownloadHandlerService");
    }

    @Override
    protected void onDownloadTaskQueued(DownloadTask downloadTask) {
        Timber.d( "onDownloadTaskQueued: ");
    }

    @Override
    protected void onDownloadTaskProgress(DownloadTask downloadTask, long bytesDownloaded, long totalBytes) {
        Timber.d( "onDownloadTaskProgress: ");
    }

    @Override
    protected void onDownloadTaskCancelled(DownloadTask downloadTask) {
        Timber.d( "onDownloadTaskCancelled: ");

        cleanupCache(downloadTask.getDownloadItems());
    }

    @Override
    protected void onDownloadTaskError(DownloadTask downloadTask) {
        List<DownloadItem> dowloadItemList = downloadTask.getDownloadItems();

        cleanupCache(dowloadItemList);
    }
    @Override
    protected void onDownloadItemComplete(DownloadTask downloadTask, DownloadItem downloadItem) {
        //Handle each download item individually or wait until onDownloadTaskComplete triggers to handle all at once

        //Copy file to expected directory

        //Delete the cache data
        downloadItem.getFile().delete();
        Timber.d("onDownloadItemComplete: ");
    }

    @Override
    protected void onDownloadTaskComplete(DownloadTask downloadTask) {
        List<DownloadItem> dowloadItemList = downloadTask.getDownloadItems();
        cleanupCache(dowloadItemList);


        Timber.d("onDownloadTaskComplete: ");
    }

    private void cleanupCache(List<DownloadItem> dowloadItemList) {
        for (int i = 0; i < dowloadItemList.size(); i++) {

            //Delete any downloaded cache data
            if(dowloadItemList.get(i).getFile().exists()) {
                //noinspection ResultOfMethodCallIgnored
                dowloadItemList.get(i).getFile().delete();
            }
        }
    }
}

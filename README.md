[![Release](https://jitpack.io/v/roadhouse-dev/LocalDownloadManager.svg)](https://jitpack.io/#roadhouse-dev/LocalDownloadManager)

# LocalDownloadManager

An configurable but easy to use download service library

#Main features
* Download resuming if supported by server
* User progress notification handling
* Wifi download restriction with automatic resuming on connection change
* Download queue survives device reboot
* Download urls as single downloads, or group them under a single label

#Download

Add the JitPack repository to your root build file

```java 
allprojects {
    repositories {
        ...
        maven { url "https://jitpack.io" }
    }
}
```

Add the dependencies:

```java 
dependencies {
    compile 'com.github.roadhouse-dev:LocalDownloadManager:1.0.1'
}
```

#Build
```java
$ git clone https://github.com/roadhouse-dev/LocalDownloadManager.git
$ ./gradlew build
```

#Setup

1 - Extends DownloadStatusService to handle download status in a service

```java
public class DownloadHandlerService extends DownloadStatusService {

    public static final String TAG ="OfflineFileCacheService";

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

        //Data may have been downloaded before the task was cancelled, so lets clean it up
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

        //TODO: Copy file to expected directory

        //Delete the cache data
        downloadItem.getFile().delete();
    }

    @Override
    protected void onDownloadTaskComplete(DownloadTask downloadTask) {
        List<DownloadItem> dowloadItemList = downloadTask.getDownloadItems();
        //Try to clean up data again incase we missed one
        cleanupCache(dowloadItemList);
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
```


2 - Alter your AndroidManifest with your receiver and service

```xml
<application>
       ...
        <service android:name=".DownloadHandlerService"
            android:exported="false">
            <intent-filter>
                <action android:name="au.com.roadhouse.localdownloadmanager.toolkit.DownloadStatusService.ACTION_DOWNLOAD_STATUS_UPDATE"/>
            </intent-filter>
        </service>
        ...
</application>
```


#Sample Usage

Receive download update status changes in an activity or fragment

```java
public class MyActivity extends Activity {

    public void onCreate(...){
        ...
        mDownloadStatusReceiver = new MyDownloadStatusReciever();
        ...
    }
    
    public void onStart(){
        ...
        //Register for broadcasts (Must use the LocalBroadcastManager here)
        LocalBroadcastManager.getInstance(this).registerReceiver(
            mDownloadStatusReciever, 
            mDownloadStatusReciever.buildIntentFilter());
        ...
    }
    
     public void onStop(){
            ...
             //Unregister when done
            LocalBroadcastManager.getInstance(this).registerReceiver(
                mDownloadStatusReciever, 
                mDownloadStatusReciever.buildIntentFilter());
            ...
        }
    


    //Implement DownloadBroadcastReciever
    private class MyDownloadStatusReceiver extends DownloadBroadcastReceiver{
     @Override
            protected void onDownloadTaskQueuedReceived(DownloadTask downloadTask) {
                ...
            }
    
            @Override
            protected void onDownloadTaskProgressReceived(DownloadTask downloadTask, long bytesDownloaded, long totalBytes) {
                ...
            }
    
            @Override
            protected void onDownloadTaskCancelledReceived(DownloadTask downloadTask) {
                ...
            }
    
            @Override
            protected void onDownloadTaskErrorReceived(DownloadTask downloadTask) {
               ...
            }
    
            @Override
            protected void onDownloadItemCompleteReceived(DownloadTask downloadTask, DownloadItem downloadItem) {
               ...
            }
    
            @Override
            protected void onDownloadTaskCompleteReceived(DownloadTask downloadTask) {
               ...
            }
    }

}

```


Download a single url

```java
LocalDownloadManager downloadManager = new LocalDownloadManager(this);

DownloadTask downloadTask =
                new DownloadTask.Builder("LocalDownloadManager Repository")
                .addDownloadUrl(url)
                .setNotificationIcon(R.drawable.my_notification_icon)
                .build();

String singleDownloadTag = downloadManager.addDownloadToQueue(downloadTask);

```

Download a group of urls

```java
LocalDownloadManager downloadManager = new LocalDownloadManager(this);

DownloadTask downloadTask =
                new DownloadTask.Builder("Roadhouse Repositories")
                .addDownloadUrl("https://github.com/roadhouse-dev/RxDBFlow/archive/master.zip")
                .addDownloadUrl("https://github.com/roadhouse-dev/LocalDownloadManager/archive/master.zip")
                .setNotificationIcon(R.drawable.my_notification_icon)
                .build();

String groupDownloadTag = downloadManager.addDownloadToQueue(downloadTask);
```

#Bugs and Feedback
For bugs, questions, requests and discussions please use the [GitHub Issues](https://github.com/roadhouse-dev/LocalDownloadManager/issues).

#Pull Requests
All pull requests are welcome, however to make the whole process smoother please use the following guides

* All pull requests should be against the ```develop``` branch
* Code formatting should match the default Android Studio format
* Limit code changes to the scope of what you're implementing
* Provide standard JavaDoc for any public accessible members and classes
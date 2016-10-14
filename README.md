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
    compile 'com.github.roadhouse-dev:LocalDownloadManager:1.0.0'
}
```

#Build
```java
$ git clone https://github.com/roadhouse-dev/LocalDownloadManager.git
$ ./gradlew build
```

#Setup

1 - Create a broadcast listener to receive download status updates
```java
public class FileStatusReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Intent serviceIntent = new Intent(context, FileStatusHandlerService.class);
        serviceIntent.setAction(intent.getAction());
        serviceIntent.putExtras(intent.getExtras());
        context.startService(serviceIntent);
    }

```


2 - Create a service which handles the file status, and moves the downloaded file to the desired directory.
```java
public class DownloadHandlerService extends IntentService {

    public static final String TAG ="OfflineFileCacheService";

    public OfflineFileCacheService() {
        super("OfflineFileCacheService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (LocalDownloadService.ACTION_DOWNLOAD_COMPLETE.equals(intent.getAction())) {
            DownloadTask downloadTask = (DownloadTask) intent.getSerializableExtra(DownloadService.EXTRA_DOWNLOAD_TASK);
            if(downloadTask.getStatus() != DownloadTask.INCOMPLETE) {
                Timber.d( "onHandleIntent: Copying files to cache");
                onCopyToCache(intent);
            } else {
                Timber.d( "onHandleIntent: Error occurred, deleting files");
                onDownloadError(intent);
            }
        }
    }

    private void onDownloadError(Intent intent) {
        DownloadTask downloadTask = (DownloadTask) intent.getSerializableExtra(DownloadService.EXTRA_DOWNLOAD_TASK);

        List<DownloadItem> downloadItems = downloadTask.getDownloadItems();
        for (int i = 0; i < downloadItems.size(); i++) {
            DownloadItem downloadItem = downloadItems.get(i);

            //clean up the downloaded files from cache
            downloadItem.getFile().delete();
        }
    }

    private void onCopyToDirectory(Intent intent) {
          DownloadTask downloadTask = (DownloadTask) intent.getSerializableExtra(DownloadService.EXTRA_DOWNLOAD_TASK);
          List<DownloadItem> downloadItems = downloadTask.getDownloadItems();
          for (int i = 0; i < downloadItems.size(); i++) {
              DownloadItem downloadItem = downloadItems.get(i);
              
              try {
                   String fileName = Uri.parse(downloadItem.getUrl()).getLastPathSegment();
                   File outputFile = new File(context.getFilesDir(), fileName);
                   FileInputStream inputFileStream = new FileInputStream(downloadItem.getFile());
                   
                   byte[] copyBuffer = new byte[1024];
                   FileOutputStream outputStream = new FileOutputStream(file);
                   int remaining;
                   while ((remaining = inputStream.read(fileBuffer)) != -1) {
                       outputStream.write(fileBuffer, 0, remaining);
                   }
                   inputStream.close();
                   outputStream.close();
                                                
                   
              } catch (IOException e) {
                  Log.w(TAG, "put: Failed to copy temp file over to cache directory", e);
                  e.printStackTrace();
              } 
  
              //clean up the downloaded files from cache
              downloadItem.getFile().delete();
          }
    }
}
```


3 - Alter your AndroidManifest with your receiver and service

```xml
<application>
       ...
        <receiver android:name=".service.OfflineFileStatusReceiver">
            <intent-filter>
                <action android:name="au.com.roadhouse.filedownloaderservice.DownloadService.ACTION_DOWNLOAD_COMPLETE"/>
                <action android:name="au.com.roadhouse.filedownloaderservice.DownloadService.ACTION_DOWNLOAD_ERROR"/>
            </intent-filter>
        </receiver>

        <service android:name=".service.OfflineFileCacheService"/>

        <provider
            android:name="android.support.v4.content.FileProvider"
            android:authorities="${applicationId}.file.provider"
            android:exported="false"
            android:grantUriPermissions="true">
            <meta-data
                android:name="android.support.FILE_PROVIDER_PATHS"
                android:resource="@xml/offile_file_paths" />
        </provider>
        ...
</application>
```

#Sample Usage

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
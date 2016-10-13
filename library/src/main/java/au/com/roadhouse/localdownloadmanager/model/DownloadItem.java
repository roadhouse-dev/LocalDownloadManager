package au.com.roadhouse.localdownloadmanager.model;


import android.os.Parcel;
import android.os.Parcelable;

import java.io.File;
import java.io.Serializable;

/**
 * Represents a single download item within a download task.
 */
public class DownloadItem implements Parcelable, Serializable {

    public static final int STATUS_WAITING = 0;
    public static final int STATUS_INCOMPLETE = 1;
    public static final int STATUS_COMPLETE = 2;
    public static final int STATUS_ERROR = -1;

    private String mUrl;
    private int mStatus;
    private long mDownloadSize;
    private long mBytesDownloaded;
    private File mFile;

    DownloadItem(){
    }

    private DownloadItem(Parcel in) {
        mUrl = in.readString();
        mStatus = in.readInt();
        mDownloadSize = in.readLong();
        mBytesDownloaded = in.readLong();
        mFile = (File) in.readSerializable();
    }

    public static final Creator<DownloadItem> CREATOR = new Creator<DownloadItem>() {
        @Override
        public DownloadItem createFromParcel(Parcel in) {
            return new DownloadItem(in);
        }

        @Override
        public DownloadItem[] newArray(int size) {
            return new DownloadItem[size];
        }
    };

    public String getUrl() {
        return mUrl;
    }

    public void setUrl(String url) {
        mUrl = url;
    }

    public int getStatus() {
        return mStatus;
    }

    public void setStatus(int status) {
        mStatus = status;
    }

    public void setDownloadSize(long downloadSize) {
        mDownloadSize = downloadSize;
    }

    public void setBytesDownloaded(long bytesDownloaded) {
        mBytesDownloaded = bytesDownloaded;
    }

    public long getBytesDownloaded() {
        return mBytesDownloaded;
    }

    public long getDownloadSize() {
        return mDownloadSize;
    }

    public File getFile() {
        return mFile;
    }

    public void setFile(File file) {
        mFile = file;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mUrl);
        dest.writeInt(mStatus);
        dest.writeLong(mDownloadSize);
        dest.writeLong(mBytesDownloaded);
        dest.writeSerializable(mFile);
    }
}

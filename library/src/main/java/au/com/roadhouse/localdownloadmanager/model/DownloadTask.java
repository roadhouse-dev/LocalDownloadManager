package au.com.roadhouse.localdownloadmanager.model;


import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.DrawableRes;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import au.com.roadhouse.localdownloadmanager.R;
import au.com.roadhouse.localdownloadmanager.internal.SerializablePendingIntent;

/**
 * Represents all the required information for a download. A download task can contain
 * one or more download items.
 */
public class DownloadTask implements Parcelable, Serializable, Comparable {

    public static final int PENDING = 0;
    public static final int DOWNLOADING = 1;
    public static final int FINISHED = 2;
    public static final int INCOMPLETE = -1;

    private int mPriority;
    private String mTag;
    private String mLabel;
    private List<DownloadItem> mDownloadItemsList = new ArrayList<>();;
    private int mStatus;
    private SerializablePendingIntent mNotificationPendingIntent;
    private int mNotificationIcon;

    public DownloadTask() {
        mNotificationIcon = R.drawable.ic_default_notify;
    }

    public DownloadTask(String tag){
        this();
        mTag = tag;
    }

    private DownloadTask(Parcel in) {
        mPriority = in.readInt();
        mTag = in.readString();
        mLabel = in.readString();
        mDownloadItemsList = in.createTypedArrayList(DownloadItem.CREATOR);
        mStatus = in.readInt();
        mNotificationIcon = in.readInt();
        mNotificationPendingIntent = (SerializablePendingIntent) in.readSerializable();
    }

    public static final Creator<DownloadTask> CREATOR = new Creator<DownloadTask>() {
        @Override
        public DownloadTask createFromParcel(Parcel in) {
            return new DownloadTask(in);
        }

        @Override
        public DownloadTask[] newArray(int size) {
            return new DownloadTask[size];
        }
    };

    private DownloadTask(Builder builder) {
        mTag = builder.mTag;
        mPriority = builder.mPriority;
        mLabel = builder.mLabel;
        mNotificationPendingIntent = builder.mNotificationPendingIntent;
        mNotificationIcon = builder.mNotificationIcon;
        for (int i = 0; i < builder.mDownloadUrls.size(); i++) {
            addDownloadUrl(builder.mDownloadUrls.get(i));
        }
    }

    public String getLabel() {
        return mLabel;
    }

    /**
     * Sets the label to display on the download notification
     * @param label The label to display on the download notification
     */
    public void setLabel(String label) {
        mLabel = label;
    }

    public String getTag() {
        return mTag;
    }

    public int getNotificationIcon() {
        return mNotificationIcon;
    }

    /**
     * Sets a custom notification icon for this download task
     * @param notificationIcon The drawable notification id
     */
    public void setNotificationIcon(@DrawableRes int notificationIcon) {
        mNotificationIcon = notificationIcon;
    }

    public SerializablePendingIntent getNotificationPendingIntent() {
        return mNotificationPendingIntent;
    }

    /**
     * Sets a pending intent which will be triggered if the user clicks the notification while the
     * downlaod task is in progress
     * @param pendingIntent The pending intent to trigger
     */
    public void setNotificationPendingIntent(SerializablePendingIntent pendingIntent){
        mNotificationPendingIntent = pendingIntent;
    }

    /**
     * Adds a new download url to the DownloadTask
     * @param url the url of the item to download
     */
    public void addDownloadUrl(String url) {
        DownloadItem downloadItem = new DownloadItem();
        downloadItem.setUrl(url);
        mDownloadItemsList.add(downloadItem);
    }

    public List<DownloadItem> getDownloadItems() {
        return mDownloadItemsList;
    }

    public void setStatus(int status) {
        mStatus = status;
    }

    public int getStatus() {
        return mStatus;
    }

    public long getDownloadSize() {
        long totalDownloadSize = 0;
        for (int i = 0; i < mDownloadItemsList.size(); i++) {
            totalDownloadSize += mDownloadItemsList.get(i).getDownloadSize();
        }

        return totalDownloadSize;
    }

    public long getBytesDownloaded() {
        long totalBytesDownloaded = 0;
        for (int i = 0; i < mDownloadItemsList.size(); i++) {
            totalBytesDownloaded += mDownloadItemsList.get(i).getBytesDownloaded();
        }

        return totalBytesDownloaded;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj == null){
            return false;
        } else if(obj instanceof DownloadTask){
            return ((DownloadTask) obj).mTag.equals(mTag);
        } else if (obj instanceof String){
            return obj.equals(mTag);
        } else {
            return false;
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(mPriority);
        dest.writeString(mTag);
        dest.writeString(mLabel);
        dest.writeTypedList(mDownloadItemsList);
        dest.writeInt(mStatus);
        dest.writeInt(mNotificationIcon);
        dest.writeSerializable(mNotificationPendingIntent);
    }

    @Override
    public int compareTo(Object o) {
        if(o instanceof DownloadTask){
            if(mPriority < ((DownloadTask) o).mPriority){
                return -1;
            } else if (mPriority > ((DownloadTask) o).mPriority){
                return 1;
            }
        }

        return 0;
    }

    /**
     * Builds a new DownloadTask instance
     */
    public static class Builder {
        private final String mTag;
        private int mNotificationIcon = R.drawable.ic_default_notify;
        private int mPriority;
        private String mLabel;
        private List<String> mDownloadUrls = new ArrayList<>();
        private SerializablePendingIntent mNotificationPendingIntent;

        /**
         * Creates a new DownloadTask builder
         * @param label The label to display on the download notification while this task is in
         *              progress
         */
        public Builder(String label){
            mLabel = label;
            mTag = UUID.randomUUID().toString();
        }

        /**
         * Adds a new download url to the DownloadTask
         * @param url The url to download
         * @return A Builder instance
         */
        public Builder addDownloadUrl(String url){
            mDownloadUrls.add(url);
            return this;
        }

        /**
         * Sets the priority of this download task relative to any other downloads tasks that are
         * currently in the que. The DownloadTask with the highest priority will be downloaded first.
         * @param priority The priority of the DownloadTask
         * @return A Builder instance
         */
        public Builder setPriority(int priority){
            mPriority = priority;
            return this;
        }

        /**
         * Sets the notification icon to display while this task is in progress.
         * @param resourceId The icon resource id
         * @return A builder instance
         */
        public Builder setNotificationIcon(@DrawableRes int resourceId){
            mNotificationIcon = resourceId;
            return this;
        }

        public DownloadTask build() {
            return new DownloadTask(this);
        }
    }
}

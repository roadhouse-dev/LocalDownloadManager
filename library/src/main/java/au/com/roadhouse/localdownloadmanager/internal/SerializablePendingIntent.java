package au.com.roadhouse.localdownloadmanager.internal;


import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import java.io.Serializable;

/**
 * A pending intent that can be serialized rather than parceled. Given the intent itself is
 * seralized, any extras that cannot be serialized will be ignored.
 */
public class SerializablePendingIntent implements Serializable {

    private static final int TYPE_ACTIVITY = 0;
    private static final int TYPE_SERVICE = 1;
    private static final int TYPE_BROADCAST = 2;
    private static final String TAG = "SPendingIntent";

    private SerializableIntent mSerializableIntent;
    private int mType;
    private int mRequestCode;
    private int mFlags;

    private SerializablePendingIntent(){
       //Do nothing
    }

    /**
     * Creates a new SeralizablePendingIntent for an activity
     * @param intent The intent to run
     * @param requestCode The request code
     * @param flags Intent flags {@link PendingIntent#getActivity(Context, int, Intent, int)}
     * @return A SeralizablePendingIntent instance
     */
    public static SerializablePendingIntent getActivity(Intent intent, int requestCode, int flags){
        SerializablePendingIntent serializablePendingIntent = new SerializablePendingIntent();
        serializablePendingIntent.mSerializableIntent = SerializableIntent.createFromIntent(intent);
        serializablePendingIntent.mType = TYPE_ACTIVITY;
        serializablePendingIntent.mRequestCode = requestCode;
        serializablePendingIntent.mFlags = flags;

        return serializablePendingIntent;
    }

    /**
     * Creates a new SeralizablePendingIntent for a broadcast
     * @param intent The intent to run
     * @param requestCode The request code
     * @param flags Intent flags {@link PendingIntent#getBroadcast(Context, int, Intent, int)}
     * @return A SeralizablePendingIntent instance
     */
    public static SerializablePendingIntent getBroadcast(Intent intent, int requestCode, int flags){
        SerializablePendingIntent serializablePendingIntent = new SerializablePendingIntent();
        serializablePendingIntent.mSerializableIntent = SerializableIntent.createFromIntent(intent);
        serializablePendingIntent.mType = TYPE_BROADCAST;
        serializablePendingIntent.mRequestCode = requestCode;
        serializablePendingIntent.mFlags = flags;

        return serializablePendingIntent;
    }

    /**
     * Creates a new SeralizablePendingIntent for a service
     * @param intent The intent to run
     * @param requestCode The request code
     * @param flags Intent flags {@link PendingIntent#getService(Context, int, Intent, int)}
     * @return A SeralizablePendingIntent instance
     */
    public static SerializablePendingIntent getService(Intent intent, int requestCode, int flags){
        SerializablePendingIntent serializablePendingIntent = new SerializablePendingIntent();
        serializablePendingIntent.mSerializableIntent = SerializableIntent.createFromIntent(intent);
        serializablePendingIntent.mType = TYPE_SERVICE;
        serializablePendingIntent.mRequestCode = requestCode;
        serializablePendingIntent.mFlags = flags;

        return serializablePendingIntent;
    }

    /**
     * Builds a pending intent from a SerializablePendingIntent instance
     * @param context A valid context
     * @return A PendingIntent instance
     */
    public PendingIntent buildPendingIntent(Context context){
        Intent intent = mSerializableIntent.buildIntent( );
        switch(mType){
            case TYPE_ACTIVITY:
                return PendingIntent.getActivity(context, mRequestCode, intent, mFlags);
            case TYPE_BROADCAST:
                return PendingIntent.getBroadcast(context, mRequestCode, intent, mFlags);
            case TYPE_SERVICE:
                return PendingIntent.getService(context, mRequestCode, intent, mFlags);
            default:
                return null;
        }
    }


}

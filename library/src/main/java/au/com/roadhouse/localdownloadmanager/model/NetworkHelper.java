package au.com.roadhouse.localdownloadmanager.model;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * A helper class which allows the retrieve of the current network state, and to listen to network
 * connectivity changes.
 */
public class NetworkHelper extends BroadcastReceiver {

    public final static int TYPE_WIFI = 0;
    public final static int TYPE_NO_CONNECTION = 1;
    public final static int TYPE_MOBILE = 2;

    private final Context mContext;
    private OnNetworkStateChangeListener mListener;

    public NetworkHelper(Context context){
        mContext = context;
    }

    public int getCurrentConnection(){
        ConnectivityManager connectivityManager =
                (ConnectivityManager)mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();

        if(isConnected && activeNetwork.getType() == ConnectivityManager.TYPE_WIFI){
            return TYPE_WIFI;
        } else if(isConnected){
            return TYPE_MOBILE;
        }  else {
            return TYPE_NO_CONNECTION;
        }
    }

    public void registerForNetworkChangeEvents(OnNetworkStateChangeListener listener){
        mListener = listener;
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        intentFilter.addCategory(Intent.CATEGORY_DEFAULT);

        mContext.registerReceiver(this, intentFilter);
    }

    public void unregisterForNetworkChangeEvents(){
        mContext.unregisterReceiver(this);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if(mListener != null){
            mListener.onNetworkConnectionChange(getCurrentConnection());
        }
    }


    /**
     * A callback which provides network connectivity change information
     */
    public interface OnNetworkStateChangeListener {

        /**
         * Triggers when the network connectivity changes
         * @param connectionType The current connection type
         */
        void onNetworkConnectionChange(int connectionType);
    }
}

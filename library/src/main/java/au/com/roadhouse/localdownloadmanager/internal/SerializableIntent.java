package au.com.roadhouse.localdownloadmanager.internal;

import android.content.ComponentName;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import timber.log.Timber;

/**
 * An intent that can be serialized rather than parceled. Due to the nature of serialization, this
 * intent only supports serializable extras.
 */
class SerializableIntent implements Serializable {

    private String mAction;
    private String mData;
    private String mIntentType;
    private List<String> mCategories = new ArrayList<>();
    private String mPackage;
    private int mIntentFlags;
    private HashMap<String, Serializable> mExtras = new HashMap<>();
    private String mComponent;

    /**
     * Creates a new SerializableIntent instance from an intent instance
     * @param intent The intent to create the SerializableIntent from
     * @return A SerializableIntent matching the target intent where possible.
     */
    static SerializableIntent createFromIntent(Intent intent) {
        SerializableIntent serializableIntent = new SerializableIntent();
        if (intent.getComponent() != null) {
            serializableIntent.mComponent = intent.getComponent().flattenToString();
        }
        serializableIntent.mAction = intent.getAction();
        if (intent.getData() != null) {
            serializableIntent.mData = intent.getData().toString();
        }
        if (intent.getCategories() != null) {
            serializableIntent.mCategories.addAll(intent.getCategories());
        }
        serializableIntent.mIntentType = intent.getType();
        serializableIntent.mPackage = intent.getPackage();
        serializableIntent.mIntentFlags = intent.getFlags();

        Bundle extras = intent.getExtras();
        if (extras != null) {
            for (String key : extras.keySet()) {
                Object extra = extras.get(key);
                if (extra instanceof Serializable) {
                    serializableIntent.mExtras.put(key, (Serializable) extra);
                } else if (extra != null) {
                    Timber.w("createFromIntent: " + extra.getClass() + "does not support serialization");
                }
            }
        }

        return serializableIntent;
    }

    /**
     * Builds an intent from a SerializableIntent instance
     * @return A Intent instance
     */
    @SuppressWarnings("unchecked")
    Intent buildIntent() {
        Intent intent = new Intent();

        if (mComponent != null) {
            intent.setComponent(ComponentName.unflattenFromString(mComponent));
        }

        if (mAction != null) {
            intent.setAction(mAction);
        }

        if (mData != null) {
            intent.setData(Uri.parse(mData));
        }

        if (mIntentType != null) {
            intent.setType(mIntentType);
        }

        if (mPackage != null) {
            intent.setPackage(mPackage);
        }

        intent.setFlags(mIntentFlags);

        for (int i = 0; i < mCategories.size(); i++) {
            intent.addCategory(mCategories.get(i));
        }

        Bundle bundle = new Bundle();
        for (String key : mExtras.keySet()) {
            Object value = mExtras.get(key);

            if (value instanceof Byte) {
                bundle.putByte(key, (Byte) value);
            } else if (value instanceof byte[]) {
                bundle.putByteArray(key, (byte[]) value);
            } else if (value instanceof Character) {
                bundle.putChar(key, (Character) value);
            } else if (value instanceof char[]) {
                bundle.putCharArray(key, (char[]) value);
            } else if (value instanceof Float) {
                bundle.putFloat(key, (Float) value);
            } else if (value instanceof float[]) {
                bundle.putFloatArray(key, (float[]) value);
            } else if (value instanceof Integer) {
                bundle.putInt(key, (Integer) value);
            } else if (value instanceof int[]) {
                bundle.putIntArray(key, (int[]) value);
            } else if (value instanceof Short) {
                bundle.putShort(key, (Short) value);
            } else if (value instanceof short[]) {
                bundle.putShortArray(key, (short[]) value);
            } else if (value instanceof Long) {
                bundle.putLong(key, (Long) value);
            } else if (value instanceof long[]) {
                bundle.putLongArray(key, (long[]) value);
            } else if (value instanceof ArrayList) {
                ArrayList valueList = (ArrayList) value;
                if (valueList.size() > 0 && valueList.get(0) instanceof Integer) {
                    bundle.putIntegerArrayList(key, valueList);
                } else if (valueList.size() > 0 && valueList.get(0) instanceof String) {
                    bundle.putStringArrayList(key, valueList);
                }
            } else {
                bundle.putSerializable(key, (Serializable) value);
            }

        }

        intent.putExtras(bundle);

        return intent;
    }


}

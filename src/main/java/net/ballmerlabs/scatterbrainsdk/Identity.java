package net.ballmerlabs.scatterbrainsdk;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;

public class Identity implements Parcelable {

    protected final Map<String, byte[]> mPubKeymap;
    protected final byte[] mScatterbrainPubKey;
    protected final String givenname;
    protected final byte[] sig;
    protected final String fingerprint;
    protected boolean hasPrivateKey;

    protected Identity(
            Map<String, byte[]> map, 
            byte[] pub, 
            String name, 
            byte[] sig, 
            String fingerprint, 
            boolean hasPrivateKey
    ) {
        this.mPubKeymap = map;
        this.mScatterbrainPubKey = pub;
        this.givenname = name;
        this.sig = sig;
        this.fingerprint = fingerprint;
        this.hasPrivateKey = hasPrivateKey;
    }

    @FunctionalInterface
    private interface ParcelWriter<T> {
        void writeToParcel(@NonNull final T value,
                           @NonNull final Parcel parcel, final int flags);
    }

    @FunctionalInterface
    private interface ParcelReader<T> {
        T readFromParcel(@NonNull final Parcel parcel);
    }

    private static <K, V> void writeParcelableMap(
            @NonNull final Map<K, V> map,
            @NonNull final Parcel parcel,
            final int flags,
            @NonNull final ParcelWriter<Map.Entry<K, V>> parcelWriter) {
        parcel.writeInt(map.size());

        for (final Map.Entry<K, V> e : map.entrySet()) {
            parcelWriter.writeToParcel(e, parcel, flags);
        }
    }

    private static <K, V> Map<K, V> readParcelableMap(
            @NonNull final Parcel parcel,
            @NonNull final ParcelReader<Map.Entry<K, V>> parcelReader) {
        int size = parcel.readInt();
        final Map<K, V> map = new HashMap<>(size);

        for (int i = 0; i < size; i++) {
            final Map.Entry<K, V> value = parcelReader.readFromParcel(parcel);
            map.put(value.getKey(), value.getValue());
        }
        return map;
    }

    protected Identity(Parcel in) {
        mPubKeymap = readParcelableMap(in, parcel -> {
            final int len = parcel.readInt();
            final byte[] key = new byte[len];
            parcel.readByteArray(key);
            return new AbstractMap.SimpleEntry<>(parcel.readString(), key);
        });
        mScatterbrainPubKey = mPubKeymap.get(ScatterbrainApi.PROTOBUF_PRIVKEY_KEY);
        givenname = in.readString();
        sig = new byte[in.readInt()];
        in.readByteArray(sig);
        fingerprint = in.readString();
        hasPrivateKey = hasKey(in.readByte());
    }

    public static final Creator<Identity> CREATOR = new Creator<Identity>() {
        @Override
        public Identity createFromParcel(Parcel in) {
            return new Identity(in);
        }

        @Override
        public Identity[] newArray(int size) {
            return new Identity[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        writeParcelableMap(mPubKeymap, parcel, i, (mapentry, p, __) -> {
            p.writeInt(mapentry.getValue().length);
            p.writeByteArray(mapentry.getValue());
            p.writeString(mapentry.getKey());
        });
        parcel.writeString(givenname);
        parcel.writeInt(sig.length);
        parcel.writeByteArray(sig);
        parcel.writeString(fingerprint);
        parcel.writeByte(hasKey(hasPrivateKey));
    }

    public Map<String, byte[]> getmPubKeymap() {
        return mPubKeymap;
    }

    public byte[] getmScatterbrainPubKey() {
        return mScatterbrainPubKey;
    }

    public String getGivenname() {
        return givenname;
    }

    public byte[] getSig() {
        return sig;
    }

    public String getFingerprint() {
        return fingerprint;
    }
    
    public boolean hasPrivateKey() {
        return hasPrivateKey;
    }

    protected void setHasPrivateKey(boolean key) {
        this.hasPrivateKey = key;
    }


    private static boolean hasKey(int val) {
        if (val == 0) {
            return true;
        } else {
            return false;
        }
    }

    private static byte hasKey(boolean val) {
        if (val) {
            return 0;
        } else {
            return 1;
        }
    }
}

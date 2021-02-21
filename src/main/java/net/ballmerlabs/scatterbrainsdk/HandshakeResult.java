package net.ballmerlabs.scatterbrainsdk;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

public class HandshakeResult implements Parcelable {


    public enum TransactionStatus {
        STATUS_SUCCESS,
        STATUS_FAIL
    }
    
    protected HandshakeResult(Parcel in) {
        identities = in.readInt();
        messages = in.readInt();
        status = TransactionStatus.values()[in.readInt()];
    }

    public static final Creator<HandshakeResult> CREATOR = new Creator<HandshakeResult>() {
        @Override
        public HandshakeResult createFromParcel(Parcel in) {
            return new HandshakeResult(in);
        }

        @Override
        public HandshakeResult[] newArray(int size) {
            return new HandshakeResult[size];
        }
    };

    public final int identities;
    public final int messages;
    public final TransactionStatus status;

    public HandshakeResult(
            int identities,
            int messages,
            TransactionStatus status
    ) {
        this.status = status;
        this.messages = messages;
        this.identities = identities;
    }

    @NonNull
    public HandshakeResult from(HandshakeResult stats) {
        final TransactionStatus status;
        if (stats.status == TransactionStatus.STATUS_FAIL ||
                this.status == TransactionStatus.STATUS_FAIL) {
            status = TransactionStatus.STATUS_FAIL;
        } else {
            status = stats.status;
        }
        return new HandshakeResult(
                stats.identities + this.identities,
                stats.messages + this.messages,
                status
        );
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(identities);
        parcel.writeInt(messages);
        parcel.writeInt(status.ordinal());
    }

}
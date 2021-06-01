package net.ballmerlabs.scatterbrainsdk;

import android.net.Uri;
import android.os.BadParcelableException;
import android.os.Build;
import android.os.Parcel;
import android.os.ParcelFileDescriptor;
import android.os.Parcelable;
import android.webkit.MimeTypeMap;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Date;
import java.util.concurrent.atomic.AtomicReference;

public class ScatterMessage implements Parcelable {
    protected final byte[] body;
    protected final String fromFingerprint;
    protected final String toFingerprint;
    protected final AtomicReference<byte[]> sig = new AtomicReference<>();
    protected final String fingerprint;
    protected final String application;
    protected final String extension;
    protected final String mime;
    protected final String filename;
    protected final Date sendDate;
    protected final Date receiveDate;
    protected final ParcelFileDescriptor fileDescriptor;
    protected final int toDisk;
    public static final int DISK = 1;
    public static final int NODISK = 0;

    protected int validateBody(int val) {
        if (val > ScatterbrainApi.MAX_BODY_SIZE) {
            throw new BadParcelableException("invalid array size");
        }
        return val;
    }

    protected ScatterMessage(Parcel in) {
        body = new byte[validateBody(in.readInt())];
        in.readByteArray(body);
        this.fromFingerprint = in.readString();
        this.toFingerprint = in.readString();
        this.application = in.readString();
        this.extension = in.readString();
        this.mime = in.readString();
        this.filename = in.readString();
        this.fileDescriptor = in.readFileDescriptor();
        this.fingerprint = in.readString();
        this.toDisk = in.readInt();
        this.sendDate = new Date(in.readLong());
        this.receiveDate = new Date(in.readLong());
    }

    protected ScatterMessage(Builder builder) {
        this.body = builder.body;
        this.fromFingerprint = builder.fromFingerprint;
        this.toFingerprint = builder.toFingerprint;
        this.application = builder.application;
        this.extension = builder.extension;
        this.mime = builder.mime;
        this.filename = builder.filename;
        this.fileDescriptor = builder.fileDescriptor;
        this.fingerprint = builder.fingerprint;
        this.toDisk = builder.todisk;
        this.sendDate = builder.sendDate;
        this.receiveDate = builder.receiveDate;
    }

    public static final Creator<ScatterMessage> CREATOR = new Creator<ScatterMessage>() {
        @Override
        public ScatterMessage createFromParcel(Parcel in) {
            return new ScatterMessage(in);
        }

        @Override
        public ScatterMessage[] newArray(int size) {
            return new ScatterMessage[size];
        }
    };

    @Override
    public int describeContents() {
        return Parcelable.CONTENTS_FILE_DESCRIPTOR;
    }

    public boolean toDisk() {
        return toDisk == DISK;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(body.length);
        parcel.writeByteArray(body);
        parcel.writeString(fromFingerprint);
        parcel.writeString(toFingerprint);
        parcel.writeString(application);
        parcel.writeString(extension);
        parcel.writeString(mime);
        parcel.writeString(filename);
        parcel.writeFileDescriptor(fileDescriptor.getFileDescriptor());
        parcel.writeString(fingerprint);
        parcel.writeInt(toDisk);
    }

    public byte[] getBody() {
        return body;
    }

    public String getFromFingerprint() {
        return fromFingerprint;
    }

    public String getToFingerprint() {
        return toFingerprint;
    }

    public String getApplication() {
        return application;
    }

    public String getExtension() {
        return extension;
    }

    public String getMime() {
        return mime;
    }

    public String getFilename() {
        return filename;
    }

    public boolean hasIdentity() {
        return !this.fingerprint.equals("");
    }

    public String getIdentityFingerprint() {
        return this.fingerprint;
    }

    public ParcelFileDescriptor getFileDescriptor() {
        return fileDescriptor;
    }

    public byte[] getSig() {
        return sig.get();
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public static class Builder {
        protected byte[] body;
        protected String fromFingerprint;
        protected String toFingerprint;
        protected String application;
        protected String extension;
        protected String mime;
        protected String filename;
        protected ParcelFileDescriptor fileDescriptor;
        protected String fingerprint = "";
        protected boolean fileNotFound = false;
        protected int todisk = NODISK;
        protected Date sendDate = new Date(0L);
        protected Date receiveDate = new Date(0L);

        protected Builder() {
            this.fingerprint = "";
        }

        public Builder setBody(byte[] body) {
            this.body = body;
            this.todisk = NODISK;
            return this;
        }

        public Builder setTo(String to) {
            this.toFingerprint = to;
            return this;
        }

        public Builder setFrom(String from) {
            this.fromFingerprint = from;
            return this;
        }

        public Builder setApplication(String application) {
            this.application = application;
            return this;
        }

        public Builder setSendDate(Date sendDate) {
            this.sendDate = sendDate;
            return this;
        }

        public Builder setFile(File file, int mode) {
           if (file != null) {
               try {
                   this.fileDescriptor = ParcelFileDescriptor.open(file, mode);
                   this.extension = MimeTypeMap.getFileExtensionFromUrl(Uri.fromFile(file).toString());
                   this.mime = ScatterbrainApi.getMimeType(file);
                   this.filename = file.getName();
                   this.todisk = DISK;
               } catch (FileNotFoundException e) {
                   this.fileDescriptor = null;
                   this.mime = null;
                   this.filename = null;
                   this.extension = null;
                   fileNotFound = true;
               }
           }
            return this;
        }

        public Builder setFile(ParcelFileDescriptor descriptor, String ext, String mime, String name) {
            this.fileDescriptor = descriptor;
            this.extension = ext;
            this.mime = mime;
            this.filename = name;
            this.todisk = DISK;
            return this;
        }

        protected void verify() {
            if (body != null && fileDescriptor != null) {
                throw new IllegalArgumentException("must set one of body or file");
            }

            if (body == null && fileDescriptor == null) {
                throw new IllegalArgumentException("set either body or file");
            }

            if (application == null) {
                throw new IllegalArgumentException("applicaiton must be set");
            }

            if (fileNotFound) {
                throw new IllegalStateException("file not found");
            }
        }

        public ScatterMessage build() {
            verify();
            return new ScatterMessage(this);
        }
    }
}

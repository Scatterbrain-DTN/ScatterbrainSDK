package net.ballmerlabs.scatterbrainsdk;

import android.content.Context;
import android.provider.DocumentsContract;
import android.webkit.MimeTypeMap;

import net.ballmerlabs.scatterbrainsdk.internal.DaggerSdkComponent;
import net.ballmerlabs.scatterbrainsdk.internal.SdkComponent;

import java.io.File;
import java.util.Objects;

public class ScatterbrainApi {
    public static int MAX_BODY_SIZE = 1024*1024*4;
    public static final String EXTRA_TRANSACTION_RESULT = "transaction_result";
    public static final String PROTOBUF_PRIVKEY_KEY = "scatterbrain";
    private final SdkComponent sdkComponent;

    public ScatterbrainApi(Context applicationContext) {
        sdkComponent = Objects.requireNonNull(
                DaggerSdkComponent.builder().applicationContext(applicationContext)
        ).build();
    }

    public BinderWrapper getBinderWrapper() {
        return sdkComponent.sdk();
    }

    public ScatterbrainBroadcastReceiver getBroadcastReceiver() {
        return sdkComponent.broadcastReceiver();
    }

    public static String getMimeType(File file) {
        if (file.isDirectory()) {
            return DocumentsContract.Document.MIME_TYPE_DIR;
        } else {
            final String name = file.getName();
            final int lastDot = name.lastIndexOf('.');
            if (lastDot >= 0) {
                final String extension = name.substring(lastDot + 1).toLowerCase();
                final String mime = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
                if (mime != null) return mime;
            }
            return "application/octet-stream";
        }
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        sdkComponent.broadcastReceiver().unregister();
        sdkComponent.sdk().unregisterCallback();
    }
}

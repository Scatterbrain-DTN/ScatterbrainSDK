package net.ballmerlabs.scatterbrainsdk;

import android.provider.DocumentsContract;
import android.webkit.MimeTypeMap;

import java.io.File;

public class ScatterbrainApi {
    public static int MAX_BODY_SIZE = 1024*1024*4;
    public static final String EXTRA_TRANSACTION_RESULT = "transaction_result";
    public static final String PROTOBUF_PRIVKEY_KEY = "scatterbrain";
    public static final String KEYSTORE_ID = "scatterbrainkeystore";

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
}

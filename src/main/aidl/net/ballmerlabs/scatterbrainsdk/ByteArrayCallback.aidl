// ByteArrayCallback.aidl
package net.ballmerlabs.scatterbrainsdk;

// Declare any non-default types here with import statements

interface ByteArrayCallback {
    void onError(in String error);
    void onData(in byte[] data);
}
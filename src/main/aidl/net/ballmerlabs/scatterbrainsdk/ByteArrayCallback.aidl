// ByteArrayCallback.aidl
package net.ballmerlabs.scatterbrainsdk;

// Declare any non-default types here with import statements

interface ByteArrayCallback {
    oneway void onError(in String error);
    oneway void onData(in byte[] data);
}
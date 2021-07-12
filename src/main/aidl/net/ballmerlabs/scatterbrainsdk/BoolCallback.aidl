// BoolCallback.aidl
package net.ballmerlabs.scatterbrainsdk;

// Declare any non-default types here with import statements

interface BoolCallback {
    void onError(in String error);
    void onResult(boolean result);
}
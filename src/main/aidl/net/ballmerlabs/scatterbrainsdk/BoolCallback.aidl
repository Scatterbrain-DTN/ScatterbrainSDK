// BoolCallback.aidl
package net.ballmerlabs.scatterbrainsdk;

// Declare any non-default types here with import statements

interface BoolCallback {
    oneway void onError(in String error);
    oneway void onResult(boolean result);
}
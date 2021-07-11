// UnitCallback.aidl
package net.ballmerlabs.scatterbrainsdk;

interface UnitCallback {
    void onError(in String error);
    void onComplete();
}
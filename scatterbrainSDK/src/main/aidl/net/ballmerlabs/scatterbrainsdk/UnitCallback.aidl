// UnitCallback.aidl
package net.ballmerlabs.scatterbrainsdk;

interface UnitCallback {
    oneway void onError(in String error);
    oneway void onComplete();
}
// ScatterMessageCallback.aidl
package net.ballmerlabs.scatterbrainsdk;
import net.ballmerlabs.scatterbrainsdk.ScatterMessage;

interface ScatterMessageCallback {
    oneway void onError(in String error);
    oneway void onScatterMessage(in ScatterMessage message);
    oneway void onComplete();
}
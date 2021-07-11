// ScatterMessageCallback.aidl
package net.ballmerlabs.scatterbrainsdk;
import net.ballmerlabs.scatterbrainsdk.ScatterMessage;

interface ScatterMessageCallback {
    void onError(in String error);
    void onScatterMessage(in List<ScatterMessage> message);
}
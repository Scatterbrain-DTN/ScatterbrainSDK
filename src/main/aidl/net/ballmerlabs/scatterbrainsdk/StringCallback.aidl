// StringCallback.aidl
package net.ballmerlabs.scatterbrainsdk;

interface StringCallback {
   oneway void onError(in String error);
   oneway void onString(in List<String> result);
}
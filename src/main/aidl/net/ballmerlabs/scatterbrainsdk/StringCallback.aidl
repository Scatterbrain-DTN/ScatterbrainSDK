// StringCallback.aidl
package net.ballmerlabs.scatterbrainsdk;

interface StringCallback {
   void onError(in String error);
   void onString(in List<String> result);
}
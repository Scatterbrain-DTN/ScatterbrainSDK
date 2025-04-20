// DesktopAppCallback.aidl
package net.ballmerlabs.scatterbrainsdk;

import net.ballmerlabs.scatterbrainsdk.internal.SbApp;
import net.ballmerlabs.scatterbrainsdk.DesktopApp;

interface DesktopAppCallback {
   oneway void onError(in String error);
   oneway void onApp(in SbApp result);
   oneway void onDesktopApp(in DesktopApp result);
}
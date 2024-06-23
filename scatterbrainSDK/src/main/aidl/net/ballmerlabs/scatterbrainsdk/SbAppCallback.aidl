package net.ballmerlabs.scatterbrainsdk;
import net.ballmerlabs.scatterbrainsdk.internal.SbApp;

interface SbAppCallback {
   oneway void onError(in String error);
   oneway void onApp(in SbApp result);
}
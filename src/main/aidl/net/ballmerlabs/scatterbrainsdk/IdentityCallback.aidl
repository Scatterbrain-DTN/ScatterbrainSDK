// IdentityCallback.aidl
package net.ballmerlabs.scatterbrainsdk;
import net.ballmerlabs.scatterbrainsdk.Identity;

interface IdentityCallback {
    oneway void onError(in String error);
    oneway void onIdentity(in List<Identity> identity);
}
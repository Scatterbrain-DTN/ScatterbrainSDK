// IdentityCallback.aidl
package net.ballmerlabs.scatterbrainsdk;
import net.ballmerlabs.scatterbrainsdk.Identity;

interface IdentityCallback {
    void onError(in String error);
    void onIdentity(in List<Identity> identity);
}
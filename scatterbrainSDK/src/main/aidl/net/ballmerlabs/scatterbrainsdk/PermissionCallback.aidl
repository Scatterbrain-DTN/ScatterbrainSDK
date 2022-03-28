// IdentityCallback.aidl
package net.ballmerlabs.scatterbrainsdk;
import net.ballmerlabs.scatterbrainsdk.PermissionStatus;

interface PermissionCallback {
    oneway void onError(in String error);
    oneway void onPermission(in PermissionStatus permission);
}
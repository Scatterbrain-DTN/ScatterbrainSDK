// ScatterbrainBinderApi.aidl
package net.ballmerlabs.scatterbrainsdk;
import net.ballmerlabs.scatterbrainsdk.ScatterMessage;
import net.ballmerlabs.scatterbrainsdk.Identity;
import net.ballmerlabs.scatterbrainsdk.UnitCallback;
import net.ballmerlabs.scatterbrainsdk.ScatterMessageCallback;
import net.ballmerlabs.scatterbrainsdk.ByteArrayCallback;
import net.ballmerlabs.scatterbrainsdk.IdentityCallback;
import net.ballmerlabs.scatterbrainsdk.StringCallback;
import net.ballmerlabs.scatterbrainsdk.BoolCallback;

interface ScatterbrainBinderApi {


    // Blocking
    List<ScatterMessage> getByApplication(String application);

    List<ScatterMessage> getByApplicationDate(String application, long startDate, long endDate);

    ScatterMessage getById(long id);

    List<Identity> getIdentities();

    Identity getIdentityByFingerprint(in ParcelUuid fingerprint);

    oneway void sendMessage(in ScatterMessage message);

    oneway void sendAndSignMessage(in ScatterMessage message, in ParcelUuid identity);

    byte[] signDataDetached(in byte[] data, in ParcelUuid identity);

    oneway void sendMessages(in List<ScatterMessage> messages);

    oneway void startDiscovery();

    oneway void stopDiscovery();

    oneway void startPassive();

    oneway void stopPassive();

    List<String> getKnownPackages();

    boolean isDiscovering();

    boolean isPassive();

    oneway void clearDatastore();


    // Nonblocking

    oneway void getKnownPackagesAsync(StringCallback callback);

    oneway void getIdentity(in ParcelUuid fingerprint, IdentityCallback callback);

    oneway void getAppPermissions(in ParcelUuid identity, StringCallback callback);

    oneway void removeIdentity(in ParcelUuid identity, BoolCallback callback);

    oneway void authorizeApp(in ParcelUuid identity, in String packagename, UnitCallback callback);

    oneway void deauthorizeApp(in ParcelUuid identity, in String packagename);

    oneway void generateIdentity(in String name, IdentityCallback callback);

    oneway void ping(UnitCallback callback);

    oneway void signDataDetachedAsync(in byte[] data, in ParcelUuid identity, ByteArrayCallback callback);

    oneway void verifyDataAsync(in byte[] data, in byte[] sig, in ParcelUuid identiy, BoolCallback callback);

    oneway void sendMessagesAsync(in List<ScatterMessage> messages, UnitCallback callback);

    oneway void sendMessageAsync(in ScatterMessage message, UnitCallback callback);

    oneway void sendAndSignMessageAsync(in ScatterMessage message, in ParcelUuid identity, UnitCallback callback);

    oneway void sendAndSignMessagesAsync(in List<ScatterMessage> message, in ParcelUuid identity, UnitCallback callback);

    oneway void getByApplicationAsync(in String application, in ScatterMessageCallback callback);

    oneway void getByApplicationDateAsync(in String application, long startDate, long endDate, ScatterMessageCallback callback);

    oneway void manualRefreshPeers(UnitCallback callback);
}
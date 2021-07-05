// ScatterbrainAPI.aidl
package net.ballmerlabs.scatterbrainsdk;
import net.ballmerlabs.scatterbrainsdk.ScatterMessage;
import net.ballmerlabs.scatterbrainsdk.Identity;

interface ScatterbrainAPI {


    // Blocking
    List<ScatterMessage> getByApplication(String application);

    List<ScatterMessage> getByApplicationDate(String application, long startDate, long endDate);

    ScatterMessage getById(long id);

    List<Identity> getIdentities();

    Identity getIdentityByFingerprint(in ParcelUuid fingerprint);

    void sendMessage(in ScatterMessage message);

    void sendAndSignMessage(in ScatterMessage message, in ParcelUuid identity);

    byte[] signDataDetached(in byte[] data, in ParcelUuid identity);

    void sendMessages(in List<ScatterMessage> messages);

    void startDiscovery();

    void stopDiscovery();

    void startPassive();

    void stopPassive();

    Identity generateIdentity(in String name);

    boolean removeIdentity(in ParcelUuid identity);

    void authorizeApp(in ParcelUuid identity, in String packagename);

    void deauthorizeApp(in ParcelUuid identity, in String packagename);

    String[] getAppPermissions(in ParcelUuid identity);

    List<String> getKnownPackages();

    int getKnownPackagesAsync();

    boolean isDiscovering();

    boolean isPassive();

    void clearDatastore();


    // Nonblocking
    int signDataDetachedAsync(in byte[] data, in ParcelUuid identity);

    int sendMessagesAsync(in List<ScatterMessage> messages);

    int sendMessageAsync(in ScatterMessage message);

    int sendAndSignMessageAsync(in ScatterMessage message, in ParcelUuid identity);

    int sendAndSignMessagesAsync(in List<ScatterMessage> message, in ParcelUuid identity);

    int getByApplicationAsync(String application);

    int getByApplicationDateAsync(String application, long startDate, long endDate);
}
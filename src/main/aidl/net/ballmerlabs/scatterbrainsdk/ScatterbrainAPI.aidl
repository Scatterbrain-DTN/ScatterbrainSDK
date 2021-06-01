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

    Identity getIdentityByFingerprint(in String fingerprint);

    void sendMessage(in ScatterMessage message);

    void sendAndSignMessage(in ScatterMessage message, in String identity);

    byte[] signDataDetached(in byte[] data, in String identity);

    void sendMessages(in List<ScatterMessage> messages);

    void startDiscovery();

    void stopDiscovery();

    void startPassive();

    void stopPassive();

    Identity generateIdentity(in String name);

    boolean removeIdentity(in String identity);

    void authorizeApp(in String identity, in String packagename);

    void deauthorizeApp(in String identity, in String packagename);

    String[] getAppPermissions(in String identity);

    List<String> getKnownPackages();

    int getKnownPackagesAsync();

    boolean isDiscovering();

    boolean isPassive();

    void clearDatastore();


    // Nonblocking
    int signDataDetachedAsync(in byte[] data, in String identity);

    int sendMessagesAsync(in List<ScatterMessage> messages);

    int sendMessageAsync(in ScatterMessage message);

    int sendAndSignMessageAsync(in ScatterMessage message, in String identity);

    int sendAndSignMessagesAsync(in List<ScatterMessage> message, in String identity);

    int getByApplicationAsync(String application);

    int getByApplicationDateAsync(String application, long startDate, long endDate);
}
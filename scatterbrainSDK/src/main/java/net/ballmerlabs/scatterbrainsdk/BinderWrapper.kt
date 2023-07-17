package net.ballmerlabs.scatterbrainsdk

import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.net.Uri
import androidx.lifecycle.LiveData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.util.*

/**
 * Represents a connection to the Scatterbrain API. This class should only be injected by
 * dagger only. By default this class is created as a singleton, as there is little value
 * of concurrent connections to Scatterbrain
 */
interface BinderWrapper {
    /**
     * Starts the scatterbrain router if stopped. Requires
     * net.ballmerlabs.scatterroutingservice.permission.ADMIN permission
     */
    suspend fun startService()

    /**
     * Stops the scatterbrain router if started. Requires
     * net.ballmerlabs.scatterroutingservice.permission.ADMIN permission
     */
    suspend fun stopService()

    /**
     * disconnects from the binder interface
     */
    suspend fun unbindService()

    /**
     * Gets a list of all known identities.
     * requires net.ballmerlabs.scatterroutingservice.permission.ACCESS permission
     * @return list of Identity objects
     */
    @Throws(UnauthorizedException::class)
    suspend fun getIdentities(): List<Identity>

    /**
     * Gets a single identity by fingerprint.
     * requires net.ballmerlabs.scatterroutingservice.permission.ACCESS permission
     * @return identity, null if fingerprint not found
     */
    @Throws(UnauthorizedException::class)
    suspend fun getIdentity(fingerprint: UUID): Identity?

    /**
     * returns a list of all stored message objects for a given application
     * requires net.ballmerlabs.scatterroutingservice.permission.ACCESS permission
     * @param application application identifier
     * @return list of message objects for application
     */
    @Throws(UnauthorizedException::class)
    suspend fun getScatterMessages(application: String): Flow<ScatterMessage>

    /**
     * returns a list of all stored messages for a given application after a given date.
     * requires net.ballmerlabs.scatterroutingservice.permission.ACCESS permission
     * @param application application identifier
     * @param since restrict messages after this date
     * @return list of messages objects
     */
    @Throws(UnauthorizedException::class)
    suspend fun getScatterMessages(application: String, since: Date): Flow<ScatterMessage>

    /**
     * returns a list of all stored messages for a given application between two dates.
     * requires net.ballmerlabs.scatterroutingservice.permission.ACCESS permission
     * @param application application identifier
     * @param start start date
     * @param end end data
     * @return list of message objects
     */
    @Throws(UnauthorizedException::class)
    suspend fun getScatterMessages(application: String, start: Date, end: Date): Flow<ScatterMessage>

    /**
     * returns an asynchronous flow of identities received after this function is called
     * in real time.
     * requires net.ballmerlabs.scatterroutingservice.permission.ACCESS permission
     * @return async flow of identities
     */
    @ExperimentalCoroutinesApi
    @Throws(UnauthorizedException::class)
    fun observeIdentities(): Flow<List<Identity>>

    /**
     * returns an asynchronous flow of all messages received after this functions is called
     * filtered by a given application identifier.
     * requires net.ballmerlabs.scatterroutingservice.permission.ACCESS permission
     * @param application application idenifier
     * @return async flow of messages
     */
    @ExperimentalCoroutinesApi
    @Throws(UnauthorizedException::class)
    fun observeMessages(application: String): Flow<List<ScatterMessage>>

    /**
     * generates and returns a scatterbrain identity with ACLs matching the calling application only
     * If additional applications need access to this identity they can be assigned via the Scatterbrain
     * app. This requires net.ballmerlabs.scatterroutingservice.permission.ACCESS permission
     * @param name human readable name associated with identity
     * @return handle to identity
     */
    @Throws(UnauthorizedException::class)
    suspend fun generateIdentity(name: String): Identity

    /**
     * Gets a list of ACLs associated with a given identity object.
     * This function requires net.ballmerlabs.scatterroutingservice.permission.SUPERUSER permission
     * and is currently not available to 3rd party applications
     * @param identity identity object
     * @return list of ACLs
     */
    @Throws(UnauthorizedException::class)
    suspend fun getPermissions(identity: Identity): List<NamePackage>

    /**
     * Adds an ACL to an identity authorizing an app to use it.
     * This function requires net.ballmerlabs.scatterroutingservice.permission.SUPERUSER and is currently
     * not available to 3rd party applications
     * @param identity identity object
     * @param packageName android app package name
     */
    @Throws(UnauthorizedException::class)
    suspend fun authorizeIdentity(identity: Identity, packageName: String)

    /**
     * Removes an ACL authorizing an app to use an identity.
     * This function requires net.ballmerlabs.scatterroutingservice.permission.SUPERUSER and is currently
     * not available to 3rd party applications
     * @param identity identity object
     * @param packageName android app package name
     */
    @Throws(UnauthorizedException::class)
    suspend fun deauthorizeIdentity(identity: Identity, packageName: String)

    /**
     * Deletes an identity.
     * This function requires net.ballmerlabs.scatterroutingservice.permission.SUPERUSER and is currently
     * not available to 3rd party applications
     * @param identity identity object
     * @return true if identity removed
     */
    @Throws(UnauthorizedException::class)
    suspend fun removeIdentity(identity: Identity): Boolean

    /**
     * Cryptographically signs data using a stored identity.
     * This function requires net.ballmerlabs.scatterroutingservice.permission.ACCESS permission
     * @param identity identity object
     * @param data bytes to sign
     * @return detached ed25519 signature
     */
    @Throws(UnauthorizedException::class)
    suspend fun sign(identity: Identity, data: ByteArray): ByteArray

    /**
     * Cryptographically verifies a detached signature using a stored identity
     * This function requires net.ballmerlabs.scatterroutingservice.permission.ACCESS permission
     * @param identity identity object to verify with
     * @param data data to verify
     * @param sig detached signature generated by sign()
     * @return true if valid, false if invalid
     */
    @Throws(UnauthorizedException::class)
    suspend fun verify(identity: Identity, data: ByteArray, sig: ByteArray): Boolean

    /**
     * Starts active discovery using default transport modules
     * This function requires net.ballmerlabs.scatterroutingservice.permission.ADMIN permission
     */
    @Throws(UnauthorizedException::class)
    suspend fun startDiscover()

    /**
     * Stops active discovery
     * This function requires net.ballmerlabs.scatterroutingservice.permission.ADMIN permission
     */
    @Throws(UnauthorizedException::class)
    suspend fun stopDiscover()

    /**
     * Starts passive discovery using the default radio module. This usually uses less power
     * than active discovery for most transport modules but may skip peers that are also in
     * passive mode
     * This function requires net.ballmerlabs.scatterroutingservice.permission.ADMIN permission
     */
    @Throws(UnauthorizedException::class)
    suspend fun startPassive()

    /**
     * Stops passive discovery
     * This function requires net.ballmerlabs.scatterroutingservice.permission.ADMIN permission
     */
    @Throws(UnauthorizedException::class)
    suspend fun stopPassive()

    /**
     * Enqueues a Scatterbrain message to the datastore. The messages will be sent as soon
     * as a peer is available
     * This function requires net.ballmerlabs.scatterroutingservice.permission.ACCESS permission
     * @param message message to send
     */
    @Throws(UnauthorizedException::class)
    suspend fun sendMessage(message: ScatterMessage)

    /**
     * Enqueues a message to the datastore and signs it with a given identity.
     * This function requires net.ballmerlabs.scatterroutingservice.permission.ACCESS permission
     * but the calling package must have accesss to the identity
     * @param message message to send
     * @param identity identity to sign with
     */
    @Throws(UnauthorizedException::class)
    suspend fun sendMessage(message: ScatterMessage, identity: Identity)

    /**
     * Enqueues a message to the datastore and signs it with a given identity.
     * This function requires net.ballmerlabs.scatterroutingservice.permission.ACCESS permission
     * but the calling package must have accesss to the identity
     * @param message message to send
     * @param identity identity to sign with
     */
    @Throws(UnauthorizedException::class)
    suspend fun sendMessage(message: ScatterMessage, identity: UUID)

    /**
     * Enqueues a list of messages to the datastore and signs it with a given identity.
     * This function requires net.ballmerlabs.scatterroutingservice.permission.ACCESS permission
     * but the calling package must have accesss to the identity
     * @param messages messages to send
     * @param identity identity to sign with
     */
    @Throws(UnauthorizedException::class)
    suspend fun sendMessage(messages: List<ScatterMessage>, identity: UUID)

    /**
     * Enqueues multiple Scatterbrain messages to the datastore. The messages will be sent as soon
     * as a peer is available
     * This function requires net.ballmerlabs.scatterroutingservice.permission.ACCESS permission
     * @param messages message to send
     */
    @Throws(UnauthorizedException::class)
    suspend fun sendMessage(messages: List<ScatterMessage>)

    /**
     * Enqueues a list of messages to the datastore and signs it with a given identity.
     * This function requires net.ballmerlabs.scatterroutingservice.permission.ACCESS permission
     * but the calling package must have accesss to the identity
     * @param messages messages to send
     * @param identity identity to sign with
     */
    @Throws(UnauthorizedException::class)
    suspend fun sendMessage(messages: List<ScatterMessage>, identity: Identity)


    /**
     * Gets the current critical permissions granted to the router. This should be used to
     * prompt the user to open Scatterbrain and grant permissions
     */
    @Throws(UnauthorizedException::class)
    suspend fun getPermissionStatus(): PermissionStatus

    /**
     * Gets a list of packages declaring a Scatterbrain compatible BroadcastReceiver.
     * This function requires net.ballmerlabs.scatterroutingservice.permission.SUPERUSER permission
     * and is currently not available to 3rd party applications
     * @return list of package names
     */
    @Throws(UnauthorizedException::class)
    suspend fun getPackages(): List<NamePackage>


    /**
     * Returns true if the RoutingService is currently discovering
     * @return true if discovering
     */
    @Throws(UnauthorizedException::class)
    suspend fun isDiscovering(): Boolean

    /**
     * Returns a LiveData providing the current connection state
     *
     * @return livedata
     */
    fun observeBinderState(): LiveData<BinderState>

    /**
     * Unregisters the internal BroadcastReceiver for Scatterbrain events.
     * This must be called to use the Scatterbrain SDK
     */
    fun register()

    /**
     * Unregisters Scatterbrain broadcast receivers
     */
    fun unregister()

    /**
     * Attempts a connection to the scatterbrain service
     */
    suspend fun bindService(timeout: Long = 5000L)

    /**
     * Attempts to reconnect to known local peers
     */
    @Throws(UnauthorizedException::class)
    suspend fun rescanPeers()

    /**
     * Observe router state
     */
    fun observeRouterState(): LiveData<RouterState>

    suspend fun dumpDatastore(uri: Uri?)

    /**
     * Checks if this SDK is connected to a running Scatterbrain router
     * @return true if connected
     */
    suspend fun isConnected(): Boolean
    companion object {
        const val TAG = "BinderWrapper"
        const val BIND_ACTION = "net.ballmerlabs.uscatterbrain.ScatterRoutingService.BIND"
        const val BIND_PACKAGE = "net.ballmerlabs.scatterroutingservice"

        enum class BinderState(val message: String) {
            STATE_CONNECTED("Running"),
            STATE_DISCONNECTED("Stopped")
        }
    }
}

/**
 * Pair of application name and metadata
 * @property name application mae
 * @property info application metadata
 * @constructor creates a NamePackage
 */
data class NamePackage(
        val name: String,
        val info: ApplicationInfo,
        private val pm: PackageManager,
        var icon: Drawable? = null
        ) : Comparable<NamePackage> {
    override fun toString(): String {
        return name
    }

    suspend fun loadIcon(): Drawable = withContext(Dispatchers.IO) {
        val i = info.loadIcon(pm)
        icon = i
        i
    }

    override fun compareTo(other: NamePackage): Int {
        return name.compareTo(other.name)
    }
}
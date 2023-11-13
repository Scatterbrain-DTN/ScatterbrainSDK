package net.ballmerlabs.scatterbrainsdk.internal

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.ParcelUuid
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.lifecycle.LiveData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.suspendCancellableCoroutine
import net.ballmerlabs.scatterbrainsdk.*
import net.ballmerlabs.scatterbrainsdk.BinderWrapper.Companion.BIND_ACTION
import net.ballmerlabs.scatterbrainsdk.BinderWrapper.Companion.BIND_PACKAGE
import net.ballmerlabs.scatterbrainsdk.BinderWrapper.Companion.TAG
import java.util.*
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException


@Singleton
class BinderWrapperImpl @Inject constructor(
    val context: Context,
    private val broadcastReceiver: ScatterbrainBroadcastReceiver,
    private val binderProvider: BinderProvider,
    @Named(SCOPE_DEFAULT) private val defaultScope: CoroutineScope
) : BinderWrapper {

    override val coroutineScope: CoroutineScope
        get() = defaultScope

    override suspend fun startService() {
        if (!isConnected()) {
            val startIntent = Intent(BIND_ACTION)
            startIntent.`package` = BIND_PACKAGE

            ContextCompat.startForegroundService(context, startIntent)
        }
    }

    override suspend fun unbindService() {
        binderProvider.unbindService()
    }

    override suspend fun getIdentity(fingerprint: UUID): Identity? {
        val binder = binderProvider.getAsync()

        return callbackFlow {
            binder.getIdentity(ParcelUuid(fingerprint), object : IdentityCallback.Stub() {
                override fun onError(error: String) {
                    cancel(error)
                }

                override fun onIdentity(identity: Identity) {
                    trySend(identity)
                }

                override fun onComplete() {
                    close()
                }
            })

            awaitClose {  }
        }.firstOrNull()
    }


    override suspend fun sign(identity: Identity, data: ByteArray): ByteArray {
        val binder = binderProvider.getAsync()

        return suspendCancellableCoroutine { c ->
            binder.signDataDetachedAsync(
                data,
                ParcelUuid(identity.fingerprint),
                object : ByteArrayCallback.Stub() {
                    override fun onError(error: String) {
                        c.resumeWithException(IllegalStateException(error))
                    }

                    override fun onData(data: ByteArray) {
                        c.resume(data)
                    }

                })
        }

    }

    override fun observeRouterState(): LiveData<RouterState> {
        return broadcastReceiver.observeRouterState()
    }

    override suspend fun isDiscovering(): Boolean {
        val binder = binderProvider.getAsync()
        return binder.isDiscovering
    }

    override suspend fun verify(identity: Identity, data: ByteArray, sig: ByteArray): Boolean {
        val binder = binderProvider.getAsync()

        return suspendCancellableCoroutine { c ->
            binder.verifyDataAsync(
                data,
                sig,
                ParcelUuid(identity.fingerprint),
                object : BoolCallback.Stub() {
                    override fun onError(error: String) {
                        c.resumeWithException(IllegalStateException(error))
                    }

                    override fun onResult(result: Boolean) {
                        c.resume(result)
                    }

                })
        }
    }

    override suspend fun getIdentities(): List<Identity> {
        return binderProvider.getAsync().identities
    }

    override suspend fun bindService(timeout: Long) {
        binderProvider.getAsync(timeout)
    }

    override suspend fun stopService() {
        val stopIntent = Intent(BIND_ACTION)
        stopIntent.`package` = BIND_PACKAGE
        context.stopService(stopIntent)
    }

    @ExperimentalCoroutinesApi
    override fun observeIdentities(): Flow<List<Identity>> = callbackFlow {
        trySend(getIdentities())
        val callback: suspend (handshakeResult: HandshakeResult) -> Unit = { handshakeResult ->
            if (handshakeResult.identities > 0) {
                trySend(getIdentities())
            }
        }
        broadcastReceiver.addOnReceiveCallback(callback)

        awaitClose {
            broadcastReceiver.removeOnReceiveCallback(callback)
        }
    }

    override suspend fun getScatterMessages(application: String): Flow<ScatterMessage> {
        val binder = binderProvider.getAsync()
        return callbackFlow {
            binder.getByApplicationAsync(application, object : ScatterMessageCallback.Stub() {
                override fun onError(error: String) {
                    cancel(error)
                }

                override fun onScatterMessage(message: ScatterMessage) {
                    trySend(message)
                }

                override fun onComplete() {
                    close()
                }

            })

            awaitClose {  }
        }
    }

    override suspend fun rescanPeers() {
        val binder = binderProvider.getAsync()
        return suspendCancellableCoroutine { c ->
            binder.manualRefreshPeers(object : UnitCallback.Stub() {
                override fun onError(error: String) {
                    c.resumeWithException(IllegalStateException(error))
                }

                override fun onComplete() {
                    c.resume(Unit)
                }

            })
        }
    }


    override suspend fun getScatterMessages(
        application: String,
        since: Date
    ): Flow<ScatterMessage> {
        return getScatterMessages(application, since, Date())
    }

    override suspend fun getScatterMessages(
        application: String,
        start: Date,
        end: Date
    ): Flow<ScatterMessage> {
        val binder = binderProvider.getAsync()
        return callbackFlow {
            binder.getByApplicationDateAsync(
                application,
                start.time,
                end.time,
                object : ScatterMessageCallback.Stub() {
                    override fun onError(error: String) {
                        cancel(error)
                    }

                    override fun onScatterMessage(message: ScatterMessage) {
                        trySend(message)
                    }

                    override fun onComplete() {
                        close()
                    }

                })

            awaitClose {  }
        }
    }

    @ExperimentalCoroutinesApi
    override fun observeMessages(application: String): Flow<List<ScatterMessage>> = callbackFlow {
        var now = Date()
        val callback: suspend (handshakeResult: HandshakeResult) -> Unit = { handshakeResult ->
            if (handshakeResult.messages > 0) {
                val messages = getScatterMessages(application, now).toList()
                trySend(messages)
                now = Date()
            }
        }

        broadcastReceiver.addOnReceiveCallback(callback)

        awaitClose { broadcastReceiver.removeOnReceiveCallback(callback) }
    }

    override suspend fun generateIdentity(name: String): Identity {
        val binder = binderProvider.getAsync()
        return callbackFlow {
            binder.generateIdentity(name, object : IdentityCallback.Stub() {
                override fun onError(error: String) {
                    cancel(error)
                }

                override fun onIdentity(identity: Identity) {
                    trySend(identity)
                }

                override fun onComplete() {
                    close()
                }
            })
            awaitClose {  }
        }.firstOrNull()!!
    }

    override suspend fun authorizeIdentity(identity: Identity, packageName: String) {
        val binder = binderProvider.getAsync()
        return suspendCancellableCoroutine { c ->
            binder.authorizeApp(
                ParcelUuid(identity.fingerprint),
                packageName,
                object : UnitCallback.Stub() {
                    override fun onError(error: String) {
                        c.resumeWithException(IllegalStateException(error))
                    }

                    override fun onComplete() {
                        c.resume(Unit)
                    }

                })

        }
    }

    override suspend fun deauthorizeIdentity(identity: Identity, packageName: String) {
        Log.v(TAG, "deauthorizing $packageName")
        binderProvider.getAsync().deauthorizeApp(ParcelUuid(identity.fingerprint), packageName)
    }

    override suspend fun getPermissions(identity: Identity): List<NamePackage> {
        val binder = binderProvider.getAsync()
        return suspendCancellableCoroutine { c ->
            binder.getAppPermissions(
                ParcelUuid(identity.fingerprint),
                object : StringCallback.Stub() {
                    override fun onError(error: String) {
                        c.resumeWithException(IllegalStateException(error))
                    }

                    override fun onString(result: MutableList<String>) {
                        val pm = context.packageManager
                        Log.e(TAG, "retrieved permissions ${result.size}")
                        val packageList = result.map { id ->
                            val r = pm.getApplicationInfo(id, PackageManager.GET_META_DATA)
                            NamePackage(pm.getApplicationLabel(r).toString(), r, pm)
                        }
                        c.resume(packageList)
                    }

                })
        }
    }

    override suspend fun sendMessage(message: ScatterMessage) {
        val binder = binderProvider.getAsync()
        return suspendCancellableCoroutine { c ->
            binder.sendMessageAsync(message, object : UnitCallback.Stub() {
                override fun onError(error: String) {
                    c.resumeWithException(IllegalStateException(error))
                }

                override fun onComplete() {
                    c.resume(Unit)
                }

            })
        }
    }

    override suspend fun sendMessage(messages: List<ScatterMessage>) {
        val binder = binderProvider.getAsync()
        return suspendCancellableCoroutine { c ->
            binder.sendMessagesAsync(messages, object : UnitCallback.Stub() {
                override fun onError(error: String) {
                    c.resumeWithException(IllegalStateException(error))
                }

                override fun onComplete() {
                    c.resume(Unit)
                }

            })
        }
    }

    override suspend fun sendMessage(message: ScatterMessage, identity: UUID) {
        val binder = binderProvider.getAsync()
        return suspendCancellableCoroutine { c ->
            binder.sendAndSignMessageAsync(
                message,
                ParcelUuid(identity),
                object : UnitCallback.Stub() {
                    override fun onError(error: String) {
                        c.resumeWithException(IllegalStateException(error))
                    }

                    override fun onComplete() {
                        c.resume(Unit)
                    }

                })
        }
    }

    override suspend fun sendMessage(messages: List<ScatterMessage>, identity: UUID) {
        val binder = binderProvider.getAsync()
        return suspendCancellableCoroutine { c ->
            binder.sendAndSignMessagesAsync(
                messages,
                ParcelUuid(identity),
                object : UnitCallback.Stub() {
                    override fun onError(error: String) {
                        c.resumeWithException(IllegalStateException(error))
                    }

                    override fun onComplete() {
                        c.resume(Unit)
                    }

                })
        }
    }

    override suspend fun sendMessage(message: ScatterMessage, identity: Identity) {
        val binder = binderProvider.getAsync()
        return suspendCancellableCoroutine { c ->
            binder.sendAndSignMessageAsync(
                message,
                ParcelUuid(identity.fingerprint),
                object : UnitCallback.Stub() {
                    override fun onError(error: String) {
                        c.resumeWithException(IllegalStateException(error))
                    }

                    override fun onComplete() {
                        c.resume(Unit)
                    }

                })
        }
    }

    override suspend fun getPackages(): List<NamePackage> {
        val binder = binderProvider.getAsync()
        val pm = context.packageManager
        return suspendCancellableCoroutine { c ->
            binder.getKnownPackagesAsync(object : StringCallback.Stub() {
                override fun onError(error: String) {
                    c.resumeWithException(IllegalStateException(error))
                }

                override fun onString(result: MutableList<String>) {
                    val res = result.map { id ->
                        val r = pm.getApplicationInfo(id, PackageManager.GET_META_DATA)
                        NamePackage(pm.getApplicationLabel(r).toString(), r, pm)
                    }
                    c.resume(res)
                }

            })
        }
    }

    override suspend fun sendMessage(messages: List<ScatterMessage>, identity: Identity) {
        return sendMessage(messages, identity.fingerprint)
    }

    override suspend fun removeIdentity(identity: Identity): Boolean {
        val binder = binderProvider.getAsync()
        return suspendCancellableCoroutine { c ->
            binder.removeIdentity(ParcelUuid(identity.fingerprint), object : BoolCallback.Stub() {
                override fun onError(error: String) {
                    c.resumeWithException(IllegalStateException(error))
                }

                override fun onResult(result: Boolean) {
                    c.resume(result)
                }

            })
        }
    }


    override suspend fun getPermissionStatus(): PermissionStatus {
        val binder = binderProvider.getAsync()
        return suspendCancellableCoroutine { c ->
            binder.getPermissionsGranted(object : PermissionCallback.Stub() {
                override fun onError(error: String) {
                    c.resumeWithException(IllegalStateException(error))
                }

                override fun onPermission(permission: PermissionStatus) {
                    c.resume(permission)
                }

            })
        }
    }


    override suspend fun dumpDatastore(uri: Uri?) {
        val binder = binderProvider.getAsync()
        if (uri != null) {
            return suspendCancellableCoroutine { c ->
                binder.exportDatabase(uri, object : UnitCallback.Stub() {
                    override fun onError(error: String?) {
                        c.resumeWithException(IllegalStateException(error))
                    }

                    override fun onComplete() {
                        c.resume(Unit)
                    }

                })
            }
        }
    }

    override suspend fun startDiscover() {
        binderProvider.getAsync().startDiscovery()
    }

    override suspend fun startPassive() {
        binderProvider.getAsync().startPassive()
    }

    override suspend fun stopDiscover() {
        binderProvider.getAsync().stopDiscovery()
    }

    override suspend fun stopPassive() {
        binderProvider.getAsync().stopPassive()
    }

    override fun register() {
        broadcastReceiver.register()
    }

    override fun unregister() {
        broadcastReceiver.unregister()
    }

    override suspend fun isConnected(): Boolean {
        return binderProvider.isConnected()
    }

    override fun observeBinderState(): LiveData<BinderWrapper.Companion.BinderState> {
        return binderProvider.getConnectionLivedata()
    }

    init {
        Log.v(TAG, "init called")
    }
}
package net.ballmerlabs.scatterbrainsdk.internal

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.ParcelUuid
import android.os.RemoteException
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.liveData
import androidx.lifecycle.switchMap
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import net.ballmerlabs.scatterbrainsdk.Apps
import net.ballmerlabs.scatterbrainsdk.BinderProvider
import net.ballmerlabs.scatterbrainsdk.BinderWrapper
import net.ballmerlabs.scatterbrainsdk.BinderWrapper.Companion.BIND_ACTION
import net.ballmerlabs.scatterbrainsdk.BinderWrapper.Companion.BIND_PACKAGE
import net.ballmerlabs.scatterbrainsdk.BinderWrapper.Companion.TAG
import net.ballmerlabs.scatterbrainsdk.BoolCallback
import net.ballmerlabs.scatterbrainsdk.ByteArrayCallback
import net.ballmerlabs.scatterbrainsdk.DesktopApp
import net.ballmerlabs.scatterbrainsdk.HandshakeCallback
import net.ballmerlabs.scatterbrainsdk.HandshakeResult
import net.ballmerlabs.scatterbrainsdk.Identity
import net.ballmerlabs.scatterbrainsdk.IdentityCallback
import net.ballmerlabs.scatterbrainsdk.NamePackage
import net.ballmerlabs.scatterbrainsdk.PairingState
import net.ballmerlabs.scatterbrainsdk.PermissionCallback
import net.ballmerlabs.scatterbrainsdk.PermissionStatus
import net.ballmerlabs.scatterbrainsdk.RouterState
import net.ballmerlabs.scatterbrainsdk.SbAppCallback
import net.ballmerlabs.scatterbrainsdk.ScatterMessage
import net.ballmerlabs.scatterbrainsdk.ScatterMessageCallback
import net.ballmerlabs.scatterbrainsdk.ScatterbrainBroadcastReceiver
import net.ballmerlabs.scatterbrainsdk.StringCallback
import net.ballmerlabs.scatterbrainsdk.UnitCallback
import java.util.Date
import java.util.UUID
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
    @Named(SCOPE_DEFAULT) private val defaultScope: CoroutineScope,
) : BinderWrapper {

    private val handlers: Handlers = Handlers().apply {
        net.ballmerlabs.scatterbrainsdk.internal.handlers[this] = true
    }


    override val coroutineScope: CoroutineScope
        get() = defaultScope

    override suspend fun startService() = withContext(Dispatchers.IO) {
        if (!isConnected()) {
            val startIntent = Intent(BIND_ACTION)
            startIntent.`package` = BIND_PACKAGE
            ContextCompat.startForegroundService(context, startIntent)
        }
    }

    override suspend fun unbindService() = withContext(Dispatchers.IO) {
        binderProvider.unbindService()
        Unit
    }

    override fun observeIdentitiesLiveData(): LiveData<ImmutableList<Identity>> {
        return handlers.handshakeResult.switchMap { v -> liveData {
                defaultScope.launch {
                    try {
                        val id = getIdentities().toImmutableList()
                        Log.v("debug", "got identities ${id.size} ${id.filter { v -> v.frozen }.size }}")
                        emit(id)
                    } catch (exc: Exception) {
                        Log.v("debug", "failed to getIdentities: $exc")
                    }
                }
                awaitCancellation()
            } }
    }

    override suspend fun getIdentity(fingerprint: UUID): Identity? = withContext(Dispatchers.IO) {
        val binder = binderProvider.getAsync()

        callbackFlow {
            binder.getIdentity(ParcelUuid(fingerprint), object : IdentityCallback.Stub() {
                override fun onError(error: String) {
                    cancel(error)
                }

                override fun onIdentity(identity: Identity) {
                    trySendBlocking(identity)
                }

                override fun onComplete() {
                    close()
                }
            })

            awaitClose { }
        }.firstOrNull()
    }



    override suspend fun sign(identity: UUID, data: ByteArray): ByteArray {

        return suspendCancellableCoroutine { c ->
            defaultScope.launch(Dispatchers.IO) {
                try {
                    val binder = binderProvider.getAsync()
                    binder.signDataDetachedAsync(
                        data,
                        ParcelUuid(identity),
                        object : ByteArrayCallback.Stub() {
                            override fun onError(error: String) {
                                c.resumeWithException(IllegalStateException(error))
                            }

                            override fun onData(data: ByteArray) {
                                c.resume(data)
                            }

                        })
                } catch (exc: Exception) {
                    c.resumeWithException(exc)
                }
            }
        }

    }

    override fun observeRouterState(): LiveData<RouterState> {
        return handlers.routerState
    }

    override fun observeLuid(): LiveData<ParcelUuid> {
        return handlers.luidState
    }

    override suspend fun isDiscovering(): Boolean = withContext(Dispatchers.IO) {
        val binder = binderProvider.getAsync()
        binder.isDiscovering
    }

    override fun observeMetrics(): LiveData<HandshakeResult> {
        return handlers.handshakeResult
    }

    override suspend fun verify(identity: UUID, data: ByteArray, sig: ByteArray): Boolean {

        return suspendCancellableCoroutine { c ->
            defaultScope.launch(Dispatchers.IO) {
                try {
                    val binder = binderProvider.getAsync()

                    binder.verifyDataAsync(
                        data,
                        sig,
                        ParcelUuid(identity),
                        object : BoolCallback.Stub() {
                            override fun onError(error: String) {
                                c.resumeWithException(IllegalStateException(error))
                            }

                            override fun onResult(result: Boolean) {
                                c.resume(result)
                            }

                        })
                } catch (exc: Exception) {
                    c.resumeWithException(exc)
                }
            }
        }
    }

    override suspend fun randomizeLuid() {
        return suspendCancellableCoroutine { c ->
            defaultScope.launch(Dispatchers.IO) {
                try {
                    val p = binderProvider.getAsync()
                    val callback = object : UnitCallback.Stub() {
                        override fun onError(error: String) {
                            c.resumeWithException(IllegalStateException(error))
                        }

                        override fun onComplete() {
                            c.resume(Unit)
                        }
                    }
                    p.randomizeLuid(callback)
                } catch (exc: Exception) {
                    c.resumeWithException(exc)
                }
            }
        }
    }

    override suspend fun startDesktopApi(name: String) {
        return suspendCancellableCoroutine { c ->
            defaultScope.launch(Dispatchers.IO) {
                try {
                    val p = binderProvider.getAsync()
                    p.startDesktopApi(name, object : UnitCallback.Stub() {
                        override fun onError(error: String) {
                            c.resumeWithException(IllegalStateException(error))
                        }

                        override fun onComplete() {
                            c.resume(Unit)
                        }

                    })
                } catch (exc: Exception) {
                    c.resumeWithException(exc)
                }
            }

        }
    }

    override suspend fun stopDesktopApi() {
        return suspendCancellableCoroutine { c ->
            defaultScope.launch(Dispatchers.IO) {
                try {
                    val p = binderProvider.getAsync()
                    p.stopDesktopApi(object : UnitCallback.Stub() {
                        override fun onError(error: String) {
                            c.resumeWithException(IllegalStateException(error))
                        }

                        override fun onComplete() {
                            c.resume(Unit)
                        }
                    })
                } catch (exc: Exception) {
                    c.resumeWithException(exc)
                }
            }
        }
    }

    override suspend fun getIdentities(): List<Identity> = withContext(Dispatchers.IO) {
        binderProvider.getAsync().identities
    }

    override suspend fun bindService(timeout: Long) = withContext(Dispatchers.IO) {
        binderProvider.getAsync(timeout)
        Unit
    }

    override suspend fun stopService() {
        val stopIntent = Intent(BIND_ACTION)
        stopIntent.`package` = BIND_PACKAGE
        context.stopService(stopIntent)
    }

    @ExperimentalCoroutinesApi
    override fun observeIdentities(): Flow<List<Identity>> = callbackFlow {
        val callback: suspend (handshakeResult: HandshakeResult) -> Unit = { handshakeResult ->
            if (handshakeResult.identities > 0) {
                trySendBlocking(getIdentities())
            }
        }
        handlers.addOnReceiveCallback(callback)

        awaitClose {
            handlers.removeOnReceiveCallback(callback)
        }
    }

    override suspend fun purgeIdentities(purge: Boolean) {
        return suspendCancellableCoroutine { c ->
            defaultScope.launch(Dispatchers.IO) {
                val binder = binderProvider.getAsync()
                try {
                    binder.purgeIdentities(purge, object: UnitCallback.Stub() {
                        override fun onError(error: String?) {
                            c.resumeWithException(IllegalStateException(error))
                        }

                        override fun onComplete() {
                            c.resume(Unit)
                        }

                    })
                } catch (exc: Exception) {
                    c.resumeWithException(exc)
                }
            }
        }
    }

    override suspend fun purgeIdentity(fingerprint: UUID, purge: Boolean) {
        return suspendCancellableCoroutine { c ->
            defaultScope.launch(Dispatchers.IO) {
                val binder = binderProvider.getAsync()
                try {
                    binder.purgeIdentity(ParcelUuid(fingerprint), purge, object: UnitCallback.Stub() {
                        override fun onError(error: String?) {
                            c.resumeWithException(IllegalStateException(error))
                        }

                        override fun onComplete() {
                            c.resume(Unit)
                        }

                    })
                } catch (exc: Exception) {
                    c.resumeWithException(exc)
                }
            }
        }
    }

    override suspend fun purgeMessages(start: Date, end: Date) {
        return suspendCancellableCoroutine { c ->
            defaultScope.launch(Dispatchers.IO) {
                val binder = binderProvider.getAsync()
                try {
                    binder.purge(start.time, end.time, object: UnitCallback.Stub() {
                        override fun onError(error: String?) {
                            c.resumeWithException(IllegalStateException(error))
                        }

                        override fun onComplete() {
                            c.resume(Unit)
                        }

                    })
                } catch (exc: Exception) {
                    c.resumeWithException(exc)
                }
            }
        }
    }

    override suspend fun getScatterMessages(application: String, limit: Int): Flow<ScatterMessage> {
        return callbackFlow {
            defaultScope.launch(Dispatchers.IO) {
                try {
                    val binder = binderProvider.getAsync()
                    binder.getByApplicationAsync(
                        application,
                        limit,
                        object : ScatterMessageCallback.Stub() {
                            override fun onError(error: String) {
                                cancel(error)
                            }

                            override fun onScatterMessage(message: ScatterMessage) {
                                trySendBlocking(message)
                            }

                            override fun onComplete() {
                                close()
                            }

                        })
                } catch (exc: Exception) {
                    error(exc.message ?: "")
                }
            }
            awaitClose { }
        }
    }

    override suspend fun approveDesktopIdentity(handle: UUID, identity: UUID) {
        return suspendCancellableCoroutine { c ->
            defaultScope.launch(Dispatchers.IO) {
                val binder = binderProvider.getAsync()
                try {
                    binder.confirmIdentityImport(ParcelUuid(handle), ParcelUuid(identity), true, object : UnitCallback.Stub() {
                        override fun onError(error: String) {
                            c.resumeWithException(IllegalStateException(error))
                        }

                        override fun onComplete() {
                            c.resume(Unit)
                        }

                    })
                } catch (exc: Exception) {
                    c.resumeWithException(exc)
                }
            }
        }
    }

    override suspend fun rescanPeers() {
        return suspendCancellableCoroutine { c ->
            defaultScope.launch(Dispatchers.IO) {
                val binder = binderProvider.getAsync()
                try {
                    binder.manualRefreshPeers(object : UnitCallback.Stub() {
                        override fun onError(error: String) {
                            c.resumeWithException(IllegalStateException(error))
                        }

                        override fun onComplete() {
                            c.resume(Unit)
                        }

                    })
                } catch (exc: Exception) {
                    c.resumeWithException(exc)
                }
            }
        }
    }


    override suspend fun getScatterMessages(
        application: String,
        since: Date,
        limit: Int,
    ): Flow<ScatterMessage> {
        return getScatterMessages(application, since, Date(), limit)
    }

    override suspend fun getScatterMessages(
        application: String,
        start: Date,
        end: Date,
        limit: Int,
    ): Flow<ScatterMessage> {
        return callbackFlow {
            defaultScope.launch(Dispatchers.IO) {
                try {
                    val binder = binderProvider.getAsync()
                    binder.getByApplicationDateAsync(
                        application,
                        limit,
                        start.time,
                        end.time,
                        object : ScatterMessageCallback.Stub() {
                            override fun onError(error: String) {
                                cancel(error)
                            }

                            override fun onScatterMessage(message: ScatterMessage) {
                                trySendBlocking(message)
                            }

                            override fun onComplete() {
                                close()
                            }

                        })
                } catch (exc: Exception) {
                    error(exc.message ?: "")
                }
            }

            awaitClose { }
        }
    }

    @ExperimentalCoroutinesApi
    override fun observeMessages(application: String, limit: Int): LiveData<List<ScatterMessage>> {
        return liveData {
            defaultScope.launch(Dispatchers.IO) {
                try {
                    val ld = handlers.handshakeResult.switchMap { v ->
                        liveData {
                            withContext(Dispatchers.IO) {
                                if (v.messages > 0) {
                                    val m = getScatterMessages(application, limit).toList()
                                    emit(m)
                                }
                            }
                        }
                    }
                    val messages = getScatterMessages(application, limit).toList()
                    emit(messages)
                    emitSource(ld)

                } catch (exc: Exception) {
                    Log.w(TAG, "exception in observeMessages")
                }
            }
            awaitCancellation()
        }

    }

    override suspend fun generateIdentity(name: String): Identity = withContext(Dispatchers.IO) {
        val binder = binderProvider.getAsync()
        callbackFlow {
            binder.generateIdentity(name, object : IdentityCallback.Stub() {
                override fun onError(error: String) {
                    cancel(error)
                }

                override fun onIdentity(identity: Identity) {
                    trySendBlocking(identity)
                }

                override fun onComplete() {
                    close()
                }
            })
            awaitClose { }
        }.firstOrNull()!!
    }

    override suspend fun authorizeIdentity(identity: UUID, packageName: String) {
        return suspendCancellableCoroutine { c ->
            defaultScope.launch {
                val binder = binderProvider.getAsync()
                try {
                    binder.authorizeApp(
                        ParcelUuid(identity),
                        packageName,
                        object : UnitCallback.Stub() {
                            override fun onError(error: String) {
                                c.resumeWithException(IllegalStateException(error))
                            }

                            override fun onComplete() {
                                c.resume(Unit)
                            }

                        })
                } catch (exc: Exception) {
                    c.resumeWithException(exc)
                }
            }

        }
    }

    override suspend fun deauthorizeIdentity(identity: UUID, packageName: String) =
        withContext(Dispatchers.IO) {
            Log.v(TAG, "deauthorizing $packageName")
            binderProvider.getAsync().deauthorizeApp(ParcelUuid(identity), packageName)
        }

    override suspend fun getPermissions(identity: UUID): List<NamePackage> {
        val binder = binderProvider.getAsync()
        return suspendCancellableCoroutine { c ->
            defaultScope.launch(Dispatchers.IO) {
                try {
                    binder.getAppPermissions(
                        ParcelUuid(identity),
                        object : StringCallback.Stub() {
                            override fun onError(error: String) {
                                c.resumeWithException(IllegalStateException(error))
                            }

                            override fun onString(result: MutableList<String>) {
                                val pm = context.packageManager
                                Log.e(TAG, "retrieved permissions ${result.size}")
                                val packageList = result.filter { p -> p != context.packageName }
                                    .map { id ->
                                        val r =
                                            pm.getApplicationInfo(id, PackageManager.GET_META_DATA)
                                        NamePackage(pm.getApplicationLabel(r).toString(), r, pm)
                                    }
                                c.resume(packageList)
                            }

                        })
                } catch (exc: Exception) {
                    c.resumeWithException(exc)
                }
            }
        }
    }

    override suspend fun sendMessage(message: ScatterMessage) {
        return suspendCancellableCoroutine { c ->
            defaultScope.launch(Dispatchers.IO) {
                val binder = binderProvider.getAsync()
                try {
                    binder.sendMessageAsync(message, object : UnitCallback.Stub() {
                        override fun onError(error: String) {
                            c.resumeWithException(IllegalStateException(error))
                        }

                        override fun onComplete() {
                            c.resume(Unit)
                        }

                    })
                } catch (exc: Exception) {
                    c.resumeWithException(exc)
                }
            }
        }
    }

    override suspend fun getMetrics(): HandshakeResult {
        return suspendCancellableCoroutine { c ->
            defaultScope.launch(Dispatchers.IO) {
                val binder = binderProvider.getAsync()
                try {
                    binder.getMetrics(object : HandshakeCallback.Stub() {
                        override fun onError(error: String) {
                            c.resumeWithException(IllegalStateException(error))
                        }

                        override fun onResult(identity: HandshakeResult) {
                            handlers.handshakeResult.postValue(identity)
                            c.resume(identity)
                        }
                    })
                } catch (exc: Exception) {
                    c.resumeWithException(exc)
                }
            }
        }
    }

    override suspend fun sendMessage(messages: List<ScatterMessage>) {
        return suspendCancellableCoroutine { c ->
            defaultScope.launch(Dispatchers.IO) {
                val binder = binderProvider.getAsync()
                try {
                    binder.sendMessagesAsync(messages, object : UnitCallback.Stub() {
                        override fun onError(error: String) {
                            c.resumeWithException(IllegalStateException(error))
                        }

                        override fun onComplete() {
                            c.resume(Unit)
                        }

                    })
                } catch (exc: Exception) {
                    c.resumeWithException(exc)
                }
            }
        }
    }

    override suspend fun sendMessage(message: ScatterMessage, identity: UUID) {
        return suspendCancellableCoroutine { c ->
            defaultScope.launch(Dispatchers.IO) {
                val binder = binderProvider.getAsync()
                try {
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
                } catch (exc: Exception) {
                    c.resumeWithException(exc)
                }
            }
        }
    }

    override suspend fun sendMessage(messages: List<ScatterMessage>, identity: UUID) {
        val binder = binderProvider.getAsync()
        return suspendCancellableCoroutine { c ->
            defaultScope.launch(Dispatchers.IO) {
                try {
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
                } catch (exc: Exception) {
                    c.resumeWithException(exc)
                }
            }
        }
    }

    override suspend fun sendMessage(message: ScatterMessage, identity: Identity) {
        return suspendCancellableCoroutine { c ->
            defaultScope.launch(Dispatchers.IO) {
                val binder = binderProvider.getAsync()
                try {
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
                } catch (exc: Exception) {
                    c.resumeWithException(exc)
                }
            }
        }
    }

    override suspend fun getPackages(): List<NamePackage> {
        return suspendCancellableCoroutine { c ->
            defaultScope.launch(Dispatchers.IO) {
                val binder = binderProvider.getAsync()
                val pm = context.packageManager
                try {
                    binder.getKnownPackagesAsync(object : StringCallback.Stub() {
                        override fun onError(error: String) {
                            c.resumeWithException(IllegalStateException(error))
                        }

                        override fun onString(result: MutableList<String>) {
                            Log.v("debug", "getPackages ${result.size}")
                            val res = result
                                .filter { id -> id != context.packageName }
                                .map { id ->
                                    val r = pm.getApplicationInfo(id, PackageManager.GET_META_DATA)
                                    NamePackage(pm.getApplicationLabel(r).toString(), r, pm)
                                }
                            Log.v("debug", "getPackages map ${res.size}")

                            c.resume(res)
                        }
                    })
                } catch (exc: Exception) {
                    c.resumeWithException(exc)
                }
            }
        }
    }

    override suspend fun sendMessage(messages: List<ScatterMessage>, identity: Identity) {
        return sendMessage(messages, identity.fingerprint)
    }

    override suspend fun removeIdentity(identity: UUID): Boolean {
        return suspendCancellableCoroutine { c ->
            defaultScope.launch(Dispatchers.IO) {
                val binder = binderProvider.getAsync()
                try {
                    binder.removeIdentity(
                        ParcelUuid(identity),
                        object : BoolCallback.Stub() {
                            override fun onError(error: String) {
                                c.resumeWithException(IllegalStateException(error))
                            }

                            override fun onResult(result: Boolean) {
                                c.resume(result)
                            }

                        })
                } catch (exc: Exception) {
                    c.resumeWithException(exc)
                }
            }
        }
    }


    override suspend fun syncMeshtastic() {
        return suspendCancellableCoroutine { c ->
            defaultScope.launch(Dispatchers.IO) {
                val binder = binderProvider.getAsync()
                try {
                    binder.meshtasticSync(object : UnitCallback.Stub() {
                        override fun onError(error: String) {
                            c.resumeWithException(RemoteException(error))
                        }

                        override fun onComplete() {
                            c.resume(Unit)
                        }

                    })
                } catch (exc: Exception) {
                    c.resumeWithException(exc)
                }
            }
        }
    }


    override suspend fun startMeshtastic(): Boolean {
        return suspendCancellableCoroutine { c ->
            defaultScope.launch(Dispatchers.IO) {
                val binder = binderProvider.getAsync()
                try {
                    binder.connectMeshtastic(object: BoolCallback.Stub() {
                        override fun onError(error: String) {
                            c.resumeWithException(IllegalStateException(error))
                        }

                        override fun onResult(result: Boolean) {
                            c.resume(result)
                        }
                    })
                } catch (exc: Exception) {
                    c.resumeWithException(exc)
                }
            }
        }
    }


    override suspend fun getPermissionStatus(): PermissionStatus {
        return suspendCancellableCoroutine { c ->
            defaultScope.launch(Dispatchers.IO) {
                val binder = binderProvider.getAsync()
                try {
                    binder.getPermissionsGranted(object : PermissionCallback.Stub() {
                        override fun onError(error: String) {
                            c.resumeWithException(IllegalStateException(error))
                        }

                        override fun onPermission(permission: PermissionStatus) {
                            c.resume(permission)
                        }

                    })
                } catch (exc: Exception) {
                    c.resumeWithException(exc)
                }
            }
        }
    }


    override suspend fun authorizeDesktop(fingerprint: ByteArray, authorize: Boolean) {
        return suspendCancellableCoroutine { c ->
            defaultScope.launch(Dispatchers.IO) {
                val binder = binderProvider.getAsync()
                try {
                    binder.respondPairing(fingerprint, authorize, object : UnitCallback.Stub() {
                        override fun onError(error: String) {
                            c.resumeWithException(IllegalStateException(error))
                        }

                        override fun onComplete() {
                            c.resume(Unit)
                        }

                    })
                } catch (exc: Exception) {
                    c.resumeWithException(exc)
                }
            }
        }
    }

    override suspend fun getApps(): Apps {
        return suspendCancellableCoroutine { c ->

            defaultScope.launch(Dispatchers.IO) {
                val binder = binderProvider.getAsync()
                try {
                    val res = Apps()
                    binder.onAppCallback(object : SbAppCallback.Stub() {
                        override fun onError(error: String) {
                            c.resumeWithException(IllegalStateException(error))
                        }

                        override fun onApp(result: SbApp?) {
                            when(result) {
                                null -> c.resume(res)
                                else -> res.mobile.add(result)
                            }
                        }

                        override fun onDesktopApp(result: DesktopApp?) {
                            when(result) {
                                null -> c.resume(res)
                                else -> res.desktop.add(result)
                            }
                        }

                    })
                } catch (exc: Exception) {
                    c.resumeWithException(exc)
                }
            }
        }
    }


    override suspend fun deleteAndroidApp(id: String) {
        return suspendCancellableCoroutine { c ->
            defaultScope.launch(Dispatchers.IO) {
                val binder = binderProvider.getAsync()
                try {
                    binder.removeApp(id, object : UnitCallback.Stub() {
                        override fun onError(error: String) {
                            c.resumeWithException(IllegalStateException(error))
                        }

                        override fun onComplete() {
                            c.resume(Unit)
                        }

                    })
                } catch (exc: Exception) {
                    c.resumeWithException(exc)
                }
            }
        }
    }


    override suspend fun deleteDesktopApp(publicKey: ByteArray) {
        return suspendCancellableCoroutine { c ->
            defaultScope.launch(Dispatchers.IO) {
                val binder = binderProvider.getAsync()
                try {
                    binder.removeDesktopApp(publicKey, object : UnitCallback.Stub() {
                        override fun onError(error: String) {
                            c.resumeWithException(IllegalStateException(error))
                        }

                        override fun onComplete() {
                            c.resume(Unit)
                        }

                    })
                } catch (exc: Exception) {
                    c.resumeWithException(exc)
                }
            }
        }
    }

    override suspend fun dumpDatastore(uri: Uri?) {
        if (uri != null) {
            return suspendCancellableCoroutine { c ->
                defaultScope.launch(Dispatchers.IO) {
                    val binder = binderProvider.getAsync()
                    try {
                        binder.exportDatabase(uri, object : UnitCallback.Stub() {
                            override fun onError(error: String?) {
                                c.resumeWithException(IllegalStateException(error))
                            }

                            override fun onComplete() {
                                c.resume(Unit)
                            }

                        })
                    } catch (exc: Exception) {
                        c.resumeWithException(exc)
                    }
                }
            }
        }
    }

    override suspend fun startDiscover() = withContext(Dispatchers.IO) {
        binderProvider.getAsync().startDiscovery()
    }

    override suspend fun startPassive() = withContext(Dispatchers.IO) {
        binderProvider.getAsync().startPassive()
    }

    override suspend fun stopDiscover() = withContext(Dispatchers.IO) {
        binderProvider.getAsync().stopDiscovery()
    }

    override suspend fun stopPassive() = withContext(Dispatchers.IO) {
        binderProvider.getAsync().stopPassive()
    }

    override fun register() {
        broadcastReceiver.register()
    }

    override fun unregister() {
        broadcastReceiver.unregister()
    }

    override suspend fun isConnected(): Boolean = withContext(Dispatchers.IO) {
        binderProvider.isConnected()
    }

    override fun observePairingAttempts(): LiveData<PairingState> {
        return handlers.desktopPairing
    }

    override fun observeBinderState(): LiveData<BinderWrapper.Companion.BinderState> {
        return binderProvider.getConnectionLivedata()
    }

    protected fun finalize() {
        try {
            net.ballmerlabs.scatterbrainsdk.internal.handlers.remove(this.handlers)
        } catch(exc: ConcurrentModificationException) {
            Log.w(TAG, "failed to remove handler: $exc")
        }
    }

    init {
        Log.v(TAG, "init called")
    }
}
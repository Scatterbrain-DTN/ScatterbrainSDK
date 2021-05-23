package net.ballmerlabs.scatterbrainsdk.internal
import android.app.ActivityManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.IBinder
import android.os.RemoteException
import android.util.Log
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.yield
import net.ballmerlabs.scatterbrainsdk.*
import net.ballmerlabs.scatterbrainsdk.BinderWrapper.Companion.BIND_ACTION
import net.ballmerlabs.scatterbrainsdk.BinderWrapper.Companion.BIND_PACKAGE
import net.ballmerlabs.scatterbrainsdk.BinderWrapper.Companion.TAG
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine


@ExperimentalCoroutinesApi
@Singleton
class BinderWrapperImpl @Inject constructor(
        val context: Context,
        private val broadcastReceiver: ScatterbrainBroadcastReceiverImpl
) : BinderWrapper  {

    private var binder: ScatterbrainAPI? = null
    private val bindCallbackSet: MutableSet<(Boolean?) -> Unit> = mutableSetOf()
    private val pm = context.packageManager

    private val callback = object: ServiceConnection {
        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            binder = ScatterbrainAPI.Stub.asInterface(service)
            Log.v(TAG, "connected to ScatterRoutingService binder")
            try {
                bindCallbackSet.forEach { c ->  c(true)}
            } catch (e: RemoteException) {
                Log.e(TAG, "RemoteException: $e")
                bindCallbackSet.forEach { c -> c(null) }
            } finally {
                bindCallbackSet.clear()
            }
        }

        override fun onServiceDisconnected(componentName: ComponentName) {
            Log.v(TAG, "onservicedisconnected")
            binder = null
            bindCallbackSet.forEach { c -> c(false) }
            bindCallbackSet.clear()
        }
    }

    private fun registerCallback(c: (Boolean?) -> Unit) {
        bindCallbackSet.add(c)
    }

    private fun unregisterCallback(c: (Boolean?) -> Unit) {
        bindCallbackSet.remove(c)
    }

    private suspend fun bindServiceWithoutTimeout(): Unit = suspendCoroutine { ret ->
        if (binder == null) {
            registerCallback { b ->
                if (b == null || b == false) throw IllegalStateException("failed to bind service")
                ret.resume(Unit)
            }
            val bindIntent = Intent(BIND_ACTION)
            bindIntent.`package` = BIND_PACKAGE
            context.bindService(bindIntent, callback, 0)
        } else {
            ret.resume(Unit)
        }
    }

    override suspend fun startService() {
        val startIntent = Intent(BIND_ACTION)
        startIntent.`package` = BIND_PACKAGE
        context.startForegroundService(startIntent)
        bindService()
    }

    override suspend fun bindService() {
        withTimeout(5000L) {
            bindServiceWithoutTimeout()
        }
    }

    override suspend fun sign(identity: Identity, data: ByteArray): ByteArray {
        bindService()
        return binder!!.signDataDetached(data, identity.fingerprint)
    }

    override suspend fun unbindService(): Boolean = suspendCoroutine { ret ->
        try {
            if (binder != null) {
                context.unbindService(callback)
            }
            ret.resume(true)
        } catch (e: IllegalArgumentException) {
            ret.resume(true) //service already unbound
        }
    }

    override suspend fun getIdentities(): List<Identity> {
        bindService()
        return binder!!.identities
    }

    override suspend fun stopService() {
        val stopIntent = Intent(BIND_ACTION)
        stopIntent.`package` = BIND_PACKAGE
        context.stopService(stopIntent)
    }

    @ExperimentalCoroutinesApi
    override suspend fun observeIdentities(): Flow<List<Identity>>  = callbackFlow {
        bindService()
        offer(getIdentities())
        val callback: suspend (handshakeResult: HandshakeResult) -> Unit = { handshakeResult ->
            if (handshakeResult.identities > 0) {
                offer(getIdentities())
            }
        }
        broadcastReceiver.addOnReceiveCallback(callback)

        awaitClose {
            broadcastReceiver.removeOnReceiveCallback(callback)
        }
    }

    override suspend fun getScatterMessages(application: String): List<ScatterMessage> {
        bindService()
        return binder!!.getByApplication(application)
    }

    @ExperimentalCoroutinesApi
    override suspend fun observeMessages(application: String): Flow<List<ScatterMessage>> = callbackFlow  {
        bindService()
        offer(getScatterMessages(application))
        val callback: suspend (handshakeResult: HandshakeResult) -> Unit = { handshakeResult ->
            if (handshakeResult.messages > 0) {
                offer(getScatterMessages(application))
            }
        }

        broadcastReceiver.addOnReceiveCallback(callback)

        awaitClose {
            broadcastReceiver.removeOnReceiveCallback(callback)
        }
    }

    override suspend fun generateIdentity(name: String): String? {
        bindService()
        return try {
            binder!!.generateIdentity(name)
            null
        } catch (re: RemoteException) {
            Log.e(TAG, "remoteException")
            re.printStackTrace()
            re.localizedMessage
        }
    }

    override suspend fun authorizeIdentity(identity: Identity, packageName: String) {
        bindService()
        binder!!.authorizeApp(identity.fingerprint, packageName)
    }

    override suspend fun deauthorizeIdentity(identity: Identity, packageName: String) {
        bindService()
        Log.v(TAG, "deauthorizing $packageName")
        binder!!.deauthorizeApp(identity.fingerprint, packageName)
    }

    override suspend fun getPermissions(identity: Identity): Flow<List<NamePackage>> = flow {
        bindService()
        val identities = binder!!.getAppPermissions(identity.fingerprint)
        val result = mutableListOf<NamePackage>()
        val pm = context.packageManager
        for (id in identities) {
            yield()
            val r = pm.getApplicationInfo(id, PackageManager.GET_META_DATA)
            result.add(NamePackage(pm.getApplicationLabel(r).toString(), r))
        }
        emit(result)
    }

    override suspend fun removeIdentity(identity: Identity): Boolean {
        bindService()
        return binder!!.removeIdentity(identity.fingerprint)
    }

    override suspend fun startDiscover() {
        bindService()
        binder!!.startDiscovery()
    }

    override suspend fun startPassive() {
        bindService()
        binder!!.startPassive()
    }

    override suspend fun stopDiscover() {
        bindService()
        binder!!.stopDiscovery()
    }

    override suspend fun stopPassive() {
        bindService()
        binder!!.stopPassive()
    }

    override fun isConnected(): Boolean {
        val manager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        for (service in manager.getRunningServices(1)) {
            if ("net.ballmerlabs.uscatterbrain.ScatterRoutingService" == service.service.className) {
                return true
            }
        }
        return false
    }

    init {
        Log.v(TAG, "init called")
    }
}
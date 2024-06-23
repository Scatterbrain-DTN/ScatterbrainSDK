package net.ballmerlabs.scatterbrainsdk

import android.os.ParcelUuid
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

interface ScatterbrainBroadcastReceiver {
    fun register()
    fun unregister()

    companion object {
        const val EXTRA_PAIRING_STATE = "pairing-state"
    }
}
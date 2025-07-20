package net.ballmerlabs.scatterbrainsdk

interface ScatterbrainBroadcastReceiver {
    fun register()
    fun unregister()

    companion object {
        const val EXTRA_PAIRING_STATE = "pairing-state"
    }
}
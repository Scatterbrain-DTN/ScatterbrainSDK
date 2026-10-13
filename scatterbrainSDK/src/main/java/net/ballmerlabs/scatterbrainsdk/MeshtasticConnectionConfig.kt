package net.ballmerlabs.scatterbrainsdk

import android.bluetooth.BluetoothDevice
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class MeshtasticConnectionConfig(
    val mode: MeshtasticMode,
    val connection: String
): Parcelable
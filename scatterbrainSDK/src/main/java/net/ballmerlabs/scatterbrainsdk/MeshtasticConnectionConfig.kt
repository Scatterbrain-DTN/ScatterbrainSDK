package net.ballmerlabs.scatterbrainsdk

import android.bluetooth.BluetoothDevice
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class MeshtasticConnectionConfig(
    val mode: MeshtasticMode,
    val host: String? = null,
    val port: Int? = null,
    val device: BluetoothDevice? = null
): Parcelable
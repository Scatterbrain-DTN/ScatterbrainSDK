package net.ballmerlabs.scatterbrainsdk

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ForwardMeshtastic(
    val channel: Int? = null
) : Parcelable
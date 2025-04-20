package net.ballmerlabs.scatterbrainsdk

import net.ballmerlabs.scatterbrainsdk.internal.SbApp

data class Apps(
    val desktop: MutableList<DesktopApp> = mutableListOf(),
    val mobile: MutableList<SbApp> = mutableListOf()
)
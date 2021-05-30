package net.ballmerlabs.scatterbrainsdk.internal

import android.content.Context
import dagger.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import net.ballmerlabs.scatterbrainsdk.BinderWrapper
import net.ballmerlabs.scatterbrainsdk.ScatterbrainBroadcastReceiver
import javax.inject.Named
import javax.inject.Singleton

@Singleton
@Component(modules = [SdkComponent.SdkModule::class])
interface SdkComponent {

    @Component.Builder
    interface Builder {
        @BindsInstance
        fun applicationContext(context: Context): Builder?
        fun build(): SdkComponent?
    }

    @Module
    abstract class SdkModule {
        @ExperimentalCoroutinesApi
        @Binds
        @Singleton
        abstract fun bindBinderWrapper(binderWrapperImpl: BinderWrapperImpl): BinderWrapper

        @Binds
        @Singleton
        abstract fun bindScatterbrainBroadcastReceiver(
                broadcastReceiver: ScatterbrainBroadcastReceiverImpl
        ) : ScatterbrainBroadcastReceiver

        @Module
        companion object {
            @Provides
            @Singleton
            @Named("defaultScope")
            fun providesCorutineScope(context: Context): CoroutineScope {
                return CoroutineScope(Dispatchers.Default)
            }
        }
    }

    fun sdk(): BinderWrapper

    fun broadcastReceiver(): ScatterbrainBroadcastReceiver
}
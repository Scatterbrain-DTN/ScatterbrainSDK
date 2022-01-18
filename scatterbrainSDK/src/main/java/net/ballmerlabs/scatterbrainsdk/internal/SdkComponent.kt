package net.ballmerlabs.scatterbrainsdk.internal

import android.content.Context
import dagger.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import net.ballmerlabs.scatterbrainsdk.BinderProvider
import net.ballmerlabs.scatterbrainsdk.BinderWrapper
import net.ballmerlabs.scatterbrainsdk.ScatterbrainBroadcastReceiver
import javax.inject.Named
import javax.inject.Singleton

const val SCOPE_DEFAULT = "defaultScope"

@Singleton
@Component(modules = [SdkComponent.SdkModule::class])
internal interface SdkComponent {

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


        @Binds
        @Singleton
        abstract fun bindBinderProvider(
                binderProvider: BinderProviderImpl
        ): BinderProvider

        @Module
        companion object {
            @Provides
            @Singleton
            @Named(SCOPE_DEFAULT)
            fun providesCorutineScope(): CoroutineScope {
                return CoroutineScope(Dispatchers.Default)
            }
        }
    }

    fun sdk(): BinderWrapper

    fun broadcastReceiver(): ScatterbrainBroadcastReceiver

    fun binderProvider(): BinderProvider
}
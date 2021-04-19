package net.ballmerlabs.scatterbrainsdk

import android.content.Context
import dagger.Binds
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
    }

    fun sdk(): BinderWrapper

    fun broadcastReceiver(): ScatterbrainBroadcastReceiver
}
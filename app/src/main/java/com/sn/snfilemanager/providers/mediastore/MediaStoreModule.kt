package com.sn.snfilemanager.providers.mediastore

import android.content.Context
import com.sn.mediastorepv.MediaStoreBuilder
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object MediaStoreModule {
    @Provides
    @Singleton
    fun provideMediaStoreBuilder(
        @ApplicationContext context: Context,
    ): MediaStoreBuilder {
        return MediaStoreBuilder(context)
    }
}

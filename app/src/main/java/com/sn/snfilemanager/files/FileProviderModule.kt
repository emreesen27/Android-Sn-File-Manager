package com.sn.snfilemanager.files

import android.app.Application
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object FileProviderModule {

    @Provides
    @Singleton
    fun provideFileProvider(application: Application): FilePathProvider {
        return FilePathProvider(application)
    }
}
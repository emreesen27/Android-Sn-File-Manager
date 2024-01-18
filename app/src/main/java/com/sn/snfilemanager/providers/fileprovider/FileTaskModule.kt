package com.sn.snfilemanager.providers.fileprovider

import com.sn.filetaskpv.FileTask
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object FileTaskModule {
    @Singleton
    @Provides
    fun provideFileTask(): FileTask {
        return FileTask()
    }
}
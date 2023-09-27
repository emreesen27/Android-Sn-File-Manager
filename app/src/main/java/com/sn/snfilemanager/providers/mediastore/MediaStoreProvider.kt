package com.sn.snfilemanager.providers.mediastore

import com.sn.mediastorepv.MediaStoreBuilder
import com.sn.mediastorepv.data.Media
import com.sn.mediastorepv.data.MediaType
import com.sn.snfilemanager.core.base.BaseResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class MediaStoreProvider @Inject constructor(private val mediaStoreBuilder: MediaStoreBuilder) {

    suspend fun getMedia(
        mediaType: MediaType,
        ext: List<String>?
    ): BaseResult<MutableList<MediaFile>> {
        return try {
            withContext(Dispatchers.IO) {
                val result =
                    mediaStoreBuilder.setExtCheck(ext).build().getMedia(mediaType)
                        .map { it.toMediaFile() }
                        .toMutableList()
                BaseResult.Success(result)
            }
        } catch (e: Exception) {
            BaseResult.Failure(e)
        }
    }

    suspend fun deleteMedia(medias: List<Media>): BaseResult<Boolean> {
        return try {
            withContext(Dispatchers.IO) {
                val result = mediaStoreBuilder.build().deleteMedia(medias)
                BaseResult.Success(result)
            }
        } catch (e: Exception) {
            BaseResult.Failure(e)
        }
    }

    suspend fun moveMedia(sourceMedias: List<Media>, destinationPath: String): BaseResult<MutableList<Pair<String, String>>? > {
        return try {
            withContext(Dispatchers.IO) {
                val result = mediaStoreBuilder.build().moveMedia(sourceMedias, destinationPath)
                BaseResult.Success(result)
            }
        } catch (e: Exception) {
            BaseResult.Failure(e)
        }
    }
}
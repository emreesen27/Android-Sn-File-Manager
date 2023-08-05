package com.sn.snfilemanager.providers.mediastore

import com.sn.mediastorepv.Media
import com.sn.mediastorepv.MediaStoreBuilder
import com.sn.mediastorepv.MediaType
import com.sn.snfilemanager.core.base.BaseResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class MediaStoreProvider @Inject constructor(private val mediaStoreBuilder: MediaStoreBuilder) {

    private val mediaStoreRepository = mediaStoreBuilder.build()

    suspend fun getMedia(mediaType: MediaType): BaseResult<MutableList<MediaFile>> {
        return try {
            withContext(Dispatchers.IO) {
                val result = mediaStoreRepository.getMedia(mediaType).map { it.toMediaFile() }
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
                val result = mediaStoreRepository.deleteMedia(medias)
                BaseResult.Success(result)
            }
        } catch (e: Exception) {
            BaseResult.Failure(e)
        }
    }


}
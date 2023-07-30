package com.sn.snfilemanager.providers.mediastore

import com.sn.snfilemanager.core.base.BaseResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class MediaStoreProvider @Inject constructor(private val mediaRepository: MediaRepository) {

    suspend fun getMedia(mediaType: MediaType): BaseResult<MutableList<MediaFile>> {
        return try {
            withContext(Dispatchers.IO) {
                val result = mediaRepository.getMedia(mediaType)
                BaseResult.Success(result)
            }
        } catch (e: Exception) {
            BaseResult.Failure(e)
        }
    }

    suspend fun deleteMedia(medias: List<MediaFile>): BaseResult<Boolean> {
        return try {
            withContext(Dispatchers.IO) {
                val result = mediaRepository.deleteMedia(medias)
                BaseResult.Success(result)
            }
        } catch (e: Exception) {
            BaseResult.Failure(e)
        }
    }


}
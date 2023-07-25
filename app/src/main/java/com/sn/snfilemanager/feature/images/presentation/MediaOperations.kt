package com.sn.snfilemanager.feature.images.presentation

import android.net.Uri
import com.sn.snfilemanager.core.BaseResult
import com.sn.snfilemanager.media.MediaFile
import com.sn.snfilemanager.media.MediaRepository
import com.sn.snfilemanager.media.MediaType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class MediaOperations @Inject constructor(private val mediaRepository: MediaRepository) {

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
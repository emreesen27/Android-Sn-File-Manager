package com.sn.snfilemanager.feature.images.domain

import com.sn.snfilemanager.core.BaseUseCase
import com.sn.snfilemanager.core.BaseResult
import com.sn.snfilemanager.media.MediaFile
import com.sn.snfilemanager.media.MediaRepository
import com.sn.snfilemanager.media.MediaType
import javax.inject.Inject

class GetImages @Inject constructor(private val mediaRepository: MediaRepository) :
    BaseUseCase<MediaType, MutableList<MediaFile>>() {

    override suspend fun execute(params: MediaType): BaseResult<MutableList<MediaFile>> {
        return try {
            val result = mediaRepository.getMedia(params)
            BaseResult.Success(result)
        } catch (e: Exception) {
            BaseResult.Failure(e)
        }
    }
}

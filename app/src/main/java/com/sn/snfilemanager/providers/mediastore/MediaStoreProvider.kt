package com.sn.snfilemanager.providers.mediastore

import com.sn.mediastorepv.MediaStoreBuilder
import com.sn.mediastorepv.data.Media
import com.sn.mediastorepv.data.MediaSelectionData
import com.sn.mediastorepv.data.MediaType
import com.sn.snfilemanager.core.base.BaseResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class MediaStoreProvider
    @Inject
    constructor(private val mediaStoreBuilder: MediaStoreBuilder) {
        suspend fun getMedia(
            mediaType: MediaType,
            ext: List<String>?,
            order: String?,
        ): BaseResult<MutableList<Media>> {
            return try {
                withContext(Dispatchers.IO) {
                    val result =
                        mediaStoreBuilder
                            .setMediaSelectionData(MediaSelectionData(sortOrder = order))
                            .setExtCheck(ext).build().getMedia(mediaType)
                    BaseResult.Success(result)
                }
            } catch (e: Exception) {
                BaseResult.Failure(e)
            }
        }

        suspend fun searchInPath(
            fileName: String,
            path: String,
            vararg mediaType: MediaType,
        ): BaseResult<MutableList<String>> {
            return try {
                withContext(Dispatchers.IO) {
                    val result = mediaStoreBuilder.build().searchInPath(fileName, path, *mediaType)
                    BaseResult.Success(result)
                }
            } catch (e: Exception) {
                BaseResult.Failure(e)
            }
        }

        suspend fun renameMedia(
            media: Media,
            newName: String,
        ): BaseResult<Media> {
            return try {
                withContext(Dispatchers.IO) {
                    val updatedMedia = mediaStoreBuilder.build().renameMedia(media, newName)
                    if (updatedMedia != null) {
                        BaseResult.Success(updatedMedia)
                    } else {
                        BaseResult.Failure(Exception("Media renaming failed"))
                    }
                }
            } catch (e: Exception) {
                BaseResult.Failure(e)
            }
        }
    }

package com.sn.snfilemanager.providers.mediastore

import android.content.ContentUris
import android.content.Context
import android.provider.MediaStore
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MediaRepository @Inject constructor(@ApplicationContext private val context: Context) {

    fun getMedia(mediaType: MediaType): MutableList<MediaFile> {
        val selection = null
        val selectionArgs = null
        val sortOrder = "${MediaStore.MediaColumns.DATE_ADDED} DESC"

        val mediaList = mutableListOf<MediaFile>()

        context.contentResolver.query(
            mediaType.uri,
            mediaType.projection,
            selection,
            selectionArgs,
            sortOrder
        )?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns._ID)
            val nameColumn = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DISPLAY_NAME)
            val dateAddedColumn = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATE_ADDED)
            val mimeTypeColumn = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.MIME_TYPE)
            val sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.SIZE)

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idColumn)
                val name = cursor.getString(nameColumn)
                val dateAdded = cursor.getLong(dateAddedColumn)
                val mimeType = cursor.getString(mimeTypeColumn).substringAfterLast("/")
                val size = cursor.getLong(sizeColumn)
                val uri = ContentUris.withAppendedId(mediaType.uri, id)

                val mediaFile =
                    MediaFile(id, name, dateAdded, mimeType, size, mediaType, uri)
                mediaList.add(mediaFile)
            }
        }
        return mediaList
    }


    fun deleteMedia(medias: List<MediaFile>): Boolean {
        var deletedCount = 0
        for (media in medias) {
            val deleteResult = media.uri?.let { context.contentResolver.delete(it, null, null) }
            if (deleteResult != 0) {
                deletedCount++
            }
        }
        return deletedCount == medias.size
    }


}
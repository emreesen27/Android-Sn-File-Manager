package com.sn.snfilemanager.feature.media.presentation

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sn.mediastorepv.data.ConflictStrategy
import com.sn.mediastorepv.data.MediaType
import com.sn.mediastorepv.util.MediaOperationCallback
import com.sn.snfilemanager.R
import com.sn.snfilemanager.core.base.BaseResult
import com.sn.snfilemanager.core.extensions.getDirectoryNameFromPath
import com.sn.snfilemanager.core.extensions.toFormattedDateFromUnixTime
import com.sn.snfilemanager.core.extensions.toHumanReadableByteCount
import com.sn.snfilemanager.core.util.DocumentType
import com.sn.snfilemanager.core.util.Event
import com.sn.snfilemanager.core.util.MimeTypes
import com.sn.snfilemanager.core.util.StringValue
import com.sn.snfilemanager.providers.mediastore.MediaFile
import com.sn.snfilemanager.providers.mediastore.MediaStoreProvider
import com.sn.snfilemanager.providers.mediastore.toMedia
import com.sn.snfilemanager.providers.preferences.MySharedPreferences
import com.sn.snfilemanager.providers.preferences.PrefsTag
import com.sn.snfilemanager.view.dialog.detail.Detail
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.launch
import java.io.File
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class MediaViewModel @Inject constructor(
    private val mediaStoreProvider: MediaStoreProvider,
    private val sharedPreferences: MySharedPreferences
) : ViewModel() {

    private var fullMediaList: List<MediaFile>? = null
    private var selectedItemList: MutableList<MediaFile> = mutableListOf()
    private var filteredMediaList: List<MediaFile>? = null

    private var mediaType: MediaType? = null
    private var documentType: String? = null
    var selectedPath: String? = null
    var isCopy: Boolean = false

    private val getMediaMutableLiveData: MutableLiveData<Event<List<MediaFile>>> = MutableLiveData()
    val getMediaLiveData: LiveData<Event<List<MediaFile>>> = getMediaMutableLiveData

    private val deleteMediaMutableLiveData: MutableLiveData<Event<List<MediaFile>?>> =
        MutableLiveData()
    val deleteMediaLiveData: LiveData<Event<List<MediaFile>?>> = deleteMediaMutableLiveData

    private val moveMediaMutableLiveData: MutableLiveData<Event<MutableList<Pair<String, String>>?>> =
        MutableLiveData()
    val moveMediaLiveData: LiveData<Event<MutableList<Pair<String, String>>?>> =
        moveMediaMutableLiveData

    private val conflictQuestionMutableLiveData: MutableLiveData<Event<File>> = MutableLiveData()
    val conflictQuestionLiveData: LiveData<Event<File>> = conflictQuestionMutableLiveData

    private val progressMutableLiveData: MutableLiveData<Event<Int>> = MutableLiveData()
    val progressLiveData: LiveData<Event<Int>> = progressMutableLiveData

    private val clearListMutableLiveData: MutableLiveData<Event<Unit>> = MutableLiveData()
    val clearListLiveData: LiveData<Event<Unit>> = clearListMutableLiveData

    var conflictDialogDeferred = CompletableDeferred<Pair<ConflictStrategy, Boolean>>()

    private fun getFilteredMediaTypes(): MutableSet<String>? =
        when (mediaType) {
            MediaType.IMAGES -> PrefsTag.FILTER_IMAGES
            MediaType.VIDEOS -> PrefsTag.FILTER_VIDEOS
            MediaType.AUDIOS -> PrefsTag.FILTER_AUDIOS
            MediaType.FILES -> if (documentType == DocumentType.ARCHIVE.name) PrefsTag.FILTER_ARCHIVES else PrefsTag.FILTER_DOCUMENTS
            else -> null
        }?.let { tag -> sharedPreferences.getStringArray(tag) }

    private fun getDocumentMime(): List<String>? {
        return when (mediaType) {
            MediaType.FILES -> getDocumentExtensions()
            else -> null
        }
    }

    private fun getDocumentExtensions(): List<String> {
        return when (documentType) {
            DocumentType.APK.name -> MimeTypes.APK.values
            DocumentType.ARCHIVE.name -> MimeTypes.ARCHIVES.values
            else -> MimeTypes.DOCUMENTS.values
        }
    }

    private fun generateUUID(): Long = UUID.randomUUID().mostSignificantBits

    fun getMedia() = viewModelScope.launch {
        val filteredMediaTypes: MutableSet<String>? = getFilteredMediaTypes()
        mediaType?.let {
            when (val result = mediaStoreProvider.getMedia(it, getDocumentMime())) {
                is BaseResult.Success -> {
                    fullMediaList = result.data

                    fullMediaList?.let { mediaList ->
                        if (filteredMediaTypes != null) {
                            applyFilter(filteredMediaTypes)
                        } else {
                            getMediaMutableLiveData.value = Event(mediaList)
                            filteredMediaList = mediaList
                        }
                    }
                }

                is BaseResult.Failure -> {
                    Log.d("err", result.exception.toString())
                }
            }
        }
    }

    fun deleteMedia() = viewModelScope.launch {
        when (val result = mediaStoreProvider.deleteMedia(selectedItemList.map { it.toMedia() })) {
            is BaseResult.Success -> {
                if (result.data) {
                    deleteMediaMutableLiveData.value = Event(selectedItemList)
                    clearSelectionList()
                }
            }

            is BaseResult.Failure -> {}
        }
    }

    fun moveMedia() {
        selectedPath?.let { path ->
            if (!isCopy) checkPathConflicts(path)
            if (selectedItemList.isNotEmpty()) {
                viewModelScope.launch {
                    when (val result = mediaStoreProvider.moveMedia(
                        isCopy = isCopy,
                        sourceMedias = selectedItemList.map { it.toMedia() },
                        destinationPath = path,
                        callback = object : MediaOperationCallback {
                            override suspend fun fileConflict(file: File): Pair<ConflictStrategy, Boolean> {
                                conflictQuestionMutableLiveData.postValue(Event(file))
                                val result = conflictDialogDeferred.await()
                                conflictDialogDeferred = CompletableDeferred()
                                return result
                            }

                            override fun onProgress(progress: Int) {
                                progressMutableLiveData.postValue(Event(progress))
                            }
                        }
                    )) {
                        is BaseResult.Success -> {
                            result.data.let { value ->
                                if (value.isNullOrEmpty().not()) {
                                    moveMediaMutableLiveData.value = Event(value)
                                    clearSelectionList()
                                }
                            }
                        }

                        is BaseResult.Failure -> {}
                    }
                }
            } else {
                clearListMutableLiveData.value = Event(Unit)
            }
        }
    }

    fun getMimeByMediaType() =
        when (mediaType) {
            MediaType.IMAGES -> MimeTypes.IMAGES
            MediaType.VIDEOS -> MimeTypes.VIDEOS
            MediaType.AUDIOS -> MimeTypes.AUDIOS
            MediaType.FILES -> if (documentType == DocumentType.ARCHIVE.name) MimeTypes.ARCHIVES else MimeTypes.DOCUMENTS
            else -> null
        }

    fun setArguments(args: MediaFragmentArgs) {
        mediaType = args.mediaType
        documentType = args.documentType
    }

    fun addSelectedItem(mediaFile: MediaFile, selected: Boolean) {
        if (selected) {
            if (mediaFile !in selectedItemList) {
                selectedItemList.add(mediaFile)
            }
        } else {
            selectedItemList.remove(mediaFile)
        }
    }

    fun clearSelectionList() {
        if (selectedItemList.isNotEmpty())
            selectedItemList.clear()
    }


    fun applyFilter(filter: MutableSet<String>) {
        if (filter.isEmpty()) {
            fullMediaList?.let {
                filteredMediaList = it
                getMediaMutableLiveData.value = Event(it)
            }
        } else {
            fullMediaList?.filter { filter.contains(it.ext) }?.let { filteredList ->
                filteredMediaList = filteredList
                getMediaMutableLiveData.value = Event(filteredList)
            }
        }
    }

    fun searchMedia(query: String?) {
        if (query.isNullOrEmpty()) {
            filteredMediaList?.let { result ->
                getMediaMutableLiveData.value = Event(result)
            }
        } else {
            filteredMediaList?.filter { it.name.contains(query) }?.let { result ->
                getMediaMutableLiveData.value = Event(result)
            }
        }
    }

    private fun checkPathConflicts(path: String): Boolean {
        return selectedItemList.removeIf { path == it.data.substringBeforeLast("/") }
    }

    fun getSelectedItem() = selectedItemList

    fun clearFilteredList() {
        filteredMediaList = null
    }

    fun isSingleItemSelected(): Boolean = selectedItemList.size == 1



}
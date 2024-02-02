package com.sn.snfilemanager.feature.media.presentation

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sn.mediastorepv.data.ConflictStrategy
import com.sn.mediastorepv.data.Media
import com.sn.mediastorepv.data.MediaType
import com.sn.snfilemanager.core.base.BaseResult
import com.sn.snfilemanager.core.util.DocumentType
import com.sn.snfilemanager.core.util.Event
import com.sn.snfilemanager.core.util.MimeTypes
import com.sn.snfilemanager.providers.mediastore.MediaStoreProvider
import com.sn.snfilemanager.providers.preferences.MySharedPreferences
import com.sn.snfilemanager.providers.preferences.PrefsTag
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import javax.inject.Inject

@HiltViewModel
class MediaViewModel @Inject constructor(
    private val mediaStoreProvider: MediaStoreProvider,
    private val sharedPreferences: MySharedPreferences
) : ViewModel() {

    private var fullMediaList: List<Media>? = null
    private var selectedItemList: MutableList<Media> = mutableListOf()
    private var filteredMediaList: List<Media>? = null

    private var mediaType: MediaType? = null
    private var documentType: String? = null
    var isCopy: Boolean = false

    private val _getMediaLiveData: MutableLiveData<Event<List<Media>>> = MutableLiveData()
    val getMediaLiveData: LiveData<Event<List<Media>>> = _getMediaLiveData

    private val _pathConflictLiveData: MutableLiveData<Event<String>> = MutableLiveData()
    val pathConflictLiveData: LiveData<Event<String>> = _pathConflictLiveData

    private val _conflictQuestionLiveData: MutableLiveData<Event<String>> = MutableLiveData()
    val conflictQuestionLiveData: LiveData<Event<String>> = _conflictQuestionLiveData

    private val _startMoveJobLiveData: MutableLiveData<Event<Pair<List<Media>, Path>>> =
        MutableLiveData()
    val startMoveJobLiveData: LiveData<Event<Pair<List<Media>, Path>>> = _startMoveJobLiveData

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
                            _getMediaLiveData.value = Event(mediaList)
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

    fun moveMedia(destinationPath: Path) {
        val operationItemList: MutableList<Media> = mutableListOf()
        viewModelScope.launch {
            val job = async {
                for (i in selectedItemList.indices) {
                    val media = selectedItemList[i]
                    val targetPath = destinationPath.resolve(media.name)

                    if (destinationPath.toFile().absolutePath == media.data.substringBeforeLast("/")) {
                        _pathConflictLiveData.postValue(Event(media.name))
                        continue
                    }

                    if (Files.exists(targetPath)) {
                        _conflictQuestionLiveData.postValue(Event(media.name))
                        val result = conflictDialogDeferred.await()
                        conflictDialogDeferred = CompletableDeferred()

                        if (!result.second) {
                            media.conflictStrategy = result.first
                            operationItemList.add(media)
                        } else {
                            if (i < selectedItemList.size - 1) {
                                for (remainingFile in selectedItemList.subList(
                                    i + 1,
                                    selectedItemList.size
                                )) {
                                    remainingFile.conflictStrategy = result.first
                                    operationItemList.add(remainingFile)
                                }
                            }
                            break
                        }
                    } else {
                        operationItemList.add(media)
                    }
                }
            }
            job.await()
            _startMoveJobLiveData.postValue(Event(Pair(operationItemList, destinationPath)))
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

    fun addSelectedItem(mediaFile: Media, selected: Boolean) {
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
                _getMediaLiveData.value = Event(it)
            }
        } else {
            fullMediaList?.filter { filter.contains(it.ext) }?.let { filteredList ->
                filteredMediaList = filteredList
                _getMediaLiveData.value = Event(filteredList)
            }
        }
    }

    fun searchMedia(query: String?) {
        if (query.isNullOrEmpty()) {
            filteredMediaList?.let { result ->
                _getMediaLiveData.value = Event(result)
            }
        } else {
            filteredMediaList?.filter { it.name.contains(query) }?.let { result ->
                _getMediaLiveData.value = Event(result)
            }
        }
    }

    fun getSelectedItem() = selectedItemList

    fun clearFilteredList() {
        filteredMediaList = null
    }

    fun isSingleItemSelected(): Boolean = selectedItemList.size == 1

}
package com.sn.snfilemanager.feature.media.presentation

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sn.snfilemanager.core.base.BaseResult
import com.sn.snfilemanager.providers.preferences.MySharedPreferences
import com.sn.snfilemanager.providers.preferences.PrefsTag
import com.sn.snfilemanager.providers.mediastore.MediaFile
import com.sn.snfilemanager.providers.mediastore.MediaStoreProvider
import com.sn.snfilemanager.providers.mediastore.MediaType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MediaViewModel @Inject constructor(
    private val mediaStoreProvider: MediaStoreProvider,
    private val sharedPreferences: MySharedPreferences
) : ViewModel() {

    private var fullMediaList: List<MediaFile>? = null
    private var selectedItemList: MutableList<MediaFile> = mutableListOf()

    private var mediaType: MediaType? = null

    private val getMediaMutableLiveData: MutableLiveData<List<MediaFile>> = MutableLiveData()
    val getMediaLiveData: LiveData<List<MediaFile>> = getMediaMutableLiveData

    private val searchMediaMutableLiveData: MutableLiveData<List<MediaFile>?> = MutableLiveData()
    val searchMediaLiveData: LiveData<List<MediaFile>?> = searchMediaMutableLiveData

    private val deleteMediaMutableLiveData: MutableLiveData<List<MediaFile>?> = MutableLiveData()
    val deleteMediaLiveData: LiveData<List<MediaFile>?> = deleteMediaMutableLiveData


    private fun getFilteredMediaTypes(): MutableSet<String>? =
        sharedPreferences.getStringArray(
            when (mediaType) {
                MediaType.IMAGES -> PrefsTag.FILTER_IMAGES
                MediaType.VIDEOS -> PrefsTag.FILTER_VIDEOS
                else -> PrefsTag.DEFAULT
            }
        )

    fun setMediaType(mediaType: MediaType) {
        this.mediaType = mediaType
    }

    fun getMedia(type: MediaType) = viewModelScope.launch {
        val filteredMediaTypes: MutableSet<String>? = getFilteredMediaTypes()
        when (val result = mediaStoreProvider.getMedia(type)) {
            is BaseResult.Success -> {
                fullMediaList = result.data

                fullMediaList?.let { mediaList ->
                    if (filteredMediaTypes != null)
                        applyFilter(filteredMediaTypes)
                    else
                        getMediaMutableLiveData.value = mediaList
                }
            }
            is BaseResult.Failure -> {}
        }
    }

    fun deleteMedia() = viewModelScope.launch {
        when (val result = mediaStoreProvider.deleteMedia(selectedItemList)) {
            is BaseResult.Success -> {
                if (result.data) {
                    deleteMediaMutableLiveData.value = selectedItemList
                    selectedItemList.clear()
                }
            }
            is BaseResult.Failure -> {}
        }
    }

    fun addSelectedItem(mediaFile: MediaFile) {
        selectedItemList.add(mediaFile)
    }


    fun clearSearchMediaResult() {
        searchMediaMutableLiveData.value = null
    }

    fun applyFilter(filter: MutableSet<String>) {
        fullMediaList?.filter { filter.contains(it.mimeType) }?.let { filteredMediaList ->
            getMediaMutableLiveData.value = filteredMediaList
        }
    }

    fun searchMedia(query: String) {
        fullMediaList?.filter { it.name.contains(query) }?.let { filteredMediaList ->
            searchMediaMutableLiveData.value = filteredMediaList
        }
    }

}


package com.sn.snfilemanager.feature.media.presentation

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sn.mediastorepv.data.MediaType
import com.sn.snfilemanager.core.base.BaseResult
import com.sn.snfilemanager.core.util.MimeTypes
import com.sn.snfilemanager.providers.preferences.MySharedPreferences
import com.sn.snfilemanager.providers.preferences.PrefsTag
import com.sn.snfilemanager.providers.mediastore.MediaFile
import com.sn.snfilemanager.providers.mediastore.MediaStoreProvider
import com.sn.snfilemanager.providers.mediastore.toMedia
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
    private var isApkFile: Boolean = false
    private var filterIsAll: Boolean = sharedPreferences.getBoolean(PrefsTag.FILTER_ALL)

    private val getMediaMutableLiveData: MutableLiveData<List<MediaFile>> = MutableLiveData()
    val getMediaLiveData: LiveData<List<MediaFile>> = getMediaMutableLiveData

    private val searchMediaMutableLiveData: MutableLiveData<List<MediaFile>?> = MutableLiveData()
    val searchMediaLiveData: LiveData<List<MediaFile>?> = searchMediaMutableLiveData

    private val deleteMediaMutableLiveData: MutableLiveData<List<MediaFile>?> = MutableLiveData()
    val deleteMediaLiveData: LiveData<List<MediaFile>?> = deleteMediaMutableLiveData


    private fun getFilteredMediaTypes(): MutableSet<String>? =
        when (mediaType) {
            MediaType.IMAGES -> PrefsTag.FILTER_IMAGES
            MediaType.VIDEOS -> PrefsTag.FILTER_VIDEOS
            MediaType.AUDIOS -> PrefsTag.FILTER_AUDIOS
            MediaType.FILES -> PrefsTag.FILTER_DOCUMENTS
            else -> null
        }?.let { tag -> sharedPreferences.getStringArray(tag) }

    fun setArguments(args: MediaFragmentArgs) {
        mediaType = args.mediaType
        isApkFile = args.isApkFile
    }

    private fun getMimeByMediaType(): List<String>? {
        return when (mediaType) {
            MediaType.FILES -> if (isApkFile) MimeTypes.APK.values else MimeTypes.DOCUMENT.values
            else -> null
        }
    }

    fun getMedia() = viewModelScope.launch {
        val filteredMediaTypes: MutableSet<String>? = getFilteredMediaTypes()
        mediaType?.let {
            when (val result = mediaStoreProvider.getMedia(it, getMimeByMediaType())) {
                is BaseResult.Success -> {
                    fullMediaList = result.data

                    fullMediaList?.let { mediaList ->
                        if (filteredMediaTypes != null)
                            applyFilter(filteredMediaTypes, filterIsAll)
                        else
                            getMediaMutableLiveData.value = mediaList
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

    fun applyFilter(filter: MutableSet<String>, isAll: Boolean) {
        if (isAll) {
            fullMediaList?.let { getMediaMutableLiveData.value = it }
        } else {
            fullMediaList?.filter { filter.contains(it.ext) }?.let { filteredMediaList ->
                getMediaMutableLiveData.value = filteredMediaList
            }
        }
    }

    fun searchMedia(query: String) {
        fullMediaList?.filter { it.name.contains(query) }?.let { filteredMediaList ->
            searchMediaMutableLiveData.value = filteredMediaList
        }
    }

}


package com.sn.snfilemanager.feature.images.presentation

import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sn.snfilemanager.core.BaseResult
import com.sn.snfilemanager.di.MySharedPreferences
import com.sn.snfilemanager.di.PrefsTag
import com.sn.snfilemanager.media.MediaFile
import com.sn.snfilemanager.media.MediaType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ImageListViewModel @Inject constructor(
    private val mediaOperations: MediaOperations,
    private val sharedPreferences: MySharedPreferences
) : ViewModel() {

    private var fullImagesList: List<MediaFile>? = null
    private var selectedItemList: MutableList<MediaFile> = mutableListOf()

    private val filteredImageTypes: MutableSet<String>? = getFilteredImageTypes()

    private val getImagesMutableLiveData: MutableLiveData<List<MediaFile>> = MutableLiveData()
    val getImagesLiveData: LiveData<List<MediaFile>> = getImagesMutableLiveData

    private val searchImagesMutableLiveData: MutableLiveData<List<MediaFile>?> = MutableLiveData()
    val searchImagesLiveData: LiveData<List<MediaFile>?> = searchImagesMutableLiveData

    private val deleteImagesMutableLiveData: MutableLiveData<List<MediaFile>?> = MutableLiveData()
    val deleteImagesLiveData: LiveData<List<MediaFile>?> = deleteImagesMutableLiveData


    init {
        getImages()
    }

    private fun getFilteredImageTypes(): MutableSet<String>? =
        sharedPreferences.getStringArray(PrefsTag.FILTER_IMAGES)


    private fun getImages() = viewModelScope.launch {
        when (val result = mediaOperations.getMedia(MediaType.IMAGES)) {
            is BaseResult.Success -> {
                fullImagesList = result.data

                fullImagesList?.let { imageList ->
                    if (filteredImageTypes != null)
                        applyFilter(filteredImageTypes)
                    else
                        getImagesMutableLiveData.value = imageList
                }
            }
            is BaseResult.Failure -> {}
        }
    }

    fun deleteMedia() = viewModelScope.launch {
        when (val result = mediaOperations.deleteMedia(selectedItemList)) {
            is BaseResult.Success -> {
                if (result.data) {
                    deleteImagesMutableLiveData.value = selectedItemList
                    selectedItemList.clear()
                }
            }
            is BaseResult.Failure -> {}
        }
    }

    fun addSelectedItem(mediaFile: MediaFile) {
        selectedItemList.add(mediaFile)
    }


    fun clearSearchImagesResult() {
        searchImagesMutableLiveData.value = null
    }

    fun applyFilter(filter: MutableSet<String>) {
        val imageChips = filter.map { "image/$it" }.toSet()
        fullImagesList?.filter { imageChips.contains(it.mimeType) }?.let { filteredImagesList ->
            getImagesMutableLiveData.value = filteredImagesList
        }
    }

    fun searchImage(query: String) {
        fullImagesList?.filter { it.name.contains(query) }?.let { filteredImagesList ->
            searchImagesMutableLiveData.value = filteredImagesList
        }
    }

}


package com.sn.snfilemanager

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.sn.snfilemanager.core.util.Config
import com.sn.snfilemanager.core.util.Event
import com.sn.snfilemanager.core.util.SortCriterion
import com.sn.snfilemanager.core.util.SortOrder
import com.sn.snfilemanager.providers.preferences.MySharedPreferences
import com.sn.snfilemanager.providers.preferences.PrefsTag
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MainViewModel
    @Inject
    constructor(
        private val mySharedPreferences: MySharedPreferences,
    ) : ViewModel() {
        private val _firstRunLiveData: MutableLiveData<Event<Boolean>> = MutableLiveData()
        val firstRunLiveData: LiveData<Event<Boolean>> = _firstRunLiveData

        init {
            checkFirsRun()
            getSortData()
        }

        private fun checkFirsRun() {
            val firstRun: Boolean = mySharedPreferences.getBoolean(PrefsTag.FIRST_RUN)
            _firstRunLiveData.postValue(Event(firstRun))
        }

        private fun getSortData() {
            val criterionString = mySharedPreferences.getString(PrefsTag.SORT_CRITERION)
            val sortOrderString = mySharedPreferences.getString(PrefsTag.SORT_ORDER)
            val mediaCriterionString = mySharedPreferences.getString(PrefsTag.MEDIA_SORT_CRITERION)
            val mediaSortOrderString = mySharedPreferences.getString(PrefsTag.MEDIA_SORT_ORDER)

            Config.sortCriterion = SortCriterion.valueOf(criterionString ?: SortCriterion.NAME.name)
            Config.sortOrder = SortOrder.valueOf(sortOrderString ?: SortOrder.ASCENDING.name)

            Config.mediaSortCriterion = SortCriterion.valueOf(mediaCriterionString ?: SortCriterion.NAME.name)
            Config.mediaSortOrder = SortOrder.valueOf(mediaSortOrderString ?: SortOrder.ASCENDING.name)
        }
    }

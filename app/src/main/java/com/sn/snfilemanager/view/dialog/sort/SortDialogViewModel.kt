package com.sn.snfilemanager.view.dialog.sort

import androidx.lifecycle.ViewModel
import com.sn.snfilemanager.core.util.SortCriterion
import com.sn.snfilemanager.core.util.SortOrder
import com.sn.snfilemanager.providers.preferences.MySharedPreferences
import com.sn.snfilemanager.providers.preferences.PrefsTag
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SortDialogViewModel
    @Inject
    constructor(
        private val sharedPreferences: MySharedPreferences,
    ) : ViewModel() {
        private lateinit var tags: Pair<PrefsTag, PrefsTag>

        fun setPrefsTag(isMedia: Boolean) {
            tags =
                if (isMedia) {
                    PrefsTag.MEDIA_SORT_CRITERION to PrefsTag.MEDIA_SORT_ORDER
                } else {
                    PrefsTag.SORT_CRITERION to PrefsTag.SORT_ORDER
                }
        }

        fun putSortData(
            criterion: SortCriterion,
            sortOrder: SortOrder,
        ) {
            sharedPreferences.putString(tags.first, criterion.name)
            sharedPreferences.putString(tags.second, sortOrder.name)
        }

        fun getSortData(): Pair<SortCriterion, SortOrder> {
            val criterionString = sharedPreferences.getString(tags.first)
            val sortOrderString = sharedPreferences.getString(tags.second)

            val criterion = SortCriterion.valueOf(criterionString ?: SortCriterion.LAST_MODIFIED.name)
            val sortOrder = SortOrder.valueOf(sortOrderString ?: SortOrder.DESCENDING.name)

            return Pair(criterion, sortOrder)
        }
    }

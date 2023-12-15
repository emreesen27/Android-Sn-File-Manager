package com.sn.snfilemanager.feature.filter

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.forEach
import androidx.core.view.get
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.chip.Chip
import com.sn.snfilemanager.R
import com.sn.snfilemanager.core.extensions.click
import com.sn.snfilemanager.core.util.MimeTypes
import com.sn.snfilemanager.databinding.BottomSheetFilterBinding
import com.sn.snfilemanager.providers.preferences.MySharedPreferences
import com.sn.snfilemanager.providers.preferences.PrefsTag
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class FilterBottomSheet : BottomSheetDialogFragment() {

    @Inject
    lateinit var sharedPreferences: MySharedPreferences

    private var mimeTypes: MimeTypes? = null
    private var prefsTag: PrefsTag? = null

    private val binding: BottomSheetFilterBinding by lazy {
        BottomSheetFilterBinding.inflate(layoutInflater)
    }

    var onFilterApplyClick: ((MutableSet<String>) -> Unit)? = null

    companion object {
        private const val ARG_MIME_TYPE = "ARG_CHIP"
        const val TAG = "FilterBottomSheet"

        fun newInstance(
            mimeTypes: MimeTypes
        ): FilterBottomSheet {
            return FilterBottomSheet().apply {
                arguments = Bundle().apply {
                    putParcelable(ARG_MIME_TYPE, mimeTypes)
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        setArguments()
        setPrefsTag()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mimeTypes?.let {
            createChipsFromMimeTypes(it).also {
                clickApply()
            }
        }

        binding.btnCancel.click {
            dismiss()
        }

    }

    private fun setArguments() {
        mimeTypes = requireArguments().getParcelable(ARG_MIME_TYPE)
    }

    private fun setPrefsTag() {
        prefsTag = when (mimeTypes) {
            MimeTypes.IMAGES -> PrefsTag.FILTER_IMAGES
            MimeTypes.VIDEOS -> PrefsTag.FILTER_VIDEOS
            MimeTypes.AUDIOS -> PrefsTag.FILTER_AUDIOS
            MimeTypes.DOCUMENTS -> PrefsTag.FILTER_DOCUMENTS
            MimeTypes.ARCHIVES -> PrefsTag.FILTER_ARCHIVES
            else -> null
        }
    }

    private fun saveChipsChoice(chips: MutableSet<String>) {
        prefsTag?.let { sharedPreferences.putStringArray(it, chips) }
    }

    private fun getChipsChoice(): MutableSet<String>? =
        prefsTag?.let { sharedPreferences.getStringArray(it) }

    private fun clickApply() {
        binding.btnApply.click {
            val ids = binding.chipsGroup.checkedChipIds
            val chips: MutableSet<String> = hashSetOf()

            ids.forEach { id ->
                val chip = binding.chipsGroup.findViewById<Chip>(id)
                chips.add(chip.text.toString())
            }

            if (chips.contains(getString(R.string.all)))
                chips.clear()

            saveChipsChoice(chips)
            onFilterApplyClick?.invoke(chips)

            dismiss()
        }
    }

    private fun createChipsFromMimeTypes(mimeTypes: MimeTypes) {
        val chipsGroup = binding.chipsGroup
        val chipsChoice = getChipsChoice()
        var isAllSelected: Boolean

        for ((index, value) in mimeTypes.values.withIndex()) {
            if (index >= 8) break
            val chipLayout = layoutInflater.inflate(R.layout.layout_chip, chipsGroup, false) as Chip

            chipLayout.text = value
            chipLayout.isChecked =
                (chipsChoice.isNullOrEmpty() && value == getString(R.string.all)) || chipsChoice?.contains(
                    chipLayout.text
                ) == true


            if (chipLayout.text == getString(R.string.all)) {
                isAllSelected = chipLayout.isChecked

                chipLayout.click {
                    if (!isAllSelected) {
                        chipsGroup.forEach { chip ->
                            (chip as? Chip)?.let {
                                if (it.text != getString(R.string.all)) {
                                    it.isChecked = false
                                }
                            }
                        }
                        isAllSelected = true
                    }
                }
            } else {
                chipLayout.click {
                    val allChip = chipsGroup[0] as? Chip
                    allChip?.isChecked = false
                    isAllSelected = false
                }
            }

            chipsGroup.addView(chipLayout)
        }
    }
}
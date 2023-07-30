package com.sn.snfilemanager.feature.sheet

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.chip.Chip
import com.sn.snfilemanager.R
import com.sn.snfilemanager.databinding.BottomSheetFilterBinding
import com.sn.snfilemanager.providers.preferences.MySharedPreferences
import com.sn.snfilemanager.providers.preferences.PrefsTag
import com.sn.snfilemanager.providers.mediastore.MimeTypes
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class FilterBottomSheet : BottomSheetDialogFragment() {

    @Inject
    lateinit var sharedPreferences: MySharedPreferences

    private var mimeTypes: MimeTypes? = null
    private var prefsTag: PrefsTag = PrefsTag.DEFAULT

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

    }

    private fun setArguments() {
        mimeTypes = requireArguments().getParcelable(ARG_MIME_TYPE)
    }

    private fun setPrefsTag() {
        prefsTag = when (mimeTypes) {
            MimeTypes.IMAGES -> PrefsTag.FILTER_IMAGES
            MimeTypes.VIDEOS -> PrefsTag.FILTER_VIDEOS
            else -> PrefsTag.DEFAULT
        }
    }

    private fun saveChipsChoice(chips: MutableSet<String>) {
        sharedPreferences.putStringArray(prefsTag, chips)
    }

    private fun getChipsChoice(): MutableSet<String>? =
        sharedPreferences.getStringArray(prefsTag)

    private fun clickApply() {
        binding.btnApply.setOnClickListener {
            val ids = binding.chipsGroup.checkedChipIds
            val chips: MutableSet<String> = hashSetOf()

            ids.forEach { id ->
                val chip = binding.chipsGroup.findViewById<Chip>(id)
                chips.add(chip.text.toString())
            }

            saveChipsChoice(chips)
            onFilterApplyClick?.invoke(chips)
        }
    }

    private fun createChipsFromMimeTypes(mimeTypes: MimeTypes) {
        val chipsGroup = binding.chipsGroup
        val chipsChoice = getChipsChoice()
        for (value in mimeTypes.values) {
            val chipLayout =
                layoutInflater.inflate(R.layout.layout_chip, chipsGroup, false) as Chip

            chipLayout.text = value
            chipLayout.isChecked =
                chipsChoice.isNullOrEmpty() || chipsChoice.contains(chipLayout.text)

            chipsGroup.addView(chipLayout)
        }
    }
}
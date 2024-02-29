package com.sn.snfilemanager.view.dialog.license

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.sn.snfilemanager.core.extensions.click
import com.sn.snfilemanager.core.extensions.openUrl
import com.sn.snfilemanager.databinding.ItemLicenseBinding

class LicenseAdapter(private val context: Context) :
    RecyclerView.Adapter<LicenseAdapter.LicenseViewHolder>() {
    private val licenses: List<License> =
        listOf(
            License(
                "Glide",
                "https://github.com/bumptech/glide",
                "https://github.com/bumptech/glide/blob/master/LICENSE",
            ),
            License(
                "Nested Progress",
                "https://github.com/emreesen27/Android-Nested-Progress",
                "https://github.com/emreesen27/Android-Nested-Progress/blob/master/LICENSE",
            ),
            License(
                "Toasty",
                "https://github.com/GrenderG/Toasty",
                "https://github.com/GrenderG/Toasty/blob/master/LICENSE",
            ),
            License(
                "Lottie Android",
                "https://github.com/airbnb/lottie-android",
                "https://github.com/airbnb/lottie-android/blob/master/LICENSE",
            ),
            License(
                "Ssp",
                "https://github.com/intuit/ssp",
                "https://github.com/intuit/ssp/blob/master/LICENSE",
            ),
            License(
                "Android Animation",
                "https://github.com/gayanvoice/android-animations",
                "https://github.com/gayanvoice/android-animations/blob/master/LICENSE",
            ),
        )

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): LicenseAdapter.LicenseViewHolder {
        val binding =
            ItemLicenseBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return LicenseViewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: LicenseAdapter.LicenseViewHolder,
        position: Int,
    ) {
        holder.bind(licenses[position])
    }

    override fun getItemCount(): Int {
        return licenses.size
    }

    inner class LicenseViewHolder(private val binding: ItemLicenseBinding) :
        RecyclerView.ViewHolder(binding.root) {
        init {
            binding.mtvGithub.click {
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    val clickedLicense = licenses[adapterPosition]
                    context.openUrl(clickedLicense.url)
                }
            }
            binding.mtvLicense.click {
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    val clickedLicense = licenses[adapterPosition]
                    context.openUrl(clickedLicense.url)
                }
            }
        }

        fun bind(model: License) {
            binding.mtvName.text = model.name
        }
    }
}

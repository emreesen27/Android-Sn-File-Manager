package com.sn.snfilemanager.view.dialog.detail

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.sn.snfilemanager.databinding.ItemDetailBinding

class DetailItemAdapter(
    private val context: Context,
) : RecyclerView.Adapter<DetailItemAdapter.DetailViewHolder>() {
    private var detailItems: List<Detail> = emptyList()

    @SuppressLint("NotifyDataSetChanged")
    fun setItems(newItems: List<Detail>) {
        detailItems = newItems
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): DetailItemAdapter.DetailViewHolder {
        val binding =
            ItemDetailBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return DetailViewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: DetailItemAdapter.DetailViewHolder,
        position: Int,
    ) {
        holder.bind(detailItems[position])
    }

    override fun getItemCount(): Int {
        return detailItems.size
    }

    inner class DetailViewHolder(private val binding: ItemDetailBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(model: Detail) {
            binding.tvTitle.text = model.title.asString(context)
            binding.tvValue.text = model.value
        }
    }
}

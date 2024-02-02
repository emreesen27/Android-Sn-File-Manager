package com.sn.snfilemanager.view.component.breadcrumb

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.sn.snfilemanager.R
import com.sn.snfilemanager.core.extensions.click

class BreadCrumbAdapter(var breadCrumbItemClickListener: BreadCrumbItemClickListener) :
    RecyclerView.Adapter<BreadCrumbAdapter.ViewHolder>() {
    private var breadCrumbItemsData: MutableList<BreadItem> = mutableListOf()

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_bread_crumb, parent, false),
        )
    }

    override fun getItemCount(): Int = breadCrumbItemsData.size

    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int,
    ) {
        val item = breadCrumbItemsData[position]

        if (position == 0) {
            holder.breadCrumbSeparator.visibility = View.GONE
        } else {
            holder.breadCrumbSeparator.visibility = View.VISIBLE
        }

        holder.breadCrumbTitle.text = item.title
    }

    fun getBreadCrumbItemsSize(): Int = breadCrumbItemsData.size

    fun removeLastBreadCrumbItem() {
        if (breadCrumbItemsData.size > 1) {
            breadCrumbItemsData.removeLast()
            notifyItemRemoved(breadCrumbItemsData.size)
        }
    }

    fun removeItemsFromPosition(position: Int) {
        if (position >= 0 && position < breadCrumbItemsData.size) {
            val sublistToRemove =
                ArrayList(breadCrumbItemsData.subList(position + 1, breadCrumbItemsData.size))
            notifyItemRangeRemoved(position + 1, breadCrumbItemsData.size)
            breadCrumbItemsData.removeAll(sublistToRemove)
        }
    }

    fun addBreadCrumbItem(item: BreadItem) {
        breadCrumbItemsData.add(item)
        notifyItemInserted(breadCrumbItemsData.size - 1)
    }

    inner class ViewHolder(breadCrumbItem: View) : RecyclerView.ViewHolder(breadCrumbItem) {
        var breadCrumbTitle: TextView = itemView.findViewById(R.id.bread_crumb_title)
        var breadCrumbSeparator: ImageView = itemView.findViewById(R.id.bread_crumb_separator)

        init {
            breadCrumbTitle.click {
                breadCrumbItemClickListener.onItemClick(
                    breadCrumbItemsData[adapterPosition],
                    adapterPosition,
                )
            }
        }
    }
}

package com.sn.snfilemanager.view.component.breadcrumb

import android.content.Context
import android.util.AttributeSet
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class BreadCrumbView
    @JvmOverloads
    constructor(context: Context, attrs: AttributeSet? = null) :
    FrameLayout(context, attrs) {
        private lateinit var recyclerView: RecyclerView
        private lateinit var breadCrumbAdapter: BreadCrumbAdapter

        init {
            createAndAddRecyclerView(context)
            this.setPadding(40, 0, 40, 0)
        }

        private fun createAndAddRecyclerView(context: Context) {
            recyclerView = RecyclerView(context)
            val recyclerViewParams =
                ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT,
                )

            recyclerView.layoutManager =
                LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            breadCrumbAdapter =
                BreadCrumbAdapter(
                    object : BreadCrumbItemClickListener {
                        override fun onItemClick(
                            item: BreadItem,
                            position: Int,
                        ) {}
                    },
                )

            recyclerView.adapter = breadCrumbAdapter

            addView(recyclerView, recyclerViewParams)
        }

        fun setListener(listener: BreadCrumbItemClickListener) {
            breadCrumbAdapter.breadCrumbItemClickListener = listener
        }

        fun addBreadCrumbItem(item: BreadItem) {
            breadCrumbAdapter.addBreadCrumbItem(item)
            recyclerView.smoothScrollToPosition(breadCrumbAdapter.getBreadCrumbItemsSize() - 1)
        }

        fun removeLastBreadCrumbItem() = breadCrumbAdapter.removeLastBreadCrumbItem()

        fun removeItemsWithRange(position: Int) = breadCrumbAdapter.removeItemsFromPosition(position)
    }

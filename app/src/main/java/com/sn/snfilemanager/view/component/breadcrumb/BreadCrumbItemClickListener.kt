package com.sn.snfilemanager.view.component.breadcrumb

interface BreadCrumbItemClickListener {
    fun onItemClick(
        item: BreadItem,
        position: Int,
    )
}

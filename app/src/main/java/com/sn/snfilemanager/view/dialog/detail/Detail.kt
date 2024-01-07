package com.sn.snfilemanager.view.dialog.detail

import com.idanatz.oneadapter.external.interfaces.Diffable
import com.sn.snfilemanager.core.util.StringValue

data class Detail(
    val id: Long,
    val title: StringValue.StringResource,
    val value: String
) : Diffable {
    override val uniqueIdentifier: Long
        get() = id

    override fun areContentTheSame(other: Any): Boolean = id == (other as? Detail)?.id
}

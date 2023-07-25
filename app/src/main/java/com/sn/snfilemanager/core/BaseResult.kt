package com.sn.snfilemanager.core

sealed class BaseResult<out R> {
    data class Success<out T>(val data: T) : BaseResult<T>()
    data class Failure(val exception: Exception) : BaseResult<Nothing>()
}

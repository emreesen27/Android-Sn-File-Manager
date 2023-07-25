package com.sn.snfilemanager.core

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

abstract class BaseUseCase<in P, R> {

    abstract suspend fun execute(params: P): BaseResult<R>

    suspend operator fun invoke(params: P): BaseResult<R> {
        return try {
            withContext(Dispatchers.IO) {
                execute(params)
            }
        } catch (e: Exception) {
            BaseResult.Failure(e)
        }
    }
}
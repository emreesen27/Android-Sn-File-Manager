package com.sn.snfilemanager.job

interface JobCompletedCallback {
    fun <T> jobOnCompleted(
        jobType: JobType,
        data: List<T>?,
    )

    fun scannedOnCompleted()
}

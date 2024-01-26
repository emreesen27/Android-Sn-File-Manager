package com.sn.snfilemanager.job


interface JobCompletedCallback {
    fun jobOnCompleted(jobType: JobType)
    fun scannedOnCompleted()
}
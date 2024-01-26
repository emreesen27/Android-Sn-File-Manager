package com.sn.snfilemanager.core.base

import com.sn.snfilemanager.job.JobService
import java.io.IOException
import java.io.InterruptedIOException
import java.util.Random

abstract class BaseJob {
    val id = Random().nextInt()

    internal lateinit var service: JobService
        private set

    fun runOn(service: JobService) {
        this.service = service
        try {
            run()
        } catch (e: InterruptedIOException) {
            e.printStackTrace()
        } catch (e: Exception) {
            e.printStackTrace()
            //service.showToast(e.toString())
        } finally {
            service.notificationManager.cancel(id)
        }
    }

    @Throws(IOException::class)
    protected abstract fun run()

    abstract fun onCompleted()
}

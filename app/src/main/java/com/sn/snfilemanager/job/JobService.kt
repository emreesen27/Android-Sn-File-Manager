package com.sn.snfilemanager.job

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import androidx.annotation.MainThread
import com.sn.mediastorepv.data.Media
import com.sn.snfilemanager.core.base.BaseJob
import com.sn.snfilemanager.core.extensions.removeFirst
import com.sn.snfilemanager.core.util.FrNotificationManager
import com.sn.snfilemanager.core.util.WakeWifiLock
import com.sn.snfilemanager.feature.files.data.FileModel
import com.sn.snfilemanager.job.file.CopyFileJob
import com.sn.snfilemanager.job.file.CreateDirectory
import com.sn.snfilemanager.job.file.DeleteFileJob
import com.sn.snfilemanager.job.media.DeleteMediaJob
import com.sn.snfilemanager.job.media.MoveMediaJob
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.nio.file.Path

// This class and some associated classes are inspired by or directly implemented from Material Files by Hai Zhang.
class JobService : Service() {
    private lateinit var wakeWifiLock: WakeWifiLock
    internal lateinit var notificationManager: FrNotificationManager
        private set

    private val jobScope = CoroutineScope(Dispatchers.IO)

    private val runningJobs = mutableMapOf<BaseJob, Job>()

    override fun onCreate() {
        super.onCreate()
        wakeWifiLock = WakeWifiLock(applicationContext, JobService::class.java.simpleName)
        notificationManager = FrNotificationManager(this)
        instance = this

        while (pendingJobs.isNotEmpty()) {
            startJob(pendingJobs.removeFirst())
        }
    }

    override fun onBind(intent: Intent): IBinder? = null

    override fun onStartCommand(
        intent: Intent?,
        flags: Int,
        startId: Int,
    ): Int = START_STICKY

    private val jobCount: Int
        get() = synchronized(runningJobs) { runningJobs.size }

    private fun startJob(job: BaseJob) {
        synchronized(runningJobs) {
            val newJob =
                jobScope.launch {
                    job.runOn(this@JobService)
                    synchronized(runningJobs) {
                        runningJobs.remove(job)
                        updateWakeWifiLockLocked()
                    }
                }
            runningJobs[job] = newJob
            updateWakeWifiLockLocked()
        }
    }

    private fun cancelJob(id: Int) {
        synchronized(runningJobs) {
            runningJobs.removeFirst { it.key.id == id }?.value?.cancel()
            updateWakeWifiLockLocked()
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        instance = null

        synchronized(runningJobs) {
            while (runningJobs.isNotEmpty()) {
                runningJobs.removeFirst().value.cancel()
            }
            updateWakeWifiLockLocked()
        }
    }

    private fun updateWakeWifiLockLocked() {
        wakeWifiLock.isAcquired = jobCount > 0
    }

    companion object {
        private var instance: JobService? = null

        private val pendingJobs = mutableListOf<BaseJob>()

        val runningJobCount: Int
            @MainThread
            get() = instance?.jobCount ?: 0

        @MainThread
        private fun startJob(
            job: BaseJob,
            context: Context,
        ) {
            val instance = instance
            if (instance != null) {
                instance.startJob(job)
            } else {
                pendingJobs.add(job)
                context.startService(Intent(context, JobService::class.java))
            }
        }

        fun copy(
            sources: List<FileModel>,
            targetPath: Path,
            isCopy: Boolean,
            completed: JobCompletedCallback,
            context: Context,
        ) {
            startJob(CopyFileJob(sources, targetPath, isCopy, completed), context)
        }

        fun delete(
            sources: List<FileModel>,
            completed: JobCompletedCallback,
            context: Context,
        ) {
            startJob(DeleteFileJob(sources, completed), context)
        }

        fun deleteMedia(
            sources: List<Media>,
            completed: JobCompletedCallback,
            context: Context,
        ) {
            startJob(DeleteMediaJob(sources, completed), context)
        }

        fun copyMedia(
            sources: List<Media>,
            targetPath: Path,
            isCopy: Boolean,
            completed: JobCompletedCallback,
            context: Context,
        ) {
            startJob(MoveMediaJob(sources, targetPath, isCopy, completed), context)
        }

        fun createDirectory(
            targetPath: Path,
            completed: JobCompletedCallback,
            context: Context,
        ) {
            startJob(CreateDirectory(targetPath, completed), context)
        }

        @MainThread
        fun cancelJob(id: Int) {
            pendingJobs.removeFirst { it.id == id }
            instance?.cancelJob(id)
        }
    }
}

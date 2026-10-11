package com.jannlawm.alssdosanjuan.data.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.jannlawm.alssdosanjuan.data.repository.TelemetryRepository

class SyncWorker(appContext: Context, workerParams: WorkerParameters) : CoroutineWorker(appContext, workerParams) {
    override suspend fun doWork(): Result {
        return try {
            val repository = TelemetryRepository(applicationContext)
            val success = repository.syncUnsyncedTelemetry()
            if (success) Result.success() else Result.retry()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}

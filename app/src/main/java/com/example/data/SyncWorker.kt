package com.example.data

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

class SyncWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        Log.d("SyncWorker", "Periodic background M3U sync starting...")
        return try {
            val database = KivuDatabase.getDatabase(applicationContext)
            val repository = ChannelRepository(database.kivuDao())
            repository.refreshChannels()
            Log.d("SyncWorker", "Periodic background M3U sync finished successfully.")
            Result.success()
        } catch (e: Exception) {
            Log.e("SyncWorker", "Periodic background M3U sync failed, retrying...", e)
            Result.retry()
        }
    }
}

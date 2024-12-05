package com.example.coleramsey_comp304lab4_ex1.data

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.work.*
import java.util.concurrent.TimeUnit

class SyncWorker(appContext: Context, workerParams: WorkerParameters) : Worker(appContext, workerParams) {
    override fun doWork(): Result {
        Log.d("SyncWorker", "Background task running.")

        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "sync_task_channel"
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "Sync Task", NotificationManager.IMPORTANCE_DEFAULT)
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(applicationContext, channelId)
            .setContentTitle("SyncWorker")
            .setContentText("Background task running")
            .setSmallIcon(android.R.drawable.ic_menu_compass)
            .build()

        notificationManager.notify(1, notification)

        return Result.success()
    }
}

object WorkManagerTasks {
    fun scheduleSyncTask(context: Context) {
        val workRequest = PeriodicWorkRequestBuilder<SyncWorker>(15, TimeUnit.MINUTES).build()
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            "SyncTask",
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
    }
}
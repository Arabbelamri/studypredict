package com.example.studypredict.reminders

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

// Channel
const val REMINDER_CHANNEL_ID = "study_predict_reminders"

// Extras (doivent matcher ceux utilisés dans scheduleDailyReminder)
const val EXTRA_TITLE = "extra_title"
const val EXTRA_MESSAGE = "extra_message"
const val EXTRA_REQUEST_CODE = "extra_request_code"

class ReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val title = intent.getStringExtra(EXTRA_TITLE) ?: "Rappel"
        val message = intent.getStringExtra(EXTRA_MESSAGE) ?: "C'est l'heure !"
        val reqCode = intent.getIntExtra(EXTRA_REQUEST_CODE, 0)

        // Crée le channel (Android 8+)
        createReminderNotificationChannel(context)

        // Build notification
        val notification = NotificationCompat.Builder(context, REMINDER_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_popup_reminder) // simple et dispo partout
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        // Android 13+ : permission runtime
        if (Build.VERSION.SDK_INT >= 33) {
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
        }

        NotificationManagerCompat.from(context).notify(reqCode, notification)
    }
}

fun createReminderNotificationChannel(context: Context) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

    val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    if (nm.getNotificationChannel(REMINDER_CHANNEL_ID) != null) return

    val channel = NotificationChannel(
        REMINDER_CHANNEL_ID,
        "Rappels StudyPredict",
        NotificationManager.IMPORTANCE_HIGH
    ).apply {
        description = "Notifications de rappel StudyPredict"
    }

    nm.createNotificationChannel(channel)
}
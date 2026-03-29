package com.example.studypredict.reminders

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import java.util.Calendar

// Channel
const val REMINDER_CHANNEL_ID = "study_predict_reminders"

// Extras (doivent matcher ceux utilisés dans scheduleDailyReminder)
const val EXTRA_TITLE = "extra_title"
const val EXTRA_MESSAGE = "extra_message"
const val EXTRA_REQUEST_CODE = "extra_request_code"
const val EXTRA_HOUR = "extra_hour"
const val EXTRA_MINUTE = "extra_minute"

class ReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val title = intent.getStringExtra(EXTRA_TITLE) ?: "Rappel"
        val message = intent.getStringExtra(EXTRA_MESSAGE) ?: "C'est l'heure !"
        val reqCode = intent.getIntExtra(EXTRA_REQUEST_CODE, 0)
        val hour = intent.getIntExtra(EXTRA_HOUR, -1)
        val minute = intent.getIntExtra(EXTRA_MINUTE, -1)

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
                rescheduleNext(context, reqCode, title, message, hour, minute)
                return
            }
        }

        NotificationManagerCompat.from(context).notify(reqCode, notification)
        rescheduleNext(context, reqCode, title, message, hour, minute)
    }
}

private fun rescheduleNext(
    context: Context,
    requestCode: Int,
    title: String,
    message: String,
    hour: Int,
    minute: Int
) {
    if (requestCode < 0 || hour !in 0..23 || minute !in 0..59) return

    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
        return
    }

    val nextCal = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, hour)
        set(Calendar.MINUTE, minute)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
        add(Calendar.DAY_OF_YEAR, 1)
    }

    val nextIntent = Intent(context, ReminderReceiver::class.java).apply {
        putExtra(EXTRA_TITLE, title)
        putExtra(EXTRA_MESSAGE, message)
        putExtra(EXTRA_REQUEST_CODE, requestCode)
        putExtra(EXTRA_HOUR, hour)
        putExtra(EXTRA_MINUTE, minute)
    }
    val nextPendingIntent = PendingIntent.getBroadcast(
        context,
        requestCode,
        nextIntent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            nextCal.timeInMillis,
            nextPendingIntent
        )
    } else {
        alarmManager.setExact(
            AlarmManager.RTC_WAKEUP,
            nextCal.timeInMillis,
            nextPendingIntent
        )
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

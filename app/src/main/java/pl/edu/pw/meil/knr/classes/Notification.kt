package pl.edu.pw.meil.knr.classes

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.graphics.Bitmap
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import pl.edu.pw.meil.knr.R
import pl.edu.pw.meil.knr.fragments.CHANNEL_ID

object Notification {
    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Connection State"
            val descriptionText = "Show connection status of the Hal Rover"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
                enableLights(true)
            }
            (context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).apply {
                createNotificationChannel(channel)
            }
        }
    }

    fun createNotification(context: Context, title: String, content: String, largeIcon: Bitmap?) {
        val builder = NotificationCompat.Builder(context, CHANNEL_ID).apply {
            setSmallIcon(R.mipmap.ic_launcher)
            setContentTitle(title)
            setContentText(content)
            setAutoCancel(false)
            setCategory(NotificationCompat.CATEGORY_SYSTEM)
            priority = NotificationCompat.PRIORITY_DEFAULT
            setLargeIcon(largeIcon)
        }
        with(NotificationManagerCompat.from(context)) {
            notify(1, builder.build())
        }
    }
}
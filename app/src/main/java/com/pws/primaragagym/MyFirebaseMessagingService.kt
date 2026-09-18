package com.pws.primaragagym

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.pws.primaragagym.commond.FcmTokenManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        // Simpan token baru ke Firestore ketika FCM token di-refresh
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        CoroutineScope(Dispatchers.IO).launch {
            FcmTokenManager.saveTokenToFirestore(userId, token)
        }
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        val title = remoteMessage.notification?.title
            ?: remoteMessage.data["title"]
            ?: "Primaraga Gym"

        val body = remoteMessage.notification?.body
            ?: remoteMessage.data["body"]
            ?: ""

        showNotification(title, body)
    }

    private fun showNotification(title: String, body: String) {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            System.currentTimeMillis().toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val largeIcon = try {
            BitmapFactory.decodeResource(resources, R.drawable.logogym)
        } catch (e: Exception) {
            null
        }

        val defaultSoundUri = android.media.RingtoneManager.getDefaultUri(android.media.RingtoneManager.TYPE_NOTIFICATION)

        val notificationBuilder = NotificationCompat.Builder(this, FcmTokenManager.CHANNEL_ID)
            .setSmallIcon(R.drawable.logogym)
            .apply {
                if (largeIcon != null) {
                    setLargeIcon(largeIcon)
                }
            }
            .setColor(ContextCompat.getColor(this, R.color.green_accent))
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setAutoCancel(true)
            .setSound(defaultSoundUri)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setVibrate(longArrayOf(0, 300, 200, 300))
            .setContentIntent(pendingIntent)

        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        notificationManager.notify(
            System.currentTimeMillis().toInt(),
            notificationBuilder.build()
        )
    }
}

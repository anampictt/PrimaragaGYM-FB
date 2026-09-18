package com.pws.primaragagym.commond

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.tasks.await

object FcmTokenManager {

    private const val COLLECTION_FCM_TOKENS = "fcm_tokens"
    // ID diupdate ke v2 agar Android 8-13 membuat channel baru yang dilengkapi audio sound
    const val CHANNEL_ID = "primaraga_gym_channel_v2"
    const val CHANNEL_NAME = "Primaraga Gym Notifications"

    /**
     * Ambil FCM token terbaru dan simpan ke Firestore untuk user yang sedang login.
     */
    suspend fun registerToken(userId: String) {
        try {
            val token = FirebaseMessaging.getInstance().token.await()
            saveTokenToFirestore(userId, token)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Simpan FCM token ke Firestore collection fcm_tokens/{userId}
     */
    suspend fun saveTokenToFirestore(userId: String, token: String) {
        try {
            val data = mapOf(
                "token" to token,
                "userId" to userId,
                "updatedAt" to com.google.firebase.firestore.FieldValue.serverTimestamp()
            )
            FirebaseFirestore.getInstance()
                .collection(COLLECTION_FCM_TOKENS)
                .document(userId)
                .set(data, SetOptions.merge())
                .await()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Hapus token dari Firestore saat logout.
     */
    suspend fun deleteToken(userId: String) {
        try {
            FirebaseFirestore.getInstance()
                .collection(COLLECTION_FCM_TOKENS)
                .document(userId)
                .delete()
                .await()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Buat notification channel (required Android 8.0+)
     */
    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            // Hapus channel lama yang tersimpan tanpa konfigurasi audio
            try {
                notificationManager.deleteNotificationChannel("primaraga_gym_channel")
            } catch (_: Exception) {}

            val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            val audioAttributes = AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                .build()

            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifikasi otomatis Primaraga Gym"
                setSound(defaultSoundUri, audioAttributes)
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 300, 200, 300)
                setShowBadge(true)
                lockscreenVisibility = android.app.Notification.VISIBILITY_PUBLIC
            }

            notificationManager.createNotificationChannel(channel)
        }
    }
}

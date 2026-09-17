package com.pws.primaragagym.data.datasource

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageMetadata
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.util.UUID

/**
 * DataSource untuk menangani upload dan manajemen file di Firebase Storage.
 * Mengompres gambar sebelum diunggah untuk menghemat bandwidth dan kapasitas storage.
 */
class FirebaseStorageDataSource(
    private val storage: FirebaseStorage = FirebaseStorage.getInstance()
) {

    /**
     * Upload gambar umum ke Firebase Storage.
     * Mengembalikan URL unduhan HTTPS jika sukses.
     */
    suspend fun uploadImage(
        context: Context,
        imageUri: Uri,
        folder: String,
        prefix: String = "img_"
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val bytes = compressImageToBytes(context, imageUri, maxDimension = 1280, quality = 80)
                ?: return@withContext Result.failure(Exception("Gagal membaca atau memproses gambar dari galeri."))

            val cleanFolder = folder.trim().trim('/')
            val filename = "${prefix}${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(6)}.jpg"
            val storageRef = storage.reference.child("$cleanFolder/$filename")

            val metadata = StorageMetadata.Builder()
                .setContentType("image/jpeg")
                .build()

            storageRef.putBytes(bytes, metadata).await()
            val downloadUrl = storageRef.downloadUrl.await().toString()
            Result.success(downloadUrl)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    /**
     * Upload foto profil member ke folder 'members/profile/'
     */
    suspend fun uploadMemberPhoto(context: Context, imageUri: Uri): Result<String> {
        return uploadImage(context, imageUri, folder = "members/profile", prefix = "member_")
    }

    /**
     * Upload foto profil akun pengguna (admin/staff/superadmin) ke folder 'users/profile/'
     */
    suspend fun uploadUserPhoto(context: Context, imageUri: Uri): Result<String> {
        return uploadImage(context, imageUri, folder = "users/profile", prefix = "user_")
    }

    /**
     * Upload bukti pembayaran transfer/QRIS ke folder 'payments/proofs/'
     */
    suspend fun uploadPaymentProof(context: Context, imageUri: Uri): Result<String> {
        return uploadImage(context, imageUri, folder = "payments/proofs", prefix = "proof_")
    }

    /**
     * Menghapus file di Firebase Storage berdasarkan download URL (opsional).
     */
    suspend fun deleteImageByUrl(downloadUrl: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            if (downloadUrl.startsWith("https://firebasestorage.googleapis.com")) {
                val ref = storage.getReferenceFromUrl(downloadUrl)
                ref.delete().await()
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Mengompresi ukuran gambar dengan membatasi dimensi maksimum dan kompresi JPEG.
     */
    private fun compressImageToBytes(
        context: Context,
        uri: Uri,
        maxDimension: Int = 1280,
        quality: Int = 80
    ): ByteArray? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return null
            val originalBitmap = BitmapFactory.decodeStream(inputStream)
            inputStream.close()
            if (originalBitmap == null) return null

            val width = originalBitmap.width
            val height = originalBitmap.height
            val scaledBitmap = if (width > maxDimension || height > maxDimension) {
                val scale = maxDimension.toFloat() / maxOf(width, height)
                Bitmap.createScaledBitmap(
                    originalBitmap,
                    (width * scale).toInt(),
                    (height * scale).toInt(),
                    true
                )
            } else {
                originalBitmap
            }

            val baos = ByteArrayOutputStream()
            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, quality, baos)
            baos.toByteArray()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}

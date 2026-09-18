package com.pws.primaragagym.data.datasource

import android.util.Log
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.userProfileChangeRequest
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class FirebaseAuthDataSource {

    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()

    val currentUser: FirebaseUser?
        get() = firebaseAuth.currentUser

    val authStateFlow: Flow<FirebaseUser?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { auth ->
            trySend(auth.currentUser)
        }
        firebaseAuth.addAuthStateListener(listener)
        awaitClose { firebaseAuth.removeAuthStateListener(listener) }
    }

    suspend fun signInWithEmailAndPassword(
        email: String,
        password: String
    ): Result<FirebaseUser> {
        return try {
            val result = firebaseAuth.signInWithEmailAndPassword(email, password).await()
            val user = result.user
            if (user != null) {
                Result.success(user)
            } else {
                Result.failure(Exception("Login gagal. Akun tidak ditemukan."))
            }
        } catch (e: Exception) {
            Result.failure(mapFirebaseAuthException(e))
        }
    }

    suspend fun sendPasswordResetEmail(email: String): Result<Unit> {
        val targetEmail = email.trim()
        Log.d("FirebaseAuthDataSource", "Mencoba mengirim link reset password ke: '$targetEmail'")
        return try {
            firebaseAuth.sendPasswordResetEmail(targetEmail).await()
            Log.d("FirebaseAuthDataSource", "Firebase berhasil menerima permintaan reset password untuk: '$targetEmail'")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("FirebaseAuthDataSource", "Gagal mengirim reset password ke '$targetEmail': ${e.message}", e)
            Result.failure(mapResetPasswordException(e))
        }
    }

    suspend fun changePassword(currentPassword: String, newPassword: String): Result<Unit> {
        return try {
            val user = firebaseAuth.currentUser ?: return Result.failure(
                Exception("Tidak ada sesi login aktif. Silakan login kembali.")
            )
            val email = user.email ?: return Result.failure(
                Exception("Email pengguna tidak ditemukan.")
            )

            // Re-authenticate user with current password
            val credential = EmailAuthProvider.getCredential(email, currentPassword)
            user.reauthenticate(credential).await()

            // Update to new password
            user.updatePassword(newPassword).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(mapChangePasswordException(e))
        }
    }

    suspend fun updateProfile(displayName: String, photoUrl: String? = null): Result<Unit> {
        return try {
            val user = firebaseAuth.currentUser ?: return Result.failure(
                Exception("Tidak ada pengguna yang login.")
            )
            val request = userProfileChangeRequest {
                this.displayName = displayName
                photoUrl?.let { this.photoUri = android.net.Uri.parse(it) }
            }
            user.updateProfile(request).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun signOut() {
        firebaseAuth.signOut()
    }

    private fun mapFirebaseAuthException(e: Exception): Exception {
        val message = when {
            e.message?.contains("INVALID_LOGIN_CREDENTIALS", ignoreCase = true) == true ||
            e.message?.contains("wrong-password", ignoreCase = true) == true ||
            e.message?.contains("user-not-found", ignoreCase = true) == true -> {
                "Email atau password salah."
            }
            e.message?.contains("invalid-email", ignoreCase = true) == true -> {
                "Format email tidak valid."
            }
            e.message?.contains("user-disabled", ignoreCase = true) == true -> {
                "Akun ini telah dinonaktifkan."
            }
            e.message?.contains("too-many-requests", ignoreCase = true) == true -> {
                "Terlalu banyak percobaan. Silakan coba lagi nanti."
            }
            e.message?.contains("network", ignoreCase = true) == true -> {
                "Koneksi internet bermasalah. Periksa jaringan Anda."
            }
            e.message?.contains("no user record", ignoreCase = true) == true -> {
                "Email atau password salah."
            }
            else -> e.message ?: "Terjadi kesalahan. Silakan coba lagi."
        }
        return Exception(message)
    }

    private fun mapResetPasswordException(e: Exception): Exception {
        val message = when {
            e.message?.contains("user-not-found", ignoreCase = true) == true ||
            e.message?.contains("no user record", ignoreCase = true) == true -> {
                "Email tidak terdaftar dalam sistem. Pastikan email yang dimasukkan sudah benar."
            }
            e.message?.contains("invalid-email", ignoreCase = true) == true -> {
                "Format email tidak valid."
            }
            e.message?.contains("too-many-requests", ignoreCase = true) == true -> {
                "Terlalu banyak permintaan. Harap tunggu beberapa saat sebelum mencoba lagi."
            }
            e.message?.contains("network", ignoreCase = true) == true -> {
                "Koneksi internet bermasalah. Periksa jaringan Anda."
            }
            else -> e.message ?: "Gagal mengirim link reset password. Silakan coba lagi."
        }
        return Exception(message)
    }

    private fun mapChangePasswordException(e: Exception): Exception {
        val message = when {
            e.message?.contains("INVALID_LOGIN_CREDENTIALS", ignoreCase = true) == true ||
            e.message?.contains("invalid-credential", ignoreCase = true) == true ||
            e.message?.contains("wrong-password", ignoreCase = true) == true -> {
                "Kata sandi saat ini salah."
            }
            e.message?.contains("weak-password", ignoreCase = true) == true -> {
                "Kata sandi baru terlalu lemah. Minimal 6 karakter."
            }
            e.message?.contains("requires-recent-login", ignoreCase = true) == true -> {
                "Demi keamanan, sesi login Anda telah kedaluwarsa. Silakan keluar dan login kembali."
            }
            e.message?.contains("network", ignoreCase = true) == true -> {
                "Koneksi internet bermasalah. Periksa jaringan Anda."
            }
            else -> e.message ?: "Gagal mengubah kata sandi. Silakan coba lagi."
        }
        return Exception(message)
    }
}

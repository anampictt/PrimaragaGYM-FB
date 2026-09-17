package com.pws.primaragagym.ui.components

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.pws.primaragagym.screens.admin.member.MemberColors.CardBackground
import com.pws.primaragagym.screens.admin.member.MemberColors.GreenAccent
import com.pws.primaragagym.screens.admin.member.MemberColors.GreenLight
import com.pws.primaragagym.screens.admin.member.MemberColors.TextMuted
import com.pws.primaragagym.screens.admin.member.MemberColors.TextPrimary
import com.pws.primaragagym.screens.admin.member.MemberColors.TextSecondary
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

import com.pws.primaragagym.di.ServiceLocator
import androidx.compose.ui.text.style.TextAlign

fun normalizeImageUrl(url: String?): String? {
    if (url.isNullOrBlank()) return null
    return url.trim()
}

/**
 * Menyimpan file gambar ke internal storage aplikasi dan mengembalikan URI string (fallback offline).
 */
fun saveImageToInternalStorage(context: Context, uri: Uri, subDir: String, prefix: String = "img_"): String? {
    return try {
        val dir = File(context.filesDir, subDir)
        if (!dir.exists()) {
            dir.mkdirs()
        }
        val filename = "${prefix}${System.currentTimeMillis()}.jpg"
        val destFile = File(dir, filename)

        val inputStream = context.contentResolver.openInputStream(uri) ?: return null
        val originalBitmap = BitmapFactory.decodeStream(inputStream)
        inputStream.close()
        if (originalBitmap != null) {
            val maxDim = 1200
            val width = originalBitmap.width
            val height = originalBitmap.height
            val scaledBitmap = if (width > maxDim || height > maxDim) {
                val scale = maxDim.toFloat() / maxOf(width, height)
                Bitmap.createScaledBitmap(originalBitmap, (width * scale).toInt(), (height * scale).toInt(), true)
            } else {
                originalBitmap
            }
            val fos = FileOutputStream(destFile)
            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 85, fos)
            fos.flush()
            fos.close()
        } else {
            val is2 = context.contentResolver.openInputStream(uri) ?: return null
            val fos = FileOutputStream(destFile)
            is2.copyTo(fos)
            is2.close()
            fos.close()
        }
        "file://" + destFile.absolutePath
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

@Composable
fun UploadBuktiPembayaranField(
    proofUri: Uri? = null,
    onImageSelected: ((Uri?, String?) -> Unit)? = null,
    modifier: Modifier = Modifier,
    proofUrl: String? = null,
    onProofUrlChanged: ((String) -> Unit)? = null
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var isProcessing by remember { mutableStateOf(false) }
    var uploadErrorMessage by remember { mutableStateOf<String?>(null) }
    val storageDataSource = remember { ServiceLocator.firebaseStorageDataSource }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            isProcessing = true
            uploadErrorMessage = null
            scope.launch {
                val uploadResult = storageDataSource.uploadPaymentProof(context, uri)
                isProcessing = false
                uploadResult.onSuccess { downloadUrl ->
                    onImageSelected?.invoke(uri, downloadUrl)
                    onProofUrlChanged?.invoke(downloadUrl)
                }.onFailure { error ->
                    val errorMsg = error.localizedMessage ?: "Gagal mengunggah bukti pembayaran ke Firebase Storage"
                    uploadErrorMessage = errorMsg
                    // Fallback to internal storage if network fails
                    val localPath = withContext(Dispatchers.IO) {
                        saveImageToInternalStorage(context, uri, "payment_proofs", "proof_")
                    } ?: uri.toString()
                    onImageSelected?.invoke(uri, localPath)
                    onProofUrlChanged?.invoke(localPath)
                }
            }
        }
    }

    val hasProof = proofUri != null || !proofUrl.isNullOrBlank()

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "Bukti Pembayaran (Opsional)",
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.Medium
            ),
            color = TextPrimary
        )
        Spacer(modifier = Modifier.height(6.dp))

        if (!hasProof) {
            // Tampilan upload biasa (Klik card untuk pilih gambar dari galeri)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { imagePickerLauncher.launch("image/*") }
                    .border(
                        width = 1.dp,
                        color = Color(0xFFC8E6C9),
                        shape = RoundedCornerShape(10.dp)
                    ),
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = GreenLight.copy(alpha = 0.4f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(GreenLight),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isProcessing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = GreenAccent,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.AddAPhoto,
                                contentDescription = "Upload Bukti Pembayaran",
                                tint = GreenAccent,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isProcessing) "Mengunggah ke Firebase Storage..." else "Upload Foto Bukti Transfer / QRIS",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.SemiBold
                            ),
                            color = GreenAccent
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = if (isProcessing) "Mohon tunggu sejenak..." else "Format JPG, PNG dari galeri",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                }
            }
        } else {
            // Tampilan thumbnail setelah foto dipilih
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = 1.dp,
                        color = GreenAccent,
                        shape = RoundedCornerShape(10.dp)
                    ),
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = CardBackground)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val previewModel: Any? = proofUri ?: proofUrl
                    AsyncImage(
                        model = previewModel,
                        contentDescription = "Bukti Pembayaran",
                        modifier = Modifier
                            .size(60.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.LightGray),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = GreenAccent,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Bukti Pembayaran Terlampir",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.SemiBold
                                ),
                                color = TextPrimary
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = if (proofUrl?.startsWith("http") == true) "Tersimpan di Firebase Storage" else "Foto berhasil dipilih",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextMuted,
                            maxLines = 1
                        )
                    }
                    TextButton(onClick = { imagePickerLauncher.launch("image/*") }) {
                        Text("Ganti", color = GreenAccent)
                    }
                    IconButton(
                        onClick = {
                            onProofUrlChanged?.invoke("")
                            onImageSelected?.invoke(null, null)
                            uploadErrorMessage = null
                        },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Hapus Bukti",
                            tint = Color(0xFFE53935),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }

        if (uploadErrorMessage != null) {
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = uploadErrorMessage!!,
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFFE53935),
                modifier = Modifier.padding(horizontal = 4.dp)
            )
        }
    }
}

/**
 * Upload Foto Member: Cukup lingkaran avatar biasa.
 * Saat diklik, membuka galeri, menyimpan foto ke internal storage sebagai file:///...,
 * lalu menampilkan fotonya di avatar dan mengembalikan path stringnya.
 */
@Composable
fun MemberPhotoUploadSection(
    photoUrl: String,
    onPhotoUrlChanged: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var isProcessing by remember { mutableStateOf(false) }
    var uploadErrorMessage by remember { mutableStateOf<String?>(null) }
    val storageDataSource = remember { ServiceLocator.firebaseStorageDataSource }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            isProcessing = true
            uploadErrorMessage = null
            scope.launch {
                val uploadResult = storageDataSource.uploadMemberPhoto(context, uri)
                isProcessing = false
                uploadResult.onSuccess { downloadUrl ->
                    onPhotoUrlChanged(downloadUrl)
                }.onFailure { error ->
                    val errorMsg = error.localizedMessage ?: "Gagal mengunggah foto profil ke Firebase Storage"
                    uploadErrorMessage = errorMsg
                    val fallbackPath = withContext(Dispatchers.IO) {
                        saveImageToInternalStorage(context, uri, "member_images", "img_")
                    } ?: uri.toString()
                    onPhotoUrlChanged(fallbackPath)
                }
            }
        }
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .border(2.dp, GreenAccent.copy(alpha = 0.6f), CircleShape)
                    .background(GreenLight)
                    .clickable { imagePickerLauncher.launch("image/*") },
                contentAlignment = Alignment.Center
            ) {
                if (isProcessing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(32.dp),
                        color = GreenAccent,
                        strokeWidth = 3.dp
                    )
                } else if (photoUrl.isNotBlank()) {
                    AsyncImage(
                        model = normalizeImageUrl(photoUrl),
                        contentDescription = "Foto Profil Member",
                        modifier = Modifier
                            .size(100.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Filled.AddAPhoto,
                            contentDescription = "Upload Foto",
                            tint = GreenAccent,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Upload Foto",
                            style = MaterialTheme.typography.labelSmall,
                            color = GreenAccent
                        )
                    }
                }
            }

            // Jika foto sudah terpasang, sediakan tombol hapus kecil di sudut atas
            if (photoUrl.isNotBlank()) {
                Box(
                    modifier = Modifier.size(100.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .size(26.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                            .border(1.dp, Color(0xFFE0E0E0), CircleShape)
                            .clickable {
                                onPhotoUrlChanged("")
                                uploadErrorMessage = null
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Hapus Foto",
                            tint = Color(0xFFE53935),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }

        if (uploadErrorMessage != null) {
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = uploadErrorMessage!!,
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFFE53935),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }
    }
}

package com.pws.primaragagym.screens.admin.member.card

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream

object MemberCardImageGenerator {
    private const val CARD_WIDTH_DP = 680
    private const val CARD_HEIGHT_DP = 1020
    private const val IMAGE_QUALITY = 100

    fun generateCardBitmap(
        context: Context,
        data: MemberCardData
    ): Bitmap {
        val density = context.resources.displayMetrics.densityDpi.toFloat()
        val widthPx = (CARD_WIDTH_DP * density / 160f).toInt()
        val heightPx = (CARD_HEIGHT_DP * density / 160f).toInt()

        val bitmap = Bitmap.createBitmap(widthPx, heightPx, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(android.graphics.Color.WHITE)

        val view = ComposeView(context).apply {
            setContent {
                androidx.compose.material3.MaterialTheme {
                    MemberCard(
                        data = data,
                        width = CARD_WIDTH_DP.dp,
                        cornerRadius = 20.dp
                    )
                }
            }
        }

        val listener = object : android.view.ViewTreeObserver.OnPreDrawListener {
            override fun onPreDraw(): Boolean {
                view.measure(
                    android.view.View.MeasureSpec.makeMeasureSpec(widthPx, android.view.View.MeasureSpec.EXACTLY),
                    android.view.View.MeasureSpec.makeMeasureSpec(heightPx, android.view.View.MeasureSpec.EXACTLY)
                )
                view.layout(0, 0, widthPx, heightPx)
                view.draw(canvas)
                view.viewTreeObserver.removeOnPreDrawListener(this)
                return true
            }
        }
        view.viewTreeObserver.addOnPreDrawListener(listener)

        // Force pre draw
        view.post {
            view.viewTreeObserver.dispatchOnPreDraw()
        }

        return bitmap
    }

    fun saveBitmapToFile(
        context: Context,
        bitmap: Bitmap,
        fileName: String
    ): Result<Unit> {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val contentValues = ContentValues().apply {
                    put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
                    put(MediaStore.Images.Media.MIME_TYPE, "image/png")
                    put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/PrimaragaGYM")
                }
                val uri = context.contentResolver.insert(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    contentValues
                ) ?: return Result.failure(Exception("Failed to create MediaStore entry"))

                context.contentResolver.openOutputStream(uri)?.use { stream ->
                    bitmap.compress(Bitmap.CompressFormat.PNG, IMAGE_QUALITY, stream)
                }
            } else {
                @Suppress("DEPRECATION")
                val picturesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                val appDir = File(picturesDir, "PrimaragaGYM")
                if (!appDir.exists()) appDir.mkdirs()

                val file = File(appDir, fileName)
                FileOutputStream(file).use { stream ->
                    bitmap.compress(Bitmap.CompressFormat.PNG, IMAGE_QUALITY, stream)
                }

                val values = ContentValues().apply {
                    put(MediaStore.Images.Media.DATA, file.absolutePath)
                    put(MediaStore.Images.Media.MIME_TYPE, "image/png")
                }
                context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getShareUri(context: Context, bitmap: Bitmap, fileName: String): android.net.Uri? {
        return try {
            val imagesDir = File(context.cacheDir, "images")
            if (!imagesDir.exists()) imagesDir.mkdirs()

            val file = File(imagesDir, fileName)
            FileOutputStream(file).use { stream ->
                bitmap.compress(Bitmap.CompressFormat.PNG, IMAGE_QUALITY, stream)
            }

            FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
        } catch (e: Exception) {
            null
        }
    }
}

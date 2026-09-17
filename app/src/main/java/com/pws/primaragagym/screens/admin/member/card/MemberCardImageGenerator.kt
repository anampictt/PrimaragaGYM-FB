package com.pws.primaragagym.screens.admin.member.card

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Typeface
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.FileProvider
import coil.ImageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import com.pws.primaragagym.ui.components.normalizeImageUrl
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

object MemberCardImageGenerator {
    private const val IMAGE_QUALITY = 100

    // Card dimensions in pixels (high resolution)
    private const val CARD_W = 1200
    private const val CARD_H = 750

    // Colors
    private val COLOR_BG = android.graphics.Color.WHITE
    private val COLOR_GREEN = android.graphics.Color.parseColor("#32A060")
    private val COLOR_GREEN_LIGHT = android.graphics.Color.parseColor("#E8F5E9")
    private val COLOR_TEXT_PRIMARY = android.graphics.Color.parseColor("#1A1A1A")
    private val COLOR_TEXT_SECONDARY = android.graphics.Color.parseColor("#6B6B6B")
    private val COLOR_TEXT_MUTED = android.graphics.Color.parseColor("#9E9E9E")
    private val COLOR_DIVIDER = android.graphics.Color.parseColor("#EEEEEE")
    private val COLOR_CARD_BG = android.graphics.Color.parseColor("#F8F9FA")

    suspend fun generateCardBitmap(
        context: Context,
        data: MemberCardData
    ): Bitmap = withContext(Dispatchers.IO) {
        // Pre-load photo if available
        val photoBitmap: Bitmap? = if (!data.photoUrl.isNullOrBlank()) {
            try {
                val loader = ImageLoader(context)
                val request = ImageRequest.Builder(context)
                    .data(normalizeImageUrl(data.photoUrl))
                    .allowHardware(false)
                    .build()
                val result = loader.execute(request)
                if (result is SuccessResult) {
                    result.drawable.toBitmap()
                } else null
            } catch (e: Exception) {
                null
            }
        } else null

        // Pre-load QR code
        val qrBitmap: Bitmap? = try {
            QrCodeGenerator.generateQrBitmap(data.qrContent)
        } catch (e: Exception) {
            null
        }

        withContext(Dispatchers.Default) {
            drawCard(context, data, photoBitmap, qrBitmap)
        }
    }

    private fun android.graphics.drawable.Drawable.toBitmap(): Bitmap? {
        return try {
            val bmp = Bitmap.createBitmap(intrinsicWidth.coerceAtLeast(1), intrinsicHeight.coerceAtLeast(1), Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bmp)
            setBounds(0, 0, canvas.width, canvas.height)
            draw(canvas)
            bmp
        } catch (e: Exception) {
            null
        }
    }

    private fun drawCard(
        context: Context,
        data: MemberCardData,
        photoBitmap: Bitmap?,
        qrBitmap: Bitmap?
    ): Bitmap {
        val W = CARD_W
        val H = CARD_H
        val bitmap = Bitmap.createBitmap(W, H, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val padding = 60f
        val cornerRadius = 40f

        // ---- Background ----
        val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = COLOR_BG }
        canvas.drawRoundRect(RectF(0f, 0f, W.toFloat(), H.toFloat()), cornerRadius, cornerRadius, bgPaint)

        // ---- Green left strip ----
        val stripW = 10f
        val stripPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = COLOR_GREEN }
        canvas.drawRoundRect(RectF(0f, 0f, stripW, H.toFloat()), cornerRadius, cornerRadius, stripPaint)
        canvas.drawRect(RectF(0f, cornerRadius, stripW, H.toFloat() - cornerRadius), stripPaint)

        // ---- Header: PRIMARAGA GYM ----
        val headerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = COLOR_GREEN
            textSize = 52f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        canvas.drawText("PRIMARAGA GYM", padding + stripW + 20f, padding + 52f, headerPaint)

        // Sub header
        val subHeaderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = COLOR_TEXT_MUTED
            textSize = 28f
        }
        canvas.drawText("KARTU ANGGOTA", padding + stripW + 20f, padding + 90f, subHeaderPaint)

        // ---- Divider line ----
        val dividerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = COLOR_DIVIDER }
        canvas.drawRect(RectF(padding, padding + 110f, W - padding - 260f, padding + 114f), dividerPaint)

        // ---- Avatar circle ----
        val avatarCx = padding + stripW + 20f + 90f
        val avatarCy = padding + 110f + 60f + 90f
        val avatarR = 90f

        val avatarBgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = COLOR_GREEN_LIGHT }
        canvas.drawCircle(avatarCx, avatarCy, avatarR, avatarBgPaint)

        if (photoBitmap != null) {
            // Clip photo into circle
            val circleClipBitmap = Bitmap.createBitmap((avatarR * 2).toInt(), (avatarR * 2).toInt(), Bitmap.Config.ARGB_8888)
            val cc = Canvas(circleClipBitmap)
            val circlePaint = Paint(Paint.ANTI_ALIAS_FLAG)
            cc.drawCircle(avatarR, avatarR, avatarR, circlePaint)
            circlePaint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
            val scaled = Bitmap.createScaledBitmap(photoBitmap, (avatarR * 2).toInt(), (avatarR * 2).toInt(), true)
            cc.drawBitmap(scaled, 0f, 0f, circlePaint)
            canvas.drawBitmap(circleClipBitmap, avatarCx - avatarR, avatarCy - avatarR, null)
        } else {
            // Draw initials
            val initialText = data.avatarInitial.ifBlank { data.name.firstOrNull()?.uppercaseChar()?.toString() ?: "M" }
            val initialPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = COLOR_GREEN
                textSize = 72f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                textAlign = Paint.Align.CENTER
            }
            val textBounds = Rect()
            initialPaint.getTextBounds(initialText, 0, initialText.length, textBounds)
            canvas.drawText(initialText, avatarCx, avatarCy + textBounds.height() / 2f, initialPaint)
        }

        // Avatar border
        val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = android.graphics.Color.argb(76, 0x32, 0xA0, 0x60)
            style = Paint.Style.STROKE
            strokeWidth = 4f
        }
        canvas.drawCircle(avatarCx, avatarCy, avatarR, borderPaint)

        // ---- Member info (right of avatar) ----
        val infoX = avatarCx + avatarR + 36f
        var infoY = avatarCy - avatarR + 20f

        // Name
        val namePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = COLOR_TEXT_PRIMARY
            textSize = 52f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val maxNameWidth = W - infoX - 280f
        var displayName = data.name
        while (namePaint.measureText(displayName) > maxNameWidth && displayName.length > 4) {
            displayName = displayName.dropLast(1)
        }
        if (displayName != data.name) displayName = "${displayName.trimEnd()}..."
        canvas.drawText(displayName, infoX, infoY + 52f, namePaint)
        infoY += 70f

        // Member code
        val codePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = COLOR_GREEN
            textSize = 34f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        canvas.drawText(data.memberCode, infoX, infoY + 34f, codePaint)
        infoY += 52f

        // Status badge
        val statusText = data.status
        val statusBgColor = when (statusText.lowercase()) {
            "aktif", "active" -> android.graphics.Color.parseColor("#E8F5E9")
            "akan habis", "expiring soon" -> android.graphics.Color.parseColor("#FFF3E0")
            "kadaluarsa", "expired" -> android.graphics.Color.parseColor("#FFEBEE")
            else -> android.graphics.Color.parseColor("#F5F5F5")
        }
        val statusTextColor = when (statusText.lowercase()) {
            "aktif", "active" -> android.graphics.Color.parseColor("#4CAF50")
            "akan habis", "expiring soon" -> android.graphics.Color.parseColor("#FF9800")
            "kadaluarsa", "expired" -> android.graphics.Color.parseColor("#F44336")
            else -> android.graphics.Color.parseColor("#9E9E9E")
        }
        val statusPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = statusTextColor
            textSize = 28f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val statusTextBounds = Rect()
        statusPaint.getTextBounds(statusText, 0, statusText.length, statusTextBounds)
        val statusPadH = 20f
        val statusPadV = 10f
        val statusRect = RectF(
            infoX - statusPadH,
            infoY - statusPadV,
            infoX + statusTextBounds.width() + statusPadH,
            infoY + statusTextBounds.height() + statusPadV
        )
        val statusBgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = statusBgColor }
        canvas.drawRoundRect(statusRect, 12f, 12f, statusBgPaint)
        canvas.drawText(statusText, infoX, infoY + statusTextBounds.height(), statusPaint)
        infoY += statusTextBounds.height() + statusPadV * 2 + 20f

        // ---- Plan box ----
        val planBoxTop = avatarCy + avatarR + 30f
        val planBoxLeft = padding + stripW + 20f
        val planBoxRight = W - padding - 260f
        val planBoxBottom = planBoxTop + 56f
        val planBgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = COLOR_GREEN_LIGHT }
        canvas.drawRoundRect(RectF(planBoxLeft, planBoxTop, planBoxRight, planBoxBottom), 12f, 12f, planBgPaint)
        val planPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = COLOR_GREEN
            textSize = 34f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText(data.planName, (planBoxLeft + planBoxRight) / 2f, planBoxTop + 40f, planPaint)

        // ---- Date info box ----
        val dateBoxTop = planBoxBottom + 20f
        val dateBoxBottom = dateBoxTop + 100f
        val dateBgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = COLOR_CARD_BG }
        canvas.drawRoundRect(RectF(planBoxLeft, dateBoxTop, planBoxRight, dateBoxBottom), 12f, 12f, dateBgPaint)

        val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = COLOR_TEXT_MUTED
            textSize = 24f
        }
        val valuePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = COLOR_TEXT_PRIMARY
            textSize = 28f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }

        val midX = (planBoxLeft + planBoxRight) / 2f
        val dateInnerPadding = 24f

        // Left: start date
        canvas.drawText("Mulai Aktif", planBoxLeft + dateInnerPadding, dateBoxTop + 32f, labelPaint)
        canvas.drawText(formatCardDate(data.startDate), planBoxLeft + dateInnerPadding, dateBoxTop + 72f, valuePaint)

        // Vertical separator
        canvas.drawRect(RectF(midX - 1f, dateBoxTop + 16f, midX + 1f, dateBoxBottom - 16f), dividerPaint)

        // Right: expired date
        val expiredLabel = "Berakhir"
        val expiredValue = formatCardDate(data.expiredDate)
        val expiredLabelW = labelPaint.measureText(expiredLabel)
        val expiredValueW = valuePaint.measureText(expiredValue)
        canvas.drawText(expiredLabel, planBoxRight - dateInnerPadding - expiredLabelW, dateBoxTop + 32f, labelPaint)
        canvas.drawText(expiredValue, planBoxRight - dateInnerPadding - expiredValueW, dateBoxTop + 72f, valuePaint)

        // ---- QR Code ----
        val qrSize = 220f
        val qrLeft = W - padding - qrSize
        val qrTop = padding + 100f
        val qrBottom = qrTop + qrSize

        val qrBgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = android.graphics.Color.WHITE }
        canvas.drawRoundRect(RectF(qrLeft - 16f, qrTop - 16f, qrLeft + qrSize + 16f, qrBottom + 16f), 16f, 16f, qrBgPaint)
        val qrBorderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = COLOR_DIVIDER
            style = Paint.Style.STROKE
            strokeWidth = 2f
        }
        canvas.drawRoundRect(RectF(qrLeft - 16f, qrTop - 16f, qrLeft + qrSize + 16f, qrBottom + 16f), 16f, 16f, qrBorderPaint)

        if (qrBitmap != null) {
            val scaledQr = Bitmap.createScaledBitmap(qrBitmap, qrSize.toInt(), qrSize.toInt(), false)
            canvas.drawBitmap(scaledQr, qrLeft, qrTop, null)
        }

        // QR label
        val qrLabelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = COLOR_TEXT_MUTED
            textSize = 22f
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("Scan QR untuk check-in", qrLeft + qrSize / 2f, qrBottom + 32f, qrLabelPaint)

        // ---- Bottom footer ----
        val footerY = H - 40f
        val footerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = COLOR_TEXT_MUTED
            textSize = 24f
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("PRIMARAGA GYM", W / 2f, footerY, footerPaint)

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

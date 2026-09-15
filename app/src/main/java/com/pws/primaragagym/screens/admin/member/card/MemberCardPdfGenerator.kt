package com.pws.primaragagym.screens.admin.member.card

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import android.os.Build
import android.os.Environment
import android.graphics.pdf.PdfDocument
import android.provider.MediaStore
import java.io.File
import java.io.FileOutputStream

object MemberCardPdfGenerator {
    private const val CARD_WIDTH_INCH = 3.4
    private const val CARD_HEIGHT_INCH = 5.1

    private val CardGreen = android.graphics.Color.parseColor("#32A060")
    private val CardTextPrimary = android.graphics.Color.parseColor("#1A1A1A")
    private val CardTextSecondary = android.graphics.Color.parseColor("#6B6B6B")
    private val CardTextMuted = android.graphics.Color.parseColor("#9E9E9E")
    private val CardLightGreen = android.graphics.Color.parseColor("#E8F5E9")
    private val CardWhite = android.graphics.Color.WHITE

    private fun getStatusColor(status: String): Int {
        return when (status.lowercase()) {
            "active" -> android.graphics.Color.parseColor("#4CAF50")
            "expiring soon" -> android.graphics.Color.parseColor("#FF9800")
            "expired" -> android.graphics.Color.parseColor("#F44336")
            else -> android.graphics.Color.parseColor("#9E9E9E")
        }
    }

    private fun getStatusBgColor(status: String): Int {
        return when (status.lowercase()) {
            "active" -> android.graphics.Color.parseColor("#E8F5E9")
            "expiring soon" -> android.graphics.Color.parseColor("#FFF3E0")
            "expired" -> android.graphics.Color.parseColor("#FFEBEE")
            else -> android.graphics.Color.parseColor("#F5F5F5")
        }
    }

    fun generatePdf(
        context: Context,
        data: MemberCardData,
        dpi: Int = 300
    ): Result<ByteArray> {
        return try {
            val qrBitmap = QrCodeGenerator.generateQrBitmap(data.qrContent)
            val pageWidthPx = (CARD_WIDTH_INCH * dpi).toInt()
            val pageHeightPx = (CARD_HEIGHT_INCH * dpi).toInt()

            val pdfDocument = PdfDocument()
            val pageInfo = PdfDocument.PageInfo.Builder(pageWidthPx, pageHeightPx, 1).create()
            val page = pdfDocument.startPage(pageInfo)
            val canvas: Canvas = page.canvas

            val scale = dpi / 72f
            drawCard(canvas, data, qrBitmap, scale, pageWidthPx.toFloat(), pageHeightPx.toFloat())

            pdfDocument.finishPage(page)

            val outputStream = java.io.ByteArrayOutputStream()
            pdfDocument.writeTo(outputStream)
            pdfDocument.close()

            Result.success(outputStream.toByteArray())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun drawCard(
        canvas: Canvas,
        data: MemberCardData,
        qrBitmap: Bitmap,
        scale: Float,
        width: Float,
        height: Float
    ) {
        val padding = 20f * scale
        val cornerRadius = 20f * scale

        // Draw rounded rect background
        val paint = Paint().apply {
            color = CardWhite
            isAntiAlias = true
            style = Paint.Style.FILL
        }
        canvas.drawRoundRect(0f, 0f, width, height, cornerRadius, cornerRadius, paint)

        // Shadow effect
        paint.color = android.graphics.Color.parseColor("#1A000000")
        canvas.drawRoundRect(2f, 2f, width - 2f, height - 2f, cornerRadius, cornerRadius, paint)
        canvas.drawRoundRect(0f, 0f, width, height, cornerRadius, cornerRadius, paint)

        var y = padding + 24f * scale

        // Header
        val titlePaint = Paint().apply {
            color = CardGreen
            isAntiAlias = true
            textSize = 18f * scale
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val titleWidth = titlePaint.measureText("PRIMARAGA GYM")
        canvas.drawText("PRIMARAGA GYM", (width - titleWidth) / 2, y, titlePaint)
        y += 16f * scale

        // Avatar placeholder
        val avatarSize = 72f * scale
        val avatarLeft = padding
        val avatarTop = y
        val avatarPaint = Paint().apply {
            color = CardLightGreen
            style = Paint.Style.FILL
            isAntiAlias = true
        }
        canvas.drawCircle(avatarLeft + avatarSize / 2, avatarTop + avatarSize / 2, avatarSize / 2, avatarPaint)

        // Avatar initials
        val avatarTextPaint = Paint().apply {
            color = CardGreen
            isAntiAlias = true
            textSize = 24f * scale
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText(
            data.avatarInitial,
            avatarLeft + avatarSize / 2,
            avatarTop + avatarSize / 2 + 8f * scale,
            avatarTextPaint
        )

        // Info
        val infoX = avatarLeft + avatarSize + 16f * scale
        val infoTop = avatarTop

        val namePaint = Paint().apply {
            color = CardTextPrimary
            isAntiAlias = true
            textSize = 16f * scale
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        canvas.drawText(data.name, infoX, infoTop + 20f * scale, namePaint)

        val codePaint = Paint().apply {
            color = CardGreen
            isAntiAlias = true
            textSize = 12f * scale
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        }
        canvas.drawText(data.memberCode, infoX, infoTop + 38f * scale, codePaint)

        // Status badge
        val statusPaint = Paint().apply {
            color = getStatusBgColor(data.status)
            style = Paint.Style.FILL
            isAntiAlias = true
        }
        val statusTextPaint = Paint().apply {
            color = getStatusColor(data.status)
            isAntiAlias = true
            textSize = 10f * scale
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val statusTextWidth = statusTextPaint.measureText(data.status)
        val statusPadding = 8f * scale
        val statusTop = infoTop + 48f * scale
        canvas.drawRoundRect(
            infoX, statusTop,
            infoX + statusTextWidth + statusPadding * 2,
            statusTop + 16f * scale,
            6f * scale, 6f * scale, statusPaint
        )
        canvas.drawText(data.status, infoX + statusPadding, statusTop + 12f * scale, statusTextPaint)

        y = avatarTop + avatarSize + 16f * scale

        // Plan badge
        val planTextPaint = Paint().apply {
            color = CardGreen
            isAntiAlias = true
            textSize = 13f * scale
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }
        val planTextWidth = planTextPaint.measureText(data.planName)
        val planBgPaint = Paint().apply {
            color = CardLightGreen
            style = Paint.Style.FILL
            isAntiAlias = true
        }
        canvas.drawRoundRect(
            padding, y,
            width - padding, y + 32f * scale,
            8f * scale, 8f * scale, planBgPaint
        )
        canvas.drawText(data.planName, width / 2, y + 22f * scale, planTextPaint)
        y += 32f * scale + 12f * scale

        // Dates
        val labelPaint = Paint().apply {
            color = CardTextMuted
            isAntiAlias = true
            textSize = 10f * scale
        }
        val valuePaint = Paint().apply {
            color = CardTextPrimary
            isAntiAlias = true
            textSize = 12f * scale
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        }

        canvas.drawText("Aktif", padding, y + 12f * scale, labelPaint)
        canvas.drawText(data.startDate, padding, y + 26f * scale, valuePaint)

        val endLabelPaint = Paint().apply {
            color = CardTextMuted
            isAntiAlias = true
            textSize = 10f * scale
            textAlign = Paint.Align.RIGHT
        }
        val endValuePaint = Paint().apply {
            color = CardTextPrimary
            isAntiAlias = true
            textSize = 12f * scale
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            textAlign = Paint.Align.RIGHT
        }
        canvas.drawText("Berakhir", width - padding, y + 12f * scale, endLabelPaint)
        canvas.drawText(data.expiredDate, width - padding, y + 26f * scale, endValuePaint)
        y += 44f * scale

        // QR Code (Persegi)
        val qrSize = 96f * scale
        val qrX = (width - qrSize) / 2
        val qrBgPaint = Paint().apply {
            color = CardWhite
            style = Paint.Style.FILL
            isAntiAlias = true
        }
        canvas.drawRoundRect(
            qrX - 4f, y,
            qrX + qrSize + 4f, y + qrSize + 4f,
            8f * scale, 8f * scale, qrBgPaint
        )
        val scaledQr = Bitmap.createScaledBitmap(qrBitmap, qrSize.toInt(), qrSize.toInt(), true)
        canvas.drawBitmap(scaledQr, qrX, y, null)
        y += qrSize + 16f * scale

        // Footer
        val footerPaint = Paint().apply {
            color = CardTextMuted
            isAntiAlias = true
            textSize = 10f * scale
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
            letterSpacing = 0.08f
        }
        canvas.drawText("PRIMARAGA GYM", width / 2, y, footerPaint)
    }

    fun savePdfToFile(
        context: Context,
        pdfBytes: ByteArray,
        fileName: String
    ): Result<Unit> {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val contentValues = ContentValues().apply {
                    put(MediaStore.Downloads.DISPLAY_NAME, fileName)
                    put(MediaStore.Downloads.MIME_TYPE, "application/pdf")
                    put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOCUMENTS + "/PrimaragaGYM")
                }
                val uri = context.contentResolver.insert(
                    MediaStore.Downloads.EXTERNAL_CONTENT_URI,
                    contentValues
                ) ?: return Result.failure(Exception("Failed to create MediaStore entry"))

                context.contentResolver.openOutputStream(uri)?.use { stream ->
                    stream.write(pdfBytes)
                }
            } else {
                @Suppress("DEPRECATION")
                val documentsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
                val appDir = File(documentsDir, "PrimaragaGYM")
                if (!appDir.exists()) appDir.mkdirs()

                val file = File(appDir, fileName)
                FileOutputStream(file).use { stream ->
                    stream.write(pdfBytes)
                }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getShareUri(context: Context, pdfBytes: ByteArray, fileName: String): android.net.Uri? {
        return try {
            val pdfsDir = File(context.cacheDir, "pdfs")
            if (!pdfsDir.exists()) pdfsDir.mkdirs()

            val file = File(pdfsDir, fileName)
            FileOutputStream(file).use { stream ->
                stream.write(pdfBytes)
            }

            androidx.core.content.FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
        } catch (e: Exception) {
            null
        }
    }
}

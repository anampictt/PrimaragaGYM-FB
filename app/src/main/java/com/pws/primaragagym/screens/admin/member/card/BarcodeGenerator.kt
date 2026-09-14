package com.pws.primaragagym.screens.admin.member.card

import android.graphics.Bitmap
import android.graphics.Color
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.MultiFormatWriter
import com.google.zxing.qrcode.QRCodeWriter

object BarcodeGenerator {
    private const val DEFAULT_QR_SIZE = 512
    private const val DEFAULT_BARCODE_WIDTH = 600
    private const val DEFAULT_BARCODE_HEIGHT = 160

    fun generateQrBitmap(content: String, size: Int = DEFAULT_QR_SIZE): Bitmap {
        val hints = mapOf(
            EncodeHintType.MARGIN to 1,
            EncodeHintType.CHARACTER_SET to "UTF-8"
        )
        val writer = QRCodeWriter()
        val bitMatrix = writer.encode(content, BarcodeFormat.QR_CODE, size, size, hints)

        val width = bitMatrix.width
        val height = bitMatrix.height
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)

        for (x in 0 until width) {
            for (y in 0 until height) {
                bitmap.setPixel(
                    x, y,
                    if (bitMatrix[x, y]) Color.BLACK else Color.WHITE
                )
            }
        }
        return bitmap
    }

    fun generateBarcode128Bitmap(
        content: String,
        width: Int = DEFAULT_BARCODE_WIDTH,
        height: Int = DEFAULT_BARCODE_HEIGHT
    ): Bitmap {
        val hints = mapOf(
            EncodeHintType.MARGIN to 2,
            EncodeHintType.CHARACTER_SET to "UTF-8"
        )
        val writer = MultiFormatWriter()
        val bitMatrix = writer.encode(content, BarcodeFormat.CODE_128, width, height, hints)

        val actualWidth = bitMatrix.width
        val actualHeight = bitMatrix.height
        val bitmap = Bitmap.createBitmap(actualWidth, actualHeight, Bitmap.Config.RGB_565)

        for (x in 0 until actualWidth) {
            for (y in 0 until actualHeight) {
                bitmap.setPixel(
                    x, y,
                    if (bitMatrix[x, y]) Color.BLACK else Color.WHITE
                )
            }
        }
        return bitmap
    }
}

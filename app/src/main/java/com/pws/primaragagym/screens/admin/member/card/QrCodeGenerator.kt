package com.pws.primaragagym.screens.admin.member.card

import android.graphics.Bitmap
import android.graphics.Color
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter

object QrCodeGenerator {
    fun generateQrBitmap(content: String, size: Int = 512): Bitmap {
        return BarcodeGenerator.generateQrBitmap(content, size)
    }

    fun generateBarcode128Bitmap(content: String, width: Int = 600, height: Int = 160): Bitmap {
        return BarcodeGenerator.generateBarcode128Bitmap(content, width, height)
    }
}

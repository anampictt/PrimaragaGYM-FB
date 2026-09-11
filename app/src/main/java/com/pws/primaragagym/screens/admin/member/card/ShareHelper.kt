package com.pws.primaragagym.screens.admin.member.card

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri

object ShareHelper {

    fun shareImage(context: Context, uri: Uri, memberName: String) {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "image/png"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "Kartu Member - $memberName")
            putExtra(Intent.EXTRA_TEXT, "Kartu Member Primaraga GYM\nNama: $memberName")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(shareIntent, "Bagikan Kartu Member"))
    }

    fun sharePdf(context: Context, uri: Uri, memberName: String) {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "Kartu Member - $memberName")
            putExtra(Intent.EXTRA_TEXT, "Kartu Member Primaraga GYM\nNama: $memberName")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(shareIntent, "Bagikan Kartu Member"))
    }
}

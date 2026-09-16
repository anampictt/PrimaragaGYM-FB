package com.pws.primaragagym.screens.admin.member

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Bundle
import android.os.CancellationSignal
import android.os.ParcelFileDescriptor
import android.print.PageRange
import android.print.PrintAttributes
import android.print.PrintDocumentAdapter
import android.print.PrintDocumentInfo
import android.print.PrintManager
import android.widget.Toast
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.net.URLEncoder
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale

data class InvoiceReceiptData(
    val invoiceNumber: String,
    val memberId: String,
    val memberName: String,
    val memberCode: String,
    val phoneNumber: String,
    val planName: String,
    val duration: String,
    val startDate: String,
    val expiredDate: String,
    val amount: Long,
    val paymentMethod: String,
    val adminName: String = "Admin Kasir",
    val dateStr: String = ""
)

/**
 * Adapter untuk integrasi native Android PrintManager
 */
class PdfPrintDocumentAdapter(private val file: File) : PrintDocumentAdapter() {
    override fun onLayout(
        oldAttributes: PrintAttributes?,
        newAttributes: PrintAttributes?,
        cancellationSignal: CancellationSignal?,
        callback: LayoutResultCallback?,
        extras: Bundle?
    ) {
        if (cancellationSignal?.isCanceled == true) {
            callback?.onLayoutCancelled()
            return
        }
        val info = PrintDocumentInfo.Builder(file.name)
            .setContentType(PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
            .setPageCount(PrintDocumentInfo.PAGE_COUNT_UNKNOWN)
            .build()
        callback?.onLayoutFinished(info, true)
    }

    override fun onWrite(
        pages: Array<out PageRange>?,
        destination: ParcelFileDescriptor?,
        cancellationSignal: CancellationSignal?,
        callback: WriteResultCallback?
    ) {
        try {
            FileInputStream(file).use { input ->
                FileOutputStream(destination?.fileDescriptor).use { output ->
                    input.copyTo(output)
                }
            }
            callback?.onWriteFinished(arrayOf(PageRange.ALL_PAGES))
        } catch (e: Exception) {
            callback?.onWriteFailed(e.message)
        }
    }
}

object InvoiceReceiptHelper {

    private val GreenBrand = Color.parseColor("#32A060")
    private val DarkGreen = Color.parseColor("#236B47")
    private val LightGreen = Color.parseColor("#E8F5E9")
    private val TextPrimary = Color.parseColor("#1A1A1A")
    private val TextSecondary = Color.parseColor("#5A5A5A")
    private val TextMuted = Color.parseColor("#8E8E8E")
    private val BorderColor = Color.parseColor("#E0E0E0")
    private val TableBg = Color.parseColor("#F8F9FA")

    fun formatRupiah(amount: Long): String {
        val formatter = NumberFormat.getNumberInstance(Locale("id", "ID"))
        return "Rp " + formatter.format(amount)
    }

    /**
     * Format string tanggal untuk tampilan ringkas invoice (contoh: "16 Sep 2026").
     * Menghapus jam/WIB jika ada agar muat rapi di dalam kolom tabel invoice.
     */
    fun cleanDate(dateStr: String?): String {
        if (dateStr.isNullOrBlank() || dateStr == "-") return "-"

        var clean = dateStr.trim()
        if (clean.contains(",")) {
            clean = clean.substringBefore(",").trim()
        } else if (clean.contains("T")) {
            clean = clean.substringBefore("T").trim()
        }

        val patterns = listOf(
            "yyyy-MM-dd",
            "dd MMMM yyyy",
            "d MMMM yyyy",
            "dd MMM yyyy",
            "d MMM yyyy",
            "dd-MM-yyyy",
            "dd/MM/yyyy",
            "yyyy/MM/dd"
        )
        val outSdf = SimpleDateFormat("dd MMM yyyy", Locale("id", "ID"))

        for (pattern in patterns) {
            try {
                val sdf = SimpleDateFormat(pattern, Locale("id", "ID"))
                sdf.isLenient = false
                val parsed = sdf.parse(clean)
                if (parsed != null) {
                    return outSdf.format(parsed)
                }
            } catch (_: Exception) {}

            try {
                val sdfEn = SimpleDateFormat(pattern, Locale.ENGLISH)
                sdfEn.isLenient = false
                val parsed = sdfEn.parse(clean)
                if (parsed != null) {
                    return outSdf.format(parsed)
                }
            } catch (_: Exception) {}
        }

        return clean
    }

    /**
     * Membuat dokumen Invoice resmi dalam format A4 (595 x 842 pt)
     */
    fun generateInvoicePdf(context: Context, data: InvoiceReceiptData): ByteArray {
        val pdfDocument = PdfDocument()
        val pageWidth = 595
        val pageHeight = 842
        val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas: Canvas = page.canvas

        // Background putih
        val bgPaint = Paint().apply {
            color = Color.WHITE
            style = Paint.Style.FILL
        }
        canvas.drawRect(0f, 0f, pageWidth.toFloat(), pageHeight.toFloat(), bgPaint)

        // Banner atas warna hijau brand
        val headerBarPaint = Paint().apply {
            color = GreenBrand
            style = Paint.Style.FILL
        }
        canvas.drawRect(0f, 0f, pageWidth.toFloat(), 8f, headerBarPaint)

        val margin = 36f
        var y = 48f

        // Judul Brand
        val titlePaint = Paint().apply {
            color = DarkGreen
            textSize = 22f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }
        canvas.drawText("PRIMARAGA GYM", margin, y, titlePaint)

        // Tulisan INVOICE di kanan atas
        val invTitlePaint = Paint().apply {
            color = TextPrimary
            textSize = 18f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.RIGHT
            isAntiAlias = true
        }
        canvas.drawText("INVOICE PEMBAYARAN", pageWidth - margin, y, invTitlePaint)

        y += 16f
        val subtitlePaint = Paint().apply {
            color = TextSecondary
            textSize = 10f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            isAntiAlias = true
        }
        canvas.drawText("Pusat Kebugaran, Fitnes & Olahraga Modern", margin, y, subtitlePaint)

        val invNumPaint = Paint().apply {
            color = GreenBrand
            textSize = 11f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.RIGHT
            isAntiAlias = true
        }
        canvas.drawText("No: ${data.invoiceNumber}", pageWidth - margin, y, invNumPaint)

        y += 18f
        // Garis pemisah header
        val linePaint = Paint().apply {
            color = BorderColor
            strokeWidth = 1f
            style = Paint.Style.STROKE
        }
        canvas.drawLine(margin, y, pageWidth - margin, y, linePaint)

        y += 24f

        // Grid Informasi 2 Kolom (Ditagihkan Kepada & Info Transaksi)
        val colWidth = (pageWidth - margin * 2 - 20f) / 2
        val col2X = margin + colWidth + 20f

        // Kolom Kiri: Member Info
        val sectionHeaderPaint = Paint().apply {
            color = TextSecondary
            textSize = 10f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }
        canvas.drawText("DITAGIHKAN KEPADA:", margin, y, sectionHeaderPaint)
        canvas.drawText("RINCIAN TRANSAKSI:", col2X, y, sectionHeaderPaint)

        y += 16f
        val memberNamePaint = Paint().apply {
            color = TextPrimary
            textSize = 13f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }
        canvas.drawText(data.memberName, margin, y, memberNamePaint)

        val bodyPaint = Paint().apply {
            color = TextSecondary
            textSize = 10f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            isAntiAlias = true
        }
        canvas.drawText("Tanggal       : ${data.dateStr}", col2X, y, bodyPaint)

        y += 14f
        canvas.drawText("ID Member: ${data.memberCode}", margin, y, bodyPaint)
        canvas.drawText("Metode Bayar  : ${data.paymentMethod}", col2X, y, bodyPaint)

        y += 14f
        canvas.drawText("No. Telepon: ${data.phoneNumber.ifBlank { "-" }}", margin, y, bodyPaint)
        canvas.drawText("Kasir / Admin : ${data.adminName}", col2X, y, bodyPaint)

        y += 14f
        // Status Badge
        val statusBgPaint = Paint().apply {
            color = LightGreen
            style = Paint.Style.FILL
            isAntiAlias = true
        }
        canvas.drawRoundRect(col2X, y - 10f, col2X + 60f, y + 6f, 4f, 4f, statusBgPaint)
        val statusTextPaint = Paint().apply {
            color = DarkGreen
            textSize = 9f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }
        canvas.drawText("LUNAS", col2X + 14f, y + 2f, statusTextPaint)

        y += 28f

        // Tabel Rincian Pembayaran
        val tableTop = y
        val tableWidth = pageWidth - margin * 2

        // Header Tabel
        val tableHeaderBg = Paint().apply {
            color = TableBg
            style = Paint.Style.FILL
        }
        canvas.drawRect(margin, tableTop, pageWidth - margin, tableTop + 24f, tableHeaderBg)
        canvas.drawRect(margin, tableTop, pageWidth - margin, tableTop + 24f, linePaint)

        val thPaint = Paint().apply {
            color = TextPrimary
            textSize = 10f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }
        canvas.drawText("NO", margin + 12f, tableTop + 16f, thPaint)
        canvas.drawText("DESKRIPSI ITEM", margin + 36f, tableTop + 16f, thPaint)
        canvas.drawText("PERIODE AKTIF", margin + 240f, tableTop + 16f, thPaint)

        val thRightPaint = Paint().apply {
            color = TextPrimary
            textSize = 10f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.RIGHT
            isAntiAlias = true
        }
        canvas.drawText("JUMLAH", pageWidth - margin - 12f, tableTop + 16f, thRightPaint)

        // Row 1
        y = tableTop + 24f
        val rowHeight = 44f
        canvas.drawRect(margin, y, pageWidth - margin, y + rowHeight, linePaint)

        // NO
        val tdText = Paint().apply {
            color = TextPrimary
            textSize = 10f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            isAntiAlias = true
        }
        canvas.drawText("1", margin + 12f, y + 25f, tdText)

        // DESKRIPSI ITEM
        val itemNamePaint = Paint().apply {
            color = TextPrimary
            textSize = 10.5f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }
        canvas.drawText("Membership ${data.planName} (${data.duration})", margin + 36f, y + 18f, itemNamePaint)
        val itemSubPaint = Paint().apply {
            color = TextMuted
            textSize = 8.5f
            isAntiAlias = true
        }
        canvas.drawText("Pendaftaran anggota gym baru", margin + 36f, y + 32f, itemSubPaint)

        // PERIODE AKTIF (Ringkas 2 baris agar tidak bertabrakan dengan kolom JUMLAH)
        val startClean = cleanDate(data.startDate)
        val expiredClean = cleanDate(data.expiredDate)

        val periodLabelPaint = Paint().apply {
            color = TextSecondary
            textSize = 9f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            isAntiAlias = true
        }
        val periodValuePaint = Paint().apply {
            color = TextPrimary
            textSize = 9.5f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }
        canvas.drawText("Mulai    : $startClean", margin + 240f, y + 18f, periodLabelPaint)
        canvas.drawText("Berakhir : $expiredClean", margin + 240f, y + 32f, periodValuePaint)

        // JUMLAH (Align Right)
        val priceText = formatRupiah(data.amount)
        val tdRight = Paint().apply {
            color = TextPrimary
            textSize = 11f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.RIGHT
            isAntiAlias = true
        }
        canvas.drawText(priceText, pageWidth - margin - 12f, y + 26f, tdRight)

        y += rowHeight + 16f

        // Total Section (Kanan)
        val totalLabelPaint = Paint().apply {
            color = TextSecondary
            textSize = 10f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            textAlign = Paint.Align.RIGHT
            isAntiAlias = true
        }
        val totalValuePaint = Paint().apply {
            color = DarkGreen
            textSize = 14f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.RIGHT
            isAntiAlias = true
        }

        canvas.drawText("Total Pembayaran:", pageWidth - margin - 120f, y + 10f, totalLabelPaint)
        canvas.drawText(priceText, pageWidth - margin - 12f, y + 10f, totalValuePaint)

        y += 28f

        // Box Catatan
        val noteBgPaint = Paint().apply {
            color = TableBg
            style = Paint.Style.FILL
        }
        canvas.drawRoundRect(margin, y, pageWidth - margin, y + 60f, 6f, 6f, noteBgPaint)
        canvas.drawRoundRect(margin, y, pageWidth - margin, y + 60f, 6f, 6f, linePaint)

        val noteTitlePaint = Paint().apply {
            color = TextPrimary
            textSize = 9.5f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }
        canvas.drawText("CATATAN:", margin + 12f, y + 18f, noteTitlePaint)

        val noteBodyPaint = Paint().apply {
            color = TextSecondary
            textSize = 8.5f
            isAntiAlias = true
        }
        canvas.drawText("• Bukti pembayaran ini sah dan diterbitkan secara digital oleh sistem Primaraga Gym.", margin + 12f, y + 32f, noteBodyPaint)
        canvas.drawText("• Harap tunjukkan kartu member digital atau cetak saat berkunjung untuk akses masuk gym.", margin + 12f, y + 46f, noteBodyPaint)

        // Footer
        val footerY = pageHeight - 40f
        val footerPaint = Paint().apply {
            color = TextMuted
            textSize = 9f
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }
        canvas.drawText("Terima kasih atas kepercayaan Anda berlatih bersama PRIMARAGA GYM", pageWidth / 2f, footerY, footerPaint)

        pdfDocument.finishPage(page)

        val outputStream = java.io.ByteArrayOutputStream()
        pdfDocument.writeTo(outputStream)
        pdfDocument.close()

        return outputStream.toByteArray()
    }

    /**
     * Membuat Nota Kasir format Struk Thermal (384 x 560 pt)
     */
    fun generateReceiptPdf(context: Context, data: InvoiceReceiptData): ByteArray {
        val pdfDocument = PdfDocument()
        val pageWidth = 384
        val pageHeight = 560
        val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas: Canvas = page.canvas

        // Background putih
        val bgPaint = Paint().apply {
            color = Color.WHITE
            style = Paint.Style.FILL
        }
        canvas.drawRect(0f, 0f, pageWidth.toFloat(), pageHeight.toFloat(), bgPaint)

        val margin = 20f
        var y = 32f

        // Header Nota Tengah
        val centerBold = Paint().apply {
            color = TextPrimary
            textSize = 15f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }
        canvas.drawText("PRIMARAGA GYM", pageWidth / 2f, y, centerBold)

        y += 16f
        val centerSub = Paint().apply {
            color = TextSecondary
            textSize = 9f
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }
        canvas.drawText("Pusat Kebugaran Modern", pageWidth / 2f, y, centerSub)

        y += 18f
        val dividerPaint = Paint().apply {
            color = BorderColor
            strokeWidth = 1f
            style = Paint.Style.STROKE
        }
        canvas.drawLine(margin, y, pageWidth - margin, y, dividerPaint)

        y += 16f
        val labelPaint = Paint().apply {
            color = TextSecondary
            textSize = 9.5f
            isAntiAlias = true
        }
        val valuePaint = Paint().apply {
            color = TextPrimary
            textSize = 9.5f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.RIGHT
            isAntiAlias = true
        }

        fun drawReceiptRow(label: String, value: String) {
            canvas.drawText(label, margin, y, labelPaint)
            canvas.drawText(value, pageWidth - margin, y, valuePaint)
            y += 16f
        }

        drawReceiptRow("No. Nota", data.invoiceNumber)
        drawReceiptRow("Waktu", data.dateStr)
        drawReceiptRow("Kasir", data.adminName)
        drawReceiptRow("Member", data.memberName)
        drawReceiptRow("ID Member", data.memberCode)

        y += 4f
        canvas.drawLine(margin, y, pageWidth - margin, y, dividerPaint)
        y += 16f

        // Item
        val itemTitle = Paint().apply {
            color = TextPrimary
            textSize = 10f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }
        canvas.drawText("${data.planName} (${data.duration})", margin, y, itemTitle)
        y += 14f

        val itemCalc = Paint().apply {
            color = TextSecondary
            textSize = 9f
            isAntiAlias = true
        }
        val priceStr = formatRupiah(data.amount)
        canvas.drawText("1x @ $priceStr", margin, y, itemCalc)
        canvas.drawText(priceStr, pageWidth - margin, y, valuePaint)

        y += 14f
        canvas.drawLine(margin, y, pageWidth - margin, y, dividerPaint)
        y += 18f

        // Total
        val totalLabel = Paint().apply {
            color = TextPrimary
            textSize = 11f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }
        val totalVal = Paint().apply {
            color = DarkGreen
            textSize = 13f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.RIGHT
            isAntiAlias = true
        }
        canvas.drawText("TOTAL", margin, y, totalLabel)
        canvas.drawText(priceStr, pageWidth - margin, y, totalVal)

        y += 16f
        drawReceiptRow("Bayar (${data.paymentMethod})", priceStr)
        drawReceiptRow("Status", "LUNAS")

        y += 4f
        canvas.drawLine(margin, y, pageWidth - margin, y, dividerPaint)
        y += 16f

        // Periode
        canvas.drawText("Masa Aktif Membership:", margin, y, labelPaint)
        y += 14f
        val startClean = cleanDate(data.startDate)
        val expiredClean = cleanDate(data.expiredDate)
        val periodPaint = Paint().apply {
            color = TextPrimary
            textSize = 9.5f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }
        canvas.drawText("$startClean s/d $expiredClean", margin, y, periodPaint)

        y += 24f
        canvas.drawLine(margin, y, pageWidth - margin, y, dividerPaint)
        y += 20f

        // Footer Struk
        val footerPaint = Paint().apply {
            color = TextSecondary
            textSize = 8.5f
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }
        canvas.drawText("Terima kasih atas kunjungan Anda!", pageWidth / 2f, y, footerPaint)
        y += 14f
        canvas.drawText("Selamat berlatih & salam sehat bersama kami", pageWidth / 2f, y, footerPaint)

        pdfDocument.finishPage(page)

        val outputStream = java.io.ByteArrayOutputStream()
        pdfDocument.writeTo(outputStream)
        pdfDocument.close()

        return outputStream.toByteArray()
    }

    /**
     * Membuka Android PrintManager dialog untuk mencetak Invoice A4
     */
    fun printInvoice(context: Context, data: InvoiceReceiptData) {
        try {
            val pdfBytes = generateInvoicePdf(context, data)
            val tempFile = File(context.cacheDir, "Invoice_${data.invoiceNumber}.pdf")
            FileOutputStream(tempFile).use { it.write(pdfBytes) }

            val printManager = context.getSystemService(Context.PRINT_SERVICE) as? PrintManager
            if (printManager != null) {
                val jobName = "Invoice_${data.invoiceNumber}"
                printManager.print(jobName, PdfPrintDocumentAdapter(tempFile), PrintAttributes.Builder().build())
            } else {
                Toast.makeText(context, "Layanan cetak printer tidak tersedia di HP ini", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Gagal mencetak invoice: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Membuka Android PrintManager dialog untuk mencetak Struk / Nota Kasir (Thermal)
     */
    fun printReceipt(context: Context, data: InvoiceReceiptData) {
        try {
            val pdfBytes = generateReceiptPdf(context, data)
            val tempFile = File(context.cacheDir, "Nota_${data.invoiceNumber}.pdf")
            FileOutputStream(tempFile).use { it.write(pdfBytes) }

            val printManager = context.getSystemService(Context.PRINT_SERVICE) as? PrintManager
            if (printManager != null) {
                val jobName = "Nota_${data.invoiceNumber}"
                printManager.print(jobName, PdfPrintDocumentAdapter(tempFile), PrintAttributes.Builder().build())
            } else {
                Toast.makeText(context, "Layanan cetak nota tidak tersedia di HP ini", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Gagal mencetak nota: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Membuat Invoice PDF dan langsung membuka WhatsApp member
     * dengan pesan konfirmasi pendaftaran beserta lampiran dokumen PDF.
     */
    fun sendInvoiceToWhatsApp(context: Context, data: InvoiceReceiptData) {
        try {
            val pdfBytes = generateInvoicePdf(context, data)
            val pdfsDir = File(context.cacheDir, "pdfs")
            if (!pdfsDir.exists()) pdfsDir.mkdirs()
            val file = File(pdfsDir, "Invoice_${data.invoiceNumber}.pdf")
            FileOutputStream(file).use { it.write(pdfBytes) }

            val fileUri: Uri = try {
                FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
            } catch (e: Exception) {
                Toast.makeText(context, "Gagal memproses file PDF: ${e.message}", Toast.LENGTH_SHORT).show()
                return
            }

            val digits = data.phoneNumber.filter { it.isDigit() }
            val formattedPhone = when {
                digits.startsWith("0") -> "62" + digits.substring(1)
                digits.startsWith("62") -> digits
                digits.isNotBlank() -> "62$digits"
                else -> ""
            }

            val startClean = cleanDate(data.startDate)
            val expiredClean = cleanDate(data.expiredDate)
            val priceFormatted = formatRupiah(data.amount)
            val message = """
Halo Kak ${data.memberName},
Terima kasih telah mendaftar di *PRIMARAGA GYM*! 🎉💪

Berikut rincian pendaftaran & pembayaran membership Anda:
📄 *No. Invoice:* ${data.invoiceNumber}
👤 *Nama:* ${data.memberName}
🆔 *ID Member:* ${data.memberCode}
🏋️ *Paket:* ${data.planName} (${data.duration})
📅 *Masa Aktif:* $startClean s/d $expiredClean
💰 *Total Bayar:* $priceFormatted (${data.paymentMethod})
✅ *Status:* LUNAS

Invoice digital resmi (PDF) terlampir. Harap disimpan sebagai bukti sah membership Primaraga Gym.

Selamat berlatih dan raih kebugaran maksimal! 🔥
""".trimIndent()

            if (formattedPhone.isBlank()) {
                Toast.makeText(context, "Nomor WhatsApp belum terisi, membuka opsi bagikan...", Toast.LENGTH_SHORT).show()
                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "application/pdf"
                    clipData = android.content.ClipData.newRawUri("", fileUri)
                    putExtra(Intent.EXTRA_STREAM, fileUri)
                    putExtra(Intent.EXTRA_TEXT, message)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                context.startActivity(Intent.createChooser(shareIntent, "Kirim Invoice"))
                return
            }

            // Coba kirim langsung ke WhatsApp
            try {
                val waIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "application/pdf"
                    clipData = android.content.ClipData.newRawUri("", fileUri)
                    putExtra(Intent.EXTRA_STREAM, fileUri)
                    putExtra(Intent.EXTRA_TEXT, message)
                    putExtra("jid", "$formattedPhone@s.whatsapp.net")
                    setPackage("com.whatsapp")
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                context.startActivity(waIntent)
            } catch (e1: Exception) {
                // Coba WhatsApp Business
                try {
                    val waBusinessIntent = Intent(Intent.ACTION_SEND).apply {
                        type = "application/pdf"
                        clipData = android.content.ClipData.newRawUri("", fileUri)
                        putExtra(Intent.EXTRA_STREAM, fileUri)
                        putExtra(Intent.EXTRA_TEXT, message)
                        putExtra("jid", "$formattedPhone@s.whatsapp.net")
                        setPackage("com.whatsapp.w4b")
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                    context.startActivity(waBusinessIntent)
                } catch (e2: Exception) {
                    // Jika intent app tidak tersedia, buka URL WhatsApp
                    try {
                        val encodedMsg = URLEncoder.encode(message, "UTF-8")
                        val waUrl = Uri.parse("https://wa.me/$formattedPhone?text=$encodedMsg")
                        val browserIntent = Intent(Intent.ACTION_VIEW, waUrl)
                        context.startActivity(browserIntent)
                    } catch (e3: Exception) {
                        // Fallback chooser
                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "application/pdf"
                            clipData = android.content.ClipData.newRawUri("", fileUri)
                            putExtra(Intent.EXTRA_STREAM, fileUri)
                            putExtra(Intent.EXTRA_TEXT, message)
                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        }
                        context.startActivity(Intent.createChooser(shareIntent, "Kirim Invoice ke $formattedPhone"))
                    }
                }
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Gagal mengirim ke WhatsApp: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}

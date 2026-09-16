package com.pws.primaragagym.screens.admin.keuangan

import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
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
import com.pws.primaragagym.domain.model.FirestorePayment
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.OutputStreamWriter
import java.nio.charset.StandardCharsets
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class FinancialReportExportData(
    val gymName: String = "PRIMARAGA GYM",
    val title: String = "LAPORAN KEUANGAN & ARUS KAS",
    val periodLabel: String,
    val totalIncome: Long,
    val totalExpense: Long,
    val netProfit: Long,
    val transactions: List<FirestorePayment>,
    val exportDate: Date = Date(),
    val adminName: String = "Admin Kasir"
)

/**
 * PrintDocumentAdapter untuk mencetak dokumen PDF laporan keuangan
 */
class FinancialPdfPrintAdapter(private val file: File) : PrintDocumentAdapter() {
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

object LaporanKeuanganExportHelper {

    fun formatCurrency(amount: Long): String {
        val format = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
        format.maximumFractionDigits = 0
        return format.format(amount)
    }

    private fun getExportDirectory(context: Context): File {
        val dir = File(context.cacheDir, "exports")
        if (!dir.exists()) dir.mkdirs()
        return dir
    }

    /**
     * Menghasilkan Laporan Keuangan dalam format PDF (A4, multi-halaman jika data banyak).
     */
    fun generatePdfReport(context: Context, data: FinancialReportExportData): File {
        val document = PdfDocument()
        val pageWidth = 595 // A4 width in points
        val pageHeight = 842 // A4 height in points
        val marginX = 28f
        val contentWidth = pageWidth - (marginX * 2)

        val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("id", "ID"))
        val dateClean = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(data.exportDate)
        val sortedTransactions = data.transactions.sortedBy { it.paidAt ?: it.createdAt ?: Date(0) }

        // Paints
        val brandPaint = Paint().apply {
            color = Color.parseColor("#1B4332") // Dark Pine Green
            style = Paint.Style.FILL
            isAntiAlias = true
        }

        val brandLightPaint = Paint().apply {
            color = Color.parseColor("#E8F5E9")
            style = Paint.Style.FILL
            isAntiAlias = true
        }

        val textWhite = Paint().apply {
            color = Color.WHITE
            isAntiAlias = true
        }

        val textDark = Paint().apply {
            color = Color.parseColor("#1E293B")
            isAntiAlias = true
        }

        val textMuted = Paint().apply {
            color = Color.parseColor("#64748B")
            isAntiAlias = true
        }

        val incomePaint = Paint().apply {
            color = Color.parseColor("#16A34A")
            isAntiAlias = true
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }

        val expensePaint = Paint().apply {
            color = Color.parseColor("#DC2626")
            isAntiAlias = true
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }

        val profitPaint = Paint().apply {
            color = if (data.netProfit >= 0) Color.parseColor("#2563EB") else Color.parseColor("#DC2626")
            isAntiAlias = true
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }

        val linePaint = Paint().apply {
            color = Color.parseColor("#E2E8F0")
            strokeWidth = 0.8f
            style = Paint.Style.STROKE
            isAntiAlias = true
        }

        val tableHeaderPaint = Paint().apply {
            color = Color.parseColor("#F1F5F9")
            style = Paint.Style.FILL
            isAntiAlias = true
        }

        val zebraPaint = Paint().apply {
            color = Color.parseColor("#F8FAFC")
            style = Paint.Style.FILL
            isAntiAlias = true
        }

        // Pagination setup
        val rowHeight = 22f
        var transactionIndex = 0
        var pageNumber = 1

        // Pre-calculate total pages
        val firstPageUsableHeight = 842f - 245f - 40f // space after headers and summary boxes
        val firstPageRows = (firstPageUsableHeight / rowHeight).toInt().coerceAtLeast(1)
        val remainingRows = (sortedTransactions.size - firstPageRows).coerceAtLeast(0)
        val subsequentPageRows = ((842f - 80f - 40f) / rowHeight).toInt().coerceAtLeast(1)
        val totalPages = 1 + if (remainingRows > 0) Math.ceil(remainingRows.toDouble() / subsequentPageRows).toInt() else 0

        fun drawTableHeader(canvas: Canvas, y: Float) {
            canvas.drawRect(marginX, y, marginX + contentWidth, y + rowHeight, tableHeaderPaint)
            canvas.drawLine(marginX, y, marginX + contentWidth, y, linePaint)
            canvas.drawLine(marginX, y + rowHeight, marginX + contentWidth, y + rowHeight, linePaint)

            val headerTextPaint = Paint().apply {
                color = Color.parseColor("#334155")
                textSize = 7.5f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                isAntiAlias = true
            }

            canvas.drawText("No", marginX + 6f, y + 14f, headerTextPaint)
            canvas.drawText("Waktu", marginX + 26f, y + 14f, headerTextPaint)
            canvas.drawText("No. Invoice", marginX + 90f, y + 14f, headerTextPaint)
            canvas.drawText("Keterangan / Member", marginX + 170f, y + 14f, headerTextPaint)
            canvas.drawText("Kategori", marginX + 325f, y + 14f, headerTextPaint)
            canvas.drawText("Metode", marginX + 400f, y + 14f, headerTextPaint)
            canvas.drawText("Jenis", marginX + 445f, y + 14f, headerTextPaint)

            val headerRightPaint = Paint(headerTextPaint).apply { textAlign = Paint.Align.RIGHT }
            canvas.drawText("Jumlah (Rp)", marginX + contentWidth - 6f, y + 14f, headerRightPaint)
        }

        // --- PAGE 1 ---
        var pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
        var page = document.startPage(pageInfo)
        var canvas = page.canvas

        // Header Banner
        canvas.drawRoundRect(RectF(marginX, 24f, marginX + contentWidth, 88f), 8f, 8f, brandPaint)

        textWhite.textSize = 15f
        textWhite.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText(data.gymName, marginX + 16f, 48f, textWhite)

        textWhite.textSize = 10f
        textWhite.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        canvas.drawText(data.title, marginX + 16f, 64f, textWhite)

        textWhite.textSize = 7.5f
        canvas.drawText("Dicetak: ${dateFormat.format(data.exportDate)} | Admin: ${data.adminName}", marginX + 16f, 78f, textWhite)

        // Periode badge on banner right
        val periodBadgePaint = Paint().apply {
            color = Color.parseColor("#2D6A4F")
            style = Paint.Style.FILL
            isAntiAlias = true
        }
        val periodText = "Periode: ${data.periodLabel}"
        val periodTextPaint = Paint().apply {
            color = Color.WHITE
            textSize = 8.5f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }
        val periodWidth = periodTextPaint.measureText(periodText) + 16f
        val periodRect = RectF(marginX + contentWidth - periodWidth - 14f, 44f, marginX + contentWidth - 14f, 68f)
        canvas.drawRoundRect(periodRect, 6f, 6f, periodBadgePaint)
        canvas.drawText(periodText, periodRect.left + 8f, periodRect.top + 16f, periodTextPaint)

        // 3 Financial Summary Cards
        val cardWidth = (contentWidth - 16f) / 3f
        val cardTop = 98f
        val cardHeight = 52f

        // Card 1: Pemasukan
        val incomeBg = Paint().apply { color = Color.parseColor("#F0FDF4"); style = Paint.Style.FILL }
        val incomeBorder = Paint().apply { color = Color.parseColor("#BBF7D0"); style = Paint.Style.STROKE; strokeWidth = 1f }
        val rect1 = RectF(marginX, cardTop, marginX + cardWidth, cardTop + cardHeight)
        canvas.drawRoundRect(rect1, 6f, 6f, incomeBg)
        canvas.drawRoundRect(rect1, 6f, 6f, incomeBorder)

        textMuted.textSize = 7f
        canvas.drawText("TOTAL PEMASUKAN", rect1.left + 10f, rect1.top + 16f, textMuted)
        incomePaint.textSize = 11f
        canvas.drawText(formatCurrency(data.totalIncome), rect1.left + 10f, rect1.top + 34f, incomePaint)
        textMuted.textSize = 6.5f
        canvas.drawText("Arus Kas Masuk", rect1.left + 10f, rect1.top + 46f, textMuted)

        // Card 2: Pengeluaran
        val expenseBg = Paint().apply { color = Color.parseColor("#FEF2F2"); style = Paint.Style.FILL }
        val expenseBorder = Paint().apply { color = Color.parseColor("#FECACA"); style = Paint.Style.STROKE; strokeWidth = 1f }
        val rect2 = RectF(marginX + cardWidth + 8f, cardTop, marginX + (cardWidth * 2) + 8f, cardTop + cardHeight)
        canvas.drawRoundRect(rect2, 6f, 6f, expenseBg)
        canvas.drawRoundRect(rect2, 6f, 6f, expenseBorder)

        textMuted.textSize = 7f
        canvas.drawText("TOTAL PENGELUARAN", rect2.left + 10f, rect2.top + 16f, textMuted)
        expensePaint.textSize = 11f
        canvas.drawText(formatCurrency(data.totalExpense), rect2.left + 10f, rect2.top + 34f, expensePaint)
        textMuted.textSize = 6.5f
        canvas.drawText("Biaya Operasional", rect2.left + 10f, rect2.top + 46f, textMuted)

        // Card 3: Laba Bersih
        val isSurplus = data.netProfit >= 0
        val profitBg = Paint().apply {
            color = if (isSurplus) Color.parseColor("#EFF6FF") else Color.parseColor("#FEF2F2")
            style = Paint.Style.FILL
        }
        val profitBorder = Paint().apply {
            color = if (isSurplus) Color.parseColor("#BFDBFE") else Color.parseColor("#FECACA")
            style = Paint.Style.STROKE
            strokeWidth = 1f
        }
        val rect3 = RectF(marginX + (cardWidth * 2) + 16f, cardTop, marginX + contentWidth, cardTop + cardHeight)
        canvas.drawRoundRect(rect3, 6f, 6f, profitBg)
        canvas.drawRoundRect(rect3, 6f, 6f, profitBorder)

        textMuted.textSize = 7f
        canvas.drawText("LABA BERSIH (NET)", rect3.left + 10f, rect3.top + 16f, textMuted)
        profitPaint.textSize = 11f
        canvas.drawText(formatCurrency(data.netProfit), rect3.left + 10f, rect3.top + 34f, profitPaint)
        val statusText = if (isSurplus) "Surplus (Untung)" else "Defisit (Rugi)"
        val statusPaint = Paint().apply {
            color = if (isSurplus) Color.parseColor("#1D4ED8") else Color.parseColor("#B91C1C")
            textSize = 6.5f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        canvas.drawText(statusText, rect3.left + 10f, rect3.top + 46f, statusPaint)

        // Section Title: Rincian Transaksi
        val sectionTitlePaint = Paint().apply {
            color = Color.parseColor("#0F172A")
            textSize = 9.5f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }
        canvas.drawText("Rincian Transaksi (${sortedTransactions.size} data)", marginX, 172f, sectionTitlePaint)

        // Draw Table Header
        var currentY = 182f
        drawTableHeader(canvas, currentY)
        currentY += rowHeight

        // Row Paints
        val cellTextPaint = Paint().apply {
            color = Color.parseColor("#1E293B")
            textSize = 7.5f
            isAntiAlias = true
        }
        val cellMutedPaint = Paint().apply {
            color = Color.parseColor("#64748B")
            textSize = 7f
            isAntiAlias = true
        }
        val cellAmountPaint = Paint().apply {
            textSize = 7.5f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.RIGHT
            isAntiAlias = true
        }

        // Draw rows
        while (transactionIndex < sortedTransactions.size) {
            val payment = sortedTransactions[transactionIndex]

            // Check if need new page
            if (currentY + rowHeight > 800f) {
                // Draw footer for current page
                drawPageFooter(canvas, marginX, contentWidth, pageNumber, totalPages)
                document.finishPage(page)

                pageNumber++
                pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
                page = document.startPage(pageInfo)
                canvas = page.canvas

                // Header on subsequent page
                canvas.drawRect(marginX, 24f, marginX + contentWidth, 48f, brandLightPaint)
                val subHeaderPaint = Paint().apply {
                    color = Color.parseColor("#1B4332")
                    textSize = 8.5f
                    typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                    isAntiAlias = true
                }
                canvas.drawText("${data.gymName} - ${data.title} (${data.periodLabel})", marginX + 12f, 39f, subHeaderPaint)

                currentY = 56f
                drawTableHeader(canvas, currentY)
                currentY += rowHeight
            }

            // Alternate zebra background
            if (transactionIndex % 2 == 1) {
                canvas.drawRect(marginX, currentY, marginX + contentWidth, currentY + rowHeight, zebraPaint)
            }
            canvas.drawLine(marginX, currentY + rowHeight, marginX + contentWidth, currentY + rowHeight, linePaint)

            val pDate = payment.paidAt ?: payment.createdAt
            val dateStr = if (pDate != null) dateFormat.format(pDate) else "-"
            val invCode = payment.invoiceNumber.ifBlank { payment.paymentId.take(8).uppercase() }
            val desc = if (payment.transactionType == "EXPENSE") {
                payment.notes.ifBlank { payment.category.ifBlank { "Pengeluaran" } }
            } else {
                if (payment.memberName.isNotBlank()) {
                    "${payment.memberName} (${payment.planName.ifBlank { "Membership" }})"
                } else {
                    payment.notes.ifBlank { payment.category.ifBlank { "Pemasukan" } }
                }
            }
            val shortDesc = if (desc.length > 32) desc.take(29) + "..." else desc
            val category = payment.category.ifBlank { if (payment.transactionType == "EXPENSE") "OPERASIONAL" else "MEMBERSHIP" }
            val shortCategory = if (category.length > 13) category.take(11) + ".." else category

            // Draw cell contents
            canvas.drawText("${transactionIndex + 1}", marginX + 6f, currentY + 14f, cellMutedPaint)
            canvas.drawText(dateStr, marginX + 26f, currentY + 14f, cellMutedPaint)
            canvas.drawText(invCode, marginX + 90f, currentY + 14f, cellTextPaint)
            canvas.drawText(shortDesc, marginX + 170f, currentY + 14f, cellTextPaint)
            canvas.drawText(shortCategory, marginX + 325f, currentY + 14f, cellMutedPaint)
            canvas.drawText(payment.paymentMethod, marginX + 400f, currentY + 14f, cellMutedPaint)

            if (payment.transactionType == "EXPENSE") {
                canvas.drawText("Keluar", marginX + 445f, currentY + 14f, expensePaint)
                cellAmountPaint.color = Color.parseColor("#DC2626")
                canvas.drawText("- ${formatCurrency(payment.amount)}", marginX + contentWidth - 6f, currentY + 14f, cellAmountPaint)
            } else {
                canvas.drawText("Masuk", marginX + 445f, currentY + 14f, incomePaint)
                cellAmountPaint.color = Color.parseColor("#16A34A")
                canvas.drawText("+ ${formatCurrency(payment.amount)}", marginX + contentWidth - 6f, currentY + 14f, cellAmountPaint)
            }

            currentY += rowHeight
            transactionIndex++
        }

        // Draw empty message if no transactions
        if (sortedTransactions.isEmpty()) {
            val emptyPaint = Paint().apply {
                color = Color.parseColor("#94A3B8")
                textSize = 8.5f
                textAlign = Paint.Align.CENTER
                isAntiAlias = true
            }
            canvas.drawText("Tidak ada transaksi tercatat pada periode ini", marginX + (contentWidth / 2), currentY + 30f, emptyPaint)
        }

        // Draw footer on last page
        drawPageFooter(canvas, marginX, contentWidth, pageNumber, totalPages)
        document.finishPage(page)

        // Save PDF file
        val exportsDir = getExportDirectory(context)
        val file = File(exportsDir, "Laporan_Keuangan_${dateClean}.pdf")
        FileOutputStream(file).use { out ->
            document.writeTo(out)
        }
        document.close()

        return file
    }

    private fun drawPageFooter(canvas: Canvas, marginX: Float, contentWidth: Float, currentPage: Int, totalPages: Int) {
        val footerY = 824f
        val footerPaint = Paint().apply {
            color = Color.parseColor("#94A3B8")
            textSize = 7f
            isAntiAlias = true
        }
        val linePaint = Paint().apply {
            color = Color.parseColor("#E2E8F0")
            strokeWidth = 0.5f
        }
        canvas.drawLine(marginX, footerY - 10f, marginX + contentWidth, footerY - 10f, linePaint)
        canvas.drawText("Sistem Keuangan Primaraga Gym | Dokumen Sah", marginX, footerY, footerPaint)

        val pageText = "Halaman $currentPage dari $totalPages"
        val pageTextPaint = Paint(footerPaint).apply { textAlign = Paint.Align.RIGHT }
        canvas.drawText(pageText, marginX + contentWidth, footerY, pageTextPaint)
    }

    /**
     * Menghasilkan Laporan Keuangan dalam format Spreadsheet Excel (.csv dengan UTF-8 BOM & sep=,).
     * Format ini dijamin langsung terbuka rapi dalam kolom-kolom di Microsoft Excel, Google Sheets, dan WPS Office.
     */
    fun generateExcelReport(context: Context, data: FinancialReportExportData): File {
        val exportsDir = getExportDirectory(context)
        val dateClean = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(data.exportDate)
        val file = File(exportsDir, "Laporan_Keuangan_${dateClean}.csv")
        val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("id", "ID"))
        val sortedTransactions = data.transactions.sortedBy { it.paidAt ?: it.createdAt ?: Date(0) }

        FileOutputStream(file).use { fos ->
            // Write UTF-8 BOM so Excel opens with proper encoding
            fos.write(byteArrayOf(0xEF.toByte(), 0xBB.toByte(), 0xBF.toByte()))

            OutputStreamWriter(fos, StandardCharsets.UTF_8).use { writer ->
                // Directive for Microsoft Excel to enforce comma separator
                writer.write("sep=,\n")

                // Header Info
                writer.write("\"${data.gymName}\"\n")
                writer.write("\"${data.title}\"\n")
                writer.write("\"Periode:\",\"${escapeCsv(data.periodLabel)}\"\n")
                writer.write("\"Tanggal Cetak:\",\"${dateFormat.format(data.exportDate)}\"\n")
                writer.write("\"Dicetak Oleh:\",\"${escapeCsv(data.adminName)}\"\n\n")

                // Summary Block
                writer.write("\"=== RINGKASAN EKSEKUTIF ===\"\n")
                writer.write("\"Total Pemasukan:\",${data.totalIncome},\"Rp ${formatNumber(data.totalIncome)}\"\n")
                writer.write("\"Total Pengeluaran:\",${data.totalExpense},\"Rp ${formatNumber(data.totalExpense)}\"\n")
                writer.write("\"Laba Bersih:\",${data.netProfit},\"Rp ${formatNumber(data.netProfit)}\"\n")
                writer.write("\"Status:\",\"${if (data.netProfit >= 0) "Surplus (Untung)" else "Defisit (Rugi)"}\"\n")
                writer.write("\"Total Transaksi:\",${data.transactions.size},\"transaksi\"\n\n")

                // Transaction Table Header
                writer.write("\"=== RINCIAN TRANSAKSI ===\"\n")
                writer.write("\"No\",\"Tanggal & Waktu\",\"No. Invoice\",\"Keterangan / Member\",\"Kategori\",\"Metode Pembayaran\",\"Jenis\",\"Pemasukan (Rp)\",\"Pengeluaran (Rp)\",\"Laba/Rugi (Rp)\"\n")

                var runningBalance = 0L
                sortedTransactions.forEachIndexed { index, p ->
                    val pDate = p.paidAt ?: p.createdAt
                    val dateStr = if (pDate != null) dateFormat.format(pDate) else "-"
                    val invCode = p.invoiceNumber.ifBlank { p.paymentId.take(8).uppercase() }
                    val desc = if (p.transactionType == "EXPENSE") {
                        p.notes.ifBlank { p.category.ifBlank { "Pengeluaran" } }
                    } else {
                        if (p.memberName.isNotBlank()) {
                            "${p.memberName} (${p.planName.ifBlank { "Membership" }})"
                        } else {
                            p.notes.ifBlank { p.category.ifBlank { "Pemasukan" } }
                        }
                    }
                    val cat = p.category.ifBlank { if (p.transactionType == "EXPENSE") "OPERASIONAL" else "MEMBERSHIP" }
                    val isExp = p.transactionType == "EXPENSE"
                    val incomeVal = if (!isExp) p.amount else 0L
                    val expenseVal = if (isExp) p.amount else 0L
                    val netVal = if (!isExp) p.amount else -p.amount
                    runningBalance += netVal

                    writer.write(
                        "${index + 1}," +
                        "\"$dateStr\"," +
                        "\"${escapeCsv(invCode)}\"," +
                        "\"${escapeCsv(desc)}\"," +
                        "\"${escapeCsv(cat)}\"," +
                        "\"${escapeCsv(p.paymentMethod)}\"," +
                        "\"${if (isExp) "Pengeluaran" else "Pemasukan"}\"," +
                        "$incomeVal," +
                        "$expenseVal," +
                        "$netVal\n"
                    )
                }

                // Table Summary Row
                writer.write("\n\"TOTAL\",\"\",\"\",\"\",\"\",\"\",\"\",${data.totalIncome},${data.totalExpense},${data.netProfit}\n")
                writer.write("\"\",\"\",\"\",\"\",\"\",\"\",\"\",\"Rp ${formatNumber(data.totalIncome)}\",\"Rp ${formatNumber(data.totalExpense)}\",\"Rp ${formatNumber(data.netProfit)}\"\n")
            }
        }

        return file
    }

    private fun escapeCsv(text: String): String {
        return text.replace("\"", "\"\"")
    }

    private fun formatNumber(num: Long): String {
        return NumberFormat.getNumberInstance(Locale("id", "ID")).format(num)
    }

    /**
     * Membuka file laporan dengan aplikasi pihak ketiga (PDF Viewer, Microsoft Excel, Google Sheets, WPS).
     */
    fun openFile(context: Context, file: File, mimeType: String) {
        try {
            val uri: Uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, mimeType)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(Intent.createChooser(intent, "Buka Dokumen Laporan"))
        } catch (e: Exception) {
            Toast.makeText(context, "Tidak dapat membuka file: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Membagikan file laporan ke aplikasi lain (WhatsApp, Gmail, Telegram, Google Drive, dll).
     */
    fun shareFile(context: Context, file: File, mimeType: String, subject: String) {
        try {
            val uri: Uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = mimeType
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, subject)
                putExtra(Intent.EXTRA_TEXT, "$subject - Primaraga Gym")
                clipData = ClipData.newRawUri("", uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(intent, "Bagikan Laporan"))
        } catch (e: Exception) {
            Toast.makeText(context, "Gagal membagikan: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Mencetak dokumen PDF melalui Android PrintManager.
     */
    fun printPdf(context: Context, file: File, jobName: String) {
        try {
            val printManager = context.getSystemService(Context.PRINT_SERVICE) as? PrintManager
            if (printManager != null) {
                printManager.print(
                    jobName,
                    FinancialPdfPrintAdapter(file),
                    PrintAttributes.Builder()
                        .setMediaSize(PrintAttributes.MediaSize.ISO_A4)
                        .build()
                )
            } else {
                Toast.makeText(context, "Layanan cetak tidak tersedia di perangkat ini", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Gagal mencetak: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}

package com.pws.primaragagym.commond

import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

object DateUtils {

    private val JAKARTA_TZ: TimeZone = TimeZone.getTimeZone("Asia/Jakarta")
    private val ID_LOCALE: Locale = Locale("id", "ID")

    /**
     * Format waktu terakhir login menjadi tanggal dan jam yang mudah dibaca (misal: "18 Sep 2026, 21:09 WIB").
     * Mampu menangani berbagai tipe data:
     * - Firebase [Timestamp]
     * - [Date]
     * - [Number] (epoch milliseconds / seconds)
     * - String representasi Timestamp, misal "Timestamp(seconds=1789716825, nanoseconds=860000000)"
     * - String epoch angka atau format ISO
     * - String yang sudah terformat sebelumnya
     */
    fun formatLastLogin(raw: Any?, fallback: String = "-"): String {
        if (raw == null) return fallback
        val strRaw = raw.toString().trim()
        if (strRaw.isBlank() || strRaw.equals("null", ignoreCase = true)) return fallback

        // 1. Firebase Timestamp
        if (raw is Timestamp) {
            return formatDateTime(raw.toDate())
        }

        // 2. java.util.Date
        if (raw is Date) {
            return formatDateTime(raw)
        }

        // 3. Number (epoch millis / seconds)
        if (raw is Number) {
            val num = raw.toLong()
            val date = if (num > 1_000_000_000_000L) Date(num) else Date(num * 1000L)
            return formatDateTime(date)
        }

        // 4. String format Timestamp Firestore: "Timestamp(seconds=1789716825, nanoseconds=860000000)"
        val timestampRegex = Regex("""seconds=(\d+)""")
        val match = timestampRegex.find(strRaw)
        if (match != null) {
            val seconds = match.groupValues[1].toLongOrNull()
            if (seconds != null) {
                return formatDateTime(Date(seconds * 1000L))
            }
        }

        // 5. String berupa angka epoch murni
        val digitsOnly = strRaw.toLongOrNull()
        if (digitsOnly != null) {
            val date = if (digitsOnly > 1_000_000_000_000L) Date(digitsOnly) else Date(digitsOnly * 1000L)
            return formatDateTime(date)
        }

        // 6. Jika sudah diformat (mengandung WIB atau kata-kata status)
        if (strRaw.contains("WIB", ignoreCase = true) || strRaw.contains("Baru saja", ignoreCase = true)) {
            return strRaw
        }

        // 7. Coba parsing format ISO / standar
        val patterns = listOf(
            "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
            "yyyy-MM-dd'T'HH:mm:ss'Z'",
            "yyyy-MM-dd'T'HH:mm:ss",
            "yyyy-MM-dd HH:mm:ss",
            "yyyy-MM-dd"
        )
        for (pattern in patterns) {
            try {
                val sdf = SimpleDateFormat(pattern, Locale.US).apply {
                    timeZone = TimeZone.getTimeZone("UTC")
                }
                val parsed = sdf.parse(strRaw)
                if (parsed != null) {
                    return formatDateTime(parsed)
                }
            } catch (_: Exception) {
                // Abaikan jika format tidak cocok
            }
        }

        return strRaw
    }

    /**
     * Memformat objek [Date] menjadi format tanggal dan jam: "dd MMM yyyy, HH:mm 'WIB'".
     * Contoh: "18 Sep 2026, 21:09 WIB"
     */
    fun formatDateTime(date: Date): String {
        val sdf = SimpleDateFormat("dd MMM yyyy, HH:mm 'WIB'", ID_LOCALE).apply {
            timeZone = JAKARTA_TZ
        }
        return sdf.format(date)
    }
}

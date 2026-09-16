package com.pws.primaragagym.domain.model

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

enum class ChatTemplateCategory(
    val code: String,
    val displayName: String,
    val defaultTitle: String
) {
    BIRTHDAY("BIRTHDAY", "Ulang Tahun", "Ucapan Selamat Ulang Tahun"),
    NEVER_CHECKIN("NEVER_CHECKIN", "Belum Check-in", "Sapaan Member Belum Check-in"),
    INACTIVE("INACTIVE", "Belum Perpanjang", "Ajakan Perpanjang Membership"),
    GENERAL("GENERAL", "Pesan Umum", "Pemberitahuan Umum");

    companion object {
        fun fromCode(code: String): ChatTemplateCategory {
            return entries.find { it.code.equals(code, ignoreCase = true) } ?: GENERAL
        }
    }
}

data class FirestoreChatTemplate(
    @DocumentId
    val id: String = "",
    val title: String = "",
    val message: String = "",
    val category: String = ChatTemplateCategory.GENERAL.code,
    val isDefault: Boolean = false,
    @ServerTimestamp
    val createdAt: Date? = null,
    @ServerTimestamp
    val updatedAt: Date? = null
) {
    val categoryEnum: ChatTemplateCategory
        get() = ChatTemplateCategory.fromCode(category)
}

val defaultChatTemplates = listOf(
    FirestoreChatTemplate(
        id = "default_birthday",
        title = "Ucapan Selamat Ulang Tahun",
        category = ChatTemplateCategory.BIRTHDAY.code,
        isDefault = true,
        message = "Halo Kak {nama}, segenap keluarga {gym} mengucapkan Selamat Ulang Tahun! 🎂🎉 Semoga sehat selalu, panjang umur, dan semakin bersemangat berolahraga bersama {gym}! 💪"
    ),
    FirestoreChatTemplate(
        id = "default_never_checkin",
        title = "Sapaan Member Belum Check-in",
        category = ChatTemplateCategory.NEVER_CHECKIN.code,
        isDefault = true,
        message = "Halo Kak {nama}, kami dari tim {gym} melihat Kakak sudah aktif terdaftar tapi belum sempat datang latihan nih. Ada yang bisa kami bantu atau jadwalkan pengenalan alat gym? Kami tunggu kedatangannya ya Kak! 💪🔥"
    ),
    FirestoreChatTemplate(
        id = "default_inactive",
        title = "Ajakan Perpanjang Membership",
        category = ChatTemplateCategory.INACTIVE.code,
        isDefault = true,
        message = "Halo Kak {nama}, kami kangen latihan bareng Kakak di {gym}! Yuk aktifkan kembali membership Kakak dan dapatkan promo perpanjangan menarik hari ini. Ditunggu kedatangannya ya Kak! 🏋️‍♂️"
    ),
    FirestoreChatTemplate(
        id = "default_general",
        title = "Pemberitahuan Member",
        category = ChatTemplateCategory.GENERAL.code,
        isDefault = true,
        message = "Halo Kak {nama}, ada informasi terbaru dari {gym} untuk Kakak. Jangan lupa sempatkan waktu untuk tetap berolahraga hari ini ya! 💪"
    )
)

fun formatChatTemplateMessage(
    template: String,
    memberName: String = "",
    memberCode: String = "",
    age: Int? = null,
    planName: String = "",
    daysRemaining: Int? = null,
    expiredDate: String = "",
    gymName: String = "Primaraga Gym"
): String {
    var result = template
        .replace("{nama}", memberName)
        .replace("{kode}", memberCode)
        .replace("{gym}", gymName)
        .replace("{paket}", planName)
        .replace("{expired}", expiredDate)

    if (age != null) {
        result = result.replace("{usia}", age.toString())
    } else {
        result = result.replace("{usia}", "")
    }

    if (daysRemaining != null) {
        result = result.replace("{sisa_hari}", daysRemaining.toString())
    } else {
        result = result.replace("{sisa_hari}", "")
    }

    return result
}

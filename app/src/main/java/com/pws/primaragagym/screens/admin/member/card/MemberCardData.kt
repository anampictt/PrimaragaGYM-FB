package com.pws.primaragagym.screens.admin.member.card

data class MemberCardData(
    val memberCode: String,
    val name: String,
    val planName: String,
    val startDate: String,
    val expiredDate: String,
    val status: String,
    val avatarInitial: String,
    val qrContent: String,
    val dateOfBirth: String = "",
    val photoUrl: String? = null,
    val phoneNumber: String = ""
)

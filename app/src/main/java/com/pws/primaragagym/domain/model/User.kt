package com.pws.primaragagym.domain.model

data class User(
    val id: String,
    val email: String,
    val name: String,
    val role: UserRole,
    val photoUrl: String? = null,
    val address: String = "",
    val phone: String = "",
    val lastLogin: String = ""
)

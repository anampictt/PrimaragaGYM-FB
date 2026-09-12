package com.pws.primaragagym.domain.model

data class User(
    val id: String,
    val email: String,
    val name: String,
    val role: UserRole,
    val branchId: String? = null,
    val photoUrl: String? = null,
    val address: String = "",
    val phone: String = "",
    val lastLogin: String = "",
    val roleTitle: String = role.displayName
)

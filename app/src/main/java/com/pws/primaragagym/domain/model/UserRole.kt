package com.pws.primaragagym.domain.model

enum class UserRole(val displayName: String) {
    SUPER_ADMIN("Super Admin"),
    ADMIN("Admin"),
    MEMBER("Member");

    companion object {
        fun fromString(value: String): UserRole {
            return entries.find { it.name == value.uppercase() } ?: ADMIN
        }
    }
}

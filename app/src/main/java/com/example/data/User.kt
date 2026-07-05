package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

enum class AccountStatus {
    ACTIVE,
    DISABLED
}

enum class UserRole {
    ADMINISTRATOR,
    DEVELOPER,
    STAFF,
    AUDITOR
}

@Entity(tableName = "users")
data class User(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val name: String,
    val email: String,
    val role: UserRole = UserRole.DEVELOPER,
    val status: AccountStatus = AccountStatus.ACTIVE,
    val createdAt: Long = System.currentTimeMillis()
)

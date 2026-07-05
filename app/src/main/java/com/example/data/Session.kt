package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

enum class SessionStatus {
    ACTIVE,
    REVOKED
}

@Entity(tableName = "sessions")
data class Session(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val userId: String,
    val token: String,
    val refreshToken: String,
    val deviceModel: String,
    val ipAddress: String,
    val createdAt: Long = System.currentTimeMillis(),
    val expiresAt: Long = System.currentTimeMillis() + (1000 * 60 * 60 * 2), // 2 hours
    val status: SessionStatus = SessionStatus.ACTIVE
)

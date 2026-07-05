package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

enum class AuditAction {
    REGISTRATION,
    AUTH_CHALLENGE_GEN,
    AUTH_SUCCESS,
    AUTH_FAILED,
    KEY_UPLOAD,
    KEY_ROTATION,
    KEY_REVOCATION,
    SESSION_REVOKED,
    ACCOUNT_DISABLED,
    ACCOUNT_ENABLED,
    ROLE_CHANGE,
    ADMIN_ACTION
}

@Entity(tableName = "audit_logs")
data class AuditLog(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val action: AuditAction,
    val details: String,
    val userId: String? = null,
    val ipAddress: String = "10.0.2.15",
    val deviceModel: String = "Android Emulator",
    val timestamp: Long = System.currentTimeMillis()
)

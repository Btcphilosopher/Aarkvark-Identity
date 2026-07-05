package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

enum class KeyStatus {
    ACTIVE,
    REVOKED,
    ROTATED
}

enum class TrustLevel {
    TRUSTED,
    UNTRUSTED,
    MARGINAL
}

@Entity(tableName = "public_keys")
data class PublicKeyEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val userId: String,
    val fingerprint: String,
    val publicKeyArmor: String,
    val createdAt: Long = System.currentTimeMillis(),
    val expiresAt: Long = 0L, // 0 means never expires
    val status: KeyStatus = KeyStatus.ACTIVE,
    val trustLevel: TrustLevel = TrustLevel.TRUSTED,
    val isPrimary: Boolean = false
)

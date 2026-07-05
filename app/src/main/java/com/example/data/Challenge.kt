package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

enum class ChallengeStatus {
    PENDING,
    VERIFIED,
    EXPIRED
}

@Entity(tableName = "challenges")
data class Challenge(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val userId: String,
    val nonce: String,
    val createdAt: Long = System.currentTimeMillis(),
    val expiresAt: Long = System.currentTimeMillis() + (1000 * 60 * 5), // 5 minutes expiration
    val status: ChallengeStatus = ChallengeStatus.PENDING
)

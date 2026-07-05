package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "client_keys")
data class ClientKey(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val name: String,
    val privateKeyPKCS8: String, // Encoded private key so we can load it to sign challenges
    val publicKeyArmor: String,
    val fingerprint: String,
    val createdAt: Long = System.currentTimeMillis()
)

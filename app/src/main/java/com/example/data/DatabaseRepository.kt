package com.example.data

import com.example.crypto.CryptoEngine
import kotlinx.coroutines.flow.Flow
import java.util.UUID

class DatabaseRepository(private val appDao: AppDao) {

    val users: Flow<List<User>> = appDao.getAllUsers()
    val publicKeys: Flow<List<PublicKeyEntity>> = appDao.getAllKeys()
    val sessions: Flow<List<Session>> = appDao.getAllSessions()
    val auditLogs: Flow<List<AuditLog>> = appDao.getAllAuditLogs()
    val clientKeys: Flow<List<ClientKey>> = appDao.getAllClientKeys()

    suspend fun createUser(name: String, email: String, role: UserRole): User {
        val user = User(
            id = UUID.randomUUID().toString(),
            name = name,
            email = email,
            role = role,
            status = AccountStatus.ACTIVE
        )
        appDao.insertUser(user)
        
        insertAuditLog(
            AuditAction.REGISTRATION,
            "Created user account: ${user.name} (${user.email}) as role ${user.role}",
            user.id
        )
        return user
    }

    suspend fun updateUserStatus(userId: String, status: AccountStatus) {
        val user = appDao.getUserById(userId) ?: return
        val updated = user.copy(status = status)
        appDao.updateUser(updated)
        
        val action = if (status == AccountStatus.ACTIVE) AuditAction.ACCOUNT_ENABLED else AuditAction.ACCOUNT_DISABLED
        insertAuditLog(
            action,
            "Account ${user.email} status updated to ${status.name}",
            userId
        )
    }

    suspend fun updateUserRole(userId: String, role: UserRole) {
        val user = appDao.getUserById(userId) ?: return
        val updated = user.copy(role = role)
        appDao.updateUser(updated)
        
        insertAuditLog(
            AuditAction.ROLE_CHANGE,
            "Updated role of user ${user.email} to ${role.name}",
            userId
        )
    }

    suspend fun registerPublicKey(
        userId: String,
        fingerprint: String,
        publicKeyArmor: String,
        isPrimary: Boolean = false
    ): PublicKeyEntity {
        val keyEntity = PublicKeyEntity(
            id = UUID.randomUUID().toString(),
            userId = userId,
            fingerprint = fingerprint,
            publicKeyArmor = publicKeyArmor,
            isPrimary = isPrimary
        )
        appDao.insertKey(keyEntity)
        
        insertAuditLog(
            AuditAction.KEY_UPLOAD,
            "Registered public key fingerprint: $fingerprint",
            userId
        )
        return keyEntity
    }

    suspend fun rotatePublicKey(
        userId: String,
        oldKeyId: String,
        newPublicKeyArmor: String,
        newFingerprint: String
    ): PublicKeyEntity {
        val oldKey = appDao.getKeyById(oldKeyId)
        if (oldKey != null) {
            appDao.updateKey(oldKey.copy(status = KeyStatus.ROTATED))
            insertAuditLog(
                AuditAction.KEY_ROTATION,
                "Rotated key fingerprint: ${oldKey.fingerprint}",
                userId
            )
        }
        
        return registerPublicKey(userId, newFingerprint, newPublicKeyArmor, isPrimary = oldKey?.isPrimary ?: false)
    }

    suspend fun revokePublicKey(keyId: String) {
        val key = appDao.getKeyById(keyId) ?: return
        val updated = key.copy(status = KeyStatus.REVOKED)
        appDao.updateKey(updated)
        
        insertAuditLog(
            AuditAction.KEY_REVOCATION,
            "Revoked public key fingerprint: ${key.fingerprint}",
            key.userId
        )
    }

    suspend fun deletePublicKey(keyId: String) {
        val key = appDao.getKeyById(keyId) ?: return
        appDao.deleteKeyById(keyId)
        
        insertAuditLog(
            AuditAction.KEY_REVOCATION,
            "Deleted public key from record: ${key.fingerprint}",
            key.userId
        )
    }

    // Client Keys (Keyring Vault)
    suspend fun registerClientKey(name: String, privateKeyPKCS8: String, publicKeyArmor: String, fingerprint: String): ClientKey {
        val key = ClientKey(
            id = UUID.randomUUID().toString(),
            name = name,
            privateKeyPKCS8 = privateKeyPKCS8,
            publicKeyArmor = publicKeyArmor,
            fingerprint = fingerprint
        )
        appDao.insertClientKey(key)
        return key
    }

    suspend fun deleteClientKey(id: String) {
        appDao.deleteClientKeyById(id)
    }

    // Challenge-Response
    suspend fun createChallenge(userId: String): Challenge {
        val nonce = CryptoEngine.generateSecureChallenge()
        val challenge = Challenge(
            id = UUID.randomUUID().toString(),
            userId = userId,
            nonce = nonce
        )
        appDao.insertChallenge(challenge)
        
        insertAuditLog(
            AuditAction.AUTH_CHALLENGE_GEN,
            "Generated login challenge (nonce: ${nonce.take(12)}...)",
            userId
        )
        return challenge
    }

    suspend fun verifyChallenge(challengeId: String, signatureArmor: String): Session? {
        val challenge = appDao.getChallengeById(challengeId) ?: return null
        if (challenge.status != ChallengeStatus.PENDING) return null
        if (challenge.expiresAt < System.currentTimeMillis()) {
            appDao.updateChallenge(challenge.copy(status = ChallengeStatus.EXPIRED))
            insertAuditLog(
                AuditAction.AUTH_FAILED,
                "Authentication failed: challenge expired",
                challenge.userId
            )
            return null
        }

        val user = appDao.getUserById(challenge.userId)
        if (user == null || user.status == AccountStatus.DISABLED) {
            insertAuditLog(
                AuditAction.AUTH_FAILED,
                "Authentication failed: user does not exist or is disabled",
                challenge.userId
            )
            return null
        }

        val activeKeys = appDao.getActiveKeysByUserIdSync(challenge.userId)
        if (activeKeys.isEmpty()) {
            insertAuditLog(
                AuditAction.AUTH_FAILED,
                "Authentication failed: no active public keys registered for user",
                challenge.userId
            )
            return null
        }

        var verifiedKey: PublicKeyEntity? = null
        for (key in activeKeys) {
            val isValid = CryptoEngine.verifyChallengeSignature(
                challengeText = challenge.nonce,
                signatureArmor = signatureArmor,
                publicKeyArmor = key.publicKeyArmor
            )
            if (isValid) {
                verifiedKey = key
                break
            }
        }

        if (verifiedKey != null) {
            // Mark challenge as verified
            appDao.updateChallenge(challenge.copy(status = ChallengeStatus.VERIFIED))

            // Generate JWT and session
            val token = CryptoEngine.generateSimulatedJWT(user.id, user.role.name, verifiedKey.fingerprint)
            val session = Session(
                id = UUID.randomUUID().toString(),
                userId = user.id,
                token = token,
                refreshToken = UUID.randomUUID().toString(),
                deviceModel = "Android Emulator Key Vault",
                ipAddress = "10.0.2.15"
            )
            appDao.insertSession(session)

            insertAuditLog(
                AuditAction.AUTH_SUCCESS,
                "Successful passwordless authentication using key fingerprint ${verifiedKey.fingerprint}",
                user.id
            )
            return session
        } else {
            insertAuditLog(
                AuditAction.AUTH_FAILED,
                "Authentication failed: signature verification failed for all active keys",
                user.id
            )
            return null
        }
    }

    suspend fun revokeSession(sessionId: String) {
        val session = appDao.getSessionById(sessionId) ?: return
        appDao.deleteSessionById(sessionId)
        insertAuditLog(
            AuditAction.SESSION_REVOKED,
            "Session revoked (Device: ${session.deviceModel}, IP: ${session.ipAddress})",
            session.userId
        )
    }

    suspend fun insertAuditLog(action: AuditAction, details: String, userId: String? = null) {
        val log = AuditLog(
            id = UUID.randomUUID().toString(),
            action = action,
            details = details,
            userId = userId
        )
        appDao.insertAuditLog(log)
    }
}

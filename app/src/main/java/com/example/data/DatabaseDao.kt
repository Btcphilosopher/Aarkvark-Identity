package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface AppDao {
    // Users
    @Query("SELECT * FROM users ORDER BY createdAt DESC")
    fun getAllUsers(): Flow<List<User>>

    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    suspend fun getUserById(id: String): User?

    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): User?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: User)

    @Update
    suspend fun updateUser(user: User)

    // Public Keys
    @Query("SELECT * FROM public_keys ORDER BY createdAt DESC")
    fun getAllKeys(): Flow<List<PublicKeyEntity>>

    @Query("SELECT * FROM public_keys WHERE userId = :userId ORDER BY createdAt DESC")
    fun getKeysByUserId(userId: String): Flow<List<PublicKeyEntity>>

    @Query("SELECT * FROM public_keys WHERE userId = :userId AND status = 'ACTIVE'")
    suspend fun getActiveKeysByUserIdSync(userId: String): List<PublicKeyEntity>

    @Query("SELECT * FROM public_keys WHERE id = :id LIMIT 1")
    suspend fun getKeyById(id: String): PublicKeyEntity?

    @Query("SELECT * FROM public_keys WHERE fingerprint = :fingerprint LIMIT 1")
    suspend fun getKeyByFingerprint(fingerprint: String): PublicKeyEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertKey(key: PublicKeyEntity)

    @Update
    suspend fun updateKey(key: PublicKeyEntity)

    @Query("DELETE FROM public_keys WHERE id = :id")
    suspend fun deleteKeyById(id: String)

    // Sessions
    @Query("SELECT * FROM sessions ORDER BY createdAt DESC")
    fun getAllSessions(): Flow<List<Session>>

    @Query("SELECT * FROM sessions WHERE userId = :userId ORDER BY createdAt DESC")
    fun getSessionsByUserId(userId: String): Flow<List<Session>>

    @Query("SELECT * FROM sessions WHERE id = :id LIMIT 1")
    suspend fun getSessionById(id: String): Session?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: Session)

    @Update
    suspend fun updateSession(session: Session)

    @Query("DELETE FROM sessions WHERE id = :id")
    suspend fun deleteSessionById(id: String)

    // Audit Logs
    @Query("SELECT * FROM audit_logs ORDER BY timestamp DESC")
    fun getAllAuditLogs(): Flow<List<AuditLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAuditLog(log: AuditLog)

    // Challenges
    @Query("SELECT * FROM challenges WHERE id = :id LIMIT 1")
    suspend fun getChallengeById(id: String): Challenge?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChallenge(challenge: Challenge)

    @Update
    suspend fun updateChallenge(challenge: Challenge)

    // Client Keys (Local Keyring Vault)
    @Query("SELECT * FROM client_keys ORDER BY createdAt DESC")
    fun getAllClientKeys(): Flow<List<ClientKey>>

    @Query("SELECT * FROM client_keys WHERE id = :id LIMIT 1")
    suspend fun getClientKeyById(id: String): ClientKey?

    @Query("SELECT * FROM client_keys WHERE fingerprint = :fingerprint LIMIT 1")
    suspend fun getClientKeyByFingerprint(fingerprint: String): ClientKey?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertClientKey(key: ClientKey)

    @Query("DELETE FROM client_keys WHERE id = :id")
    suspend fun deleteClientKeyById(id: String)
}

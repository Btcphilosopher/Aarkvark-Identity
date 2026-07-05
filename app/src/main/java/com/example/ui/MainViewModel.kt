package com.example.ui

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.crypto.CryptoEngine
import com.example.data.AccountStatus
import com.example.data.AppDatabase
import com.example.data.AuditAction
import com.example.data.ClientKey
import com.example.data.DatabaseRepository
import com.example.data.PublicKeyEntity
import com.example.data.Session
import com.example.data.User
import com.example.data.UserRole
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class AppScreen {
    VAULT,      // Client PGP Keyring
    GATEWAY,    // Challenge-Response Auth Sandbox
    ADMIN       // Server Administration & Audit logs
}

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: DatabaseRepository

    init {
        val database = AppDatabase.getDatabase(application)
        repository = DatabaseRepository(database.appDao())
        seedInitialData()
    }

    // Flows from Repository
    val users: StateFlow<List<User>> = repository.users
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val publicKeys: StateFlow<List<PublicKeyEntity>> = repository.publicKeys
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val sessions: StateFlow<List<Session>> = repository.sessions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val auditLogs: StateFlow<List<com.example.data.AuditLog>> = repository.auditLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val clientKeys: StateFlow<List<ClientKey>> = repository.clientKeys
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // UI state
    var selectedScreen by mutableStateOf(AppScreen.VAULT)
    
    // Auth Challenge Simulation State
    var activeChallenge by mutableStateOf<com.example.data.Challenge?>(null)
    var authenticationSuccessSession by mutableStateOf<Session?>(null)
    var authenticationFailureReason by mutableStateOf<String?>(null)
    var selectedSigningKeyId by mutableStateOf<String>("")
    var isAuthenticating by mutableStateOf(false)

    // Methods
    fun generateNewClientKey(name: String) {
        viewModelScope.launch {
            try {
                val keyPair = CryptoEngine.generateKeyPair()
                val privateKeyPKCS8 = android.util.Base64.encodeToString(keyPair.private.encoded, android.util.Base64.NO_WRAP)
                val publicKeyArmor = CryptoEngine.exportPublicKeyToArmor(keyPair.public)
                val fingerprint = CryptoEngine.computeFingerprint(keyPair.public)
                
                repository.registerClientKey(name, privateKeyPKCS8, publicKeyArmor, fingerprint)
                repository.insertAuditLog(
                    AuditAction.ADMIN_ACTION,
                    "Generated local client-side OpenPGP-compatible RSA key: $name ($fingerprint)"
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun deleteClientKey(keyId: String) {
        viewModelScope.launch {
            repository.deleteClientKey(keyId)
        }
    }

    fun registerKeyOnServer(userId: String, clientKey: ClientKey) {
        viewModelScope.launch {
            repository.registerPublicKey(
                userId = userId,
                fingerprint = clientKey.fingerprint,
                publicKeyArmor = clientKey.publicKeyArmor,
                isPrimary = true
            )
        }
    }

    fun createServerUser(name: String, email: String, role: UserRole) {
        viewModelScope.launch {
            repository.createUser(name, email, role)
        }
    }

    fun updateUserStatus(userId: String, status: AccountStatus) {
        viewModelScope.launch {
            repository.updateUserStatus(userId, status)
        }
    }

    fun updateUserRole(userId: String, role: UserRole) {
        viewModelScope.launch {
            repository.updateUserRole(userId, role)
        }
    }

    fun revokeServerKey(keyId: String) {
        viewModelScope.launch {
            repository.revokePublicKey(keyId)
        }
    }

    fun deleteServerKey(keyId: String) {
        viewModelScope.launch {
            repository.deletePublicKey(keyId)
        }
    }

    fun revokeServerSession(sessionId: String) {
        viewModelScope.launch {
            repository.revokeSession(sessionId)
        }
    }

    // Trigger an auth challenge for a specific server user
    fun initiateAuthChallenge(userId: String) {
        viewModelScope.launch {
            isAuthenticating = true
            authenticationSuccessSession = null
            authenticationFailureReason = null
            
            // Create challenge on the server
            val challenge = repository.createChallenge(userId)
            activeChallenge = challenge
        }
    }

    // Sign and verify challenge
    fun signAndVerifyChallenge(clientKey: ClientKey) {
        viewModelScope.launch {
            val challenge = activeChallenge ?: return@launch
            
            try {
                // Restore private key from saved PKCS#8 format
                val keyFactory = java.security.KeyFactory.getInstance("RSA")
                val decodedKeyBytes = android.util.Base64.decode(clientKey.privateKeyPKCS8, android.util.Base64.DEFAULT)
                val keySpec = java.security.spec.PKCS8EncodedKeySpec(decodedKeyBytes)
                val privateKey = keyFactory.generatePrivate(keySpec)
                
                // Sign locally (At no point should private keys leave the user's device)
                val signatureArmor = CryptoEngine.signChallenge(challenge.nonce, privateKey)
                
                // Submit signature to server and verify
                val session = repository.verifyChallenge(challenge.id, signatureArmor)
                
                if (session != null) {
                    authenticationSuccessSession = session
                    authenticationFailureReason = null
                } else {
                    authenticationFailureReason = "Cryptographic signature verification failed or user account is disabled."
                }
            } catch (e: Exception) {
                e.printStackTrace()
                authenticationFailureReason = "Error restoring cryptographic keys: ${e.localizedMessage}"
            } finally {
                activeChallenge = null
                isAuthenticating = false
            }
        }
    }

    fun clearChallengeState() {
        activeChallenge = null
        authenticationSuccessSession = null
        authenticationFailureReason = null
        isAuthenticating = false
    }

    private fun seedInitialData() {
        viewModelScope.launch {
            val existingUsers = repository.users.first()
            if (existingUsers.isEmpty()) {
                // 1. Create default server users
                val devUser = repository.createUser("Thomas Anderson", "neo@aardvark.io", UserRole.DEVELOPER)
                val adminUser = repository.createUser("Morpheus Security", "morpheus@aardvark.io", UserRole.ADMINISTRATOR)
                val auditorUser = repository.createUser("Trinity Audit", "trinity@aardvark.io", UserRole.AUDITOR)

                // 2. Generate a default local client key
                val keyPair = CryptoEngine.generateKeyPair()
                val privateKeyPKCS8 = android.util.Base64.encodeToString(keyPair.private.encoded, android.util.Base64.NO_WRAP)
                val publicKeyArmor = CryptoEngine.exportPublicKeyToArmor(keyPair.public)
                val fingerprint = CryptoEngine.computeFingerprint(keyPair.public)

                val clientKey = repository.registerClientKey(
                    name = "MacBook Pro Primary Key",
                    privateKeyPKCS8 = privateKeyPKCS8,
                    publicKeyArmor = publicKeyArmor,
                    fingerprint = fingerprint
                )

                // 3. Register public key on server for developer "neo@aardvark.io"
                repository.registerPublicKey(
                    userId = devUser.id,
                    fingerprint = fingerprint,
                    publicKeyArmor = publicKeyArmor,
                    isPrimary = true
                )

                // 4. Generate some historic audit logs to make the dashboard look like a real system
                repository.insertAuditLog(
                    AuditAction.REGISTRATION,
                    "Automated system bootstrapping completed successfully.",
                    null
                )
                repository.insertAuditLog(
                    AuditAction.REGISTRATION,
                    "Created system group 'Administrators' and assigned permissions.",
                    null
                )
                repository.insertAuditLog(
                    AuditAction.ADMIN_ACTION,
                    "Configured OpenPGP challenge expiration threshold to 300 seconds.",
                    adminUser.id
                )
                repository.insertAuditLog(
                    AuditAction.KEY_UPLOAD,
                    "Auto-enrolled root auditing PGP key block.",
                    auditorUser.id
                )
            }
        }
    }
}

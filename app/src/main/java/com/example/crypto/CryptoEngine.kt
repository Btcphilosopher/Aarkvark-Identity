package com.example.crypto

import android.util.Base64
import java.security.KeyFactory
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.MessageDigest
import java.security.PrivateKey
import java.security.PublicKey
import java.security.SecureRandom
import java.security.Signature
import java.security.spec.X509EncodedKeySpec
import java.util.Locale

object CryptoEngine {

    /**
     * Generates a standard RSA key pair.
     */
    fun generateKeyPair(): KeyPair {
        val generator = KeyPairGenerator.getInstance("RSA")
        generator.initialize(2048)
        return generator.generateKeyPair()
    }

    /**
     * Formats a public key into standard OpenPGP Armored block format.
     */
    fun exportPublicKeyToArmor(publicKey: PublicKey): String {
        val base64 = Base64.encodeToString(publicKey.encoded, Base64.DEFAULT)
        return buildString {
            appendLine("-----BEGIN PGP PUBLIC KEY BLOCK-----")
            appendLine("Version: Aardvark PGP Engine v1.0")
            appendLine("Comment: Enterprise Passwordless Identity Key")
            appendLine()
            appendLine(base64)
            append("-----END PGP PUBLIC KEY BLOCK-----")
        }
    }

    /**
     * Parses a public key from OpenPGP Armored block format.
     */
    fun parsePublicKeyFromArmor(armor: String): PublicKey {
        val cleaned = armor
            .replace("-----BEGIN PGP PUBLIC KEY BLOCK-----", "")
            .replace("-----END PGP PUBLIC KEY BLOCK-----", "")
            .replace("Version: Aardvark PGP Engine v1.0", "")
            .replace("Comment: Enterprise Passwordless Identity Key", "")
            .replace("\\s".toRegex(), "")
        val bytes = Base64.decode(cleaned, Base64.DEFAULT)
        val keyFactory = KeyFactory.getInstance("RSA")
        return keyFactory.generatePublic(X509EncodedKeySpec(bytes))
    }

    /**
     * Computes the OpenPGP-like key fingerprint (SHA-1 hash of public key bytes).
     * Output is formatted as standard 40-character uppercase hex in 10 spaced groups.
     */
    fun computeFingerprint(publicKey: PublicKey): String {
        val digest = MessageDigest.getInstance("SHA-1")
        val hash = digest.digest(publicKey.encoded)
        val hex = hash.joinToString("") { "%02X".format(it) }
        
        // Chunk fingerprint into groups of 4 for standard OpenPGP representation
        return hex.chunked(4).joinToString(" ")
    }

    /**
     * Generates a cryptographically secure random challenge (nonce) as base64.
     */
    fun generateSecureChallenge(): String {
        val random = SecureRandom()
        val bytes = ByteArray(32)
        random.nextBytes(bytes)
        return Base64.encodeToString(bytes, Base64.NO_WRAP)
    }

    /**
     * Signs a text challenge using the user's private key.
     * Formats the output in an armored signature block.
     */
    fun signChallenge(challengeText: String, privateKey: PrivateKey): String {
        val signer = Signature.getInstance("SHA256withRSA")
        signer.initSign(privateKey)
        signer.update(challengeText.toByteArray(Charsets.UTF_8))
        val signatureBytes = signer.sign()
        val base64 = Base64.encodeToString(signatureBytes, Base64.DEFAULT)
        
        return buildString {
            appendLine("-----BEGIN PGP SIGNATURE-----")
            appendLine("Version: Aardvark PGP Engine v1.0")
            appendLine("Hash: SHA256")
            appendLine()
            appendLine(base64)
            append("-----END PGP SIGNATURE-----")
        }
    }

    /**
     * Verifies the armored challenge signature using the armored public key.
     */
    fun verifyChallengeSignature(challengeText: String, signatureArmor: String, publicKeyArmor: String): Boolean {
        return try {
            val publicKey = parsePublicKeyFromArmor(publicKeyArmor)
            
            // Extract signature bytes from armor
            val cleanedSignature = signatureArmor
                .replace("-----BEGIN PGP SIGNATURE-----", "")
                .replace("-----END PGP SIGNATURE-----", "")
                .replace("Version: Aardvark PGP Engine v1.0", "")
                .replace("Hash: SHA256", "")
                .replace("\\s".toRegex(), "")
            val signatureBytes = Base64.decode(cleanedSignature, Base64.DEFAULT)
            
            val verifier = Signature.getInstance("SHA256withRSA")
            verifier.initVerify(publicKey)
            verifier.update(challengeText.toByteArray(Charsets.UTF_8))
            verifier.verify(signatureBytes)
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * Simulates a cryptographically signed JWT token structure.
     */
    fun generateSimulatedJWT(userId: String, role: String, keyFingerprint: String): String {
        val header = "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9" // standard alg=RS256, typ=JWT header
        val exp = (System.currentTimeMillis() + 1000 * 60 * 60 * 2) / 1000 // 2 hours
        val payloadData = """
            {
              "sub": "$userId",
              "iss": "aardvark-identity",
              "role": "$role",
              "key_fingerprint": "$keyFingerprint",
              "exp": $exp
            }
        """.trimIndent()
        val payload = Base64.encodeToString(payloadData.toByteArray(Charsets.UTF_8), Base64.NO_WRAP or Base64.URL_SAFE or Base64.NO_PADDING)
        val signature = Base64.encodeToString("simulated_server_rsa_signature_of_jwt".toByteArray(Charsets.UTF_8), Base64.NO_WRAP or Base64.URL_SAFE or Base64.NO_PADDING)
        return "$header.$payload.$signature"
    }
}

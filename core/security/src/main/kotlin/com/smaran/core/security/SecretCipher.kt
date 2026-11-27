package com.smaran.core.security

/** Encrypts and decrypts small secrets, such as other keys. */
interface SecretCipher {
    fun encrypt(plain: ByteArray): ByteArray
    fun decrypt(sealed: ByteArray): ByteArray
}

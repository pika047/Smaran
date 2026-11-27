package com.smaran.core.security

/** Supplies the secret used to open the encrypted database. */
interface DatabasePassphraseProvider {
    fun getPassphrase(): ByteArray
}

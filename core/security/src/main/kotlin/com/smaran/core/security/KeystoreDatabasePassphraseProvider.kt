package com.smaran.core.security

import android.content.Context
import androidx.core.util.AtomicFile
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.io.IOException
import java.security.SecureRandom
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Creates a random 256-bit database passphrase on first use and stores it
 * encrypted by a Keystore key, in a folder Android never backs up.
 */
@Singleton
class KeystoreDatabasePassphraseProvider @Inject constructor(
    @ApplicationContext context: Context,
) : DatabasePassphraseProvider {

    private val cipher: SecretCipher = KeystoreAesGcmCipher(KEY_ALIAS)
    private val file = AtomicFile(File(context.noBackupFilesDir, FILE_NAME))

    @Synchronized
    override fun getPassphrase(): ByteArray =
        if (file.baseFile.exists()) cipher.decrypt(file.readFully()) else createAndStore()

    private fun createAndStore(): ByteArray {
        val passphrase = ByteArray(PASSPHRASE_BYTES).also { SecureRandom().nextBytes(it) }
        val sealed = cipher.encrypt(passphrase)
        val out = file.startWrite()
        try {
            out.write(sealed)
            file.finishWrite(out)
        } catch (e: IOException) {
            file.failWrite(out)
            throw e
        }
        return passphrase
    }

    private companion object {
        const val KEY_ALIAS = "smaran_db_key_wrapper"
        const val FILE_NAME = "db_passphrase.bin"
        const val PASSPHRASE_BYTES = 32
    }
}

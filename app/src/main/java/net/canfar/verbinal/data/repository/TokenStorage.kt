package net.canfar.verbinal.data.repository

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
import java.security.GeneralSecurityException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TokenStorage
@Inject
constructor(
    @ApplicationContext private val context: Context,
) {
    private val masterKey =
        MasterKey
            .Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

    private val prefs: SharedPreferences by lazy {
        try {
            createEncryptedPrefs()
        } catch (e: GeneralSecurityException) {
            Log.w("TokenStorage", "Encrypted prefs corrupted, resetting", e)
            context.deleteSharedPreferences(PREFS_FILE)
            createEncryptedPrefs()
        }
    }

    private fun createEncryptedPrefs(): SharedPreferences = EncryptedSharedPreferences.create(
        context,
        PREFS_FILE,
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
    )

    fun saveToken(
        token: String,
        username: String,
    ) {
        prefs
            .edit()
            .putString(KEY_TOKEN, token)
            .putString(KEY_USERNAME, username)
            .apply()
    }

    fun loadToken(): Pair<String?, String?> = readSafely {
        val token = prefs.getString(KEY_TOKEN, null)
        val username = prefs.getString(KEY_USERNAME, null)
        token to username
    } ?: (null to null)

    fun saveCredentials(
        username: String,
        password: String,
    ) {
        prefs
            .edit()
            .putString(KEY_CRED_USERNAME, username)
            .putString(KEY_CRED_PASSWORD, password)
            .apply()
    }

    fun loadCredentials(): Pair<String?, String?> = readSafely {
        val username = prefs.getString(KEY_CRED_USERNAME, null)
        val password = prefs.getString(KEY_CRED_PASSWORD, null)
        username to password
    } ?: (null to null)

    fun hasStoredSession(): Boolean = readSafely {
        prefs.getString(KEY_CRED_USERNAME, null) != null
    } ?: false

    fun clearAll() {
        prefs
            .edit()
            .remove(KEY_TOKEN)
            .remove(KEY_USERNAME)
            .remove(KEY_CRED_USERNAME)
            .remove(KEY_CRED_PASSWORD)
            .apply()
    }

    fun clearToken() {
        prefs
            .edit()
            .remove(KEY_TOKEN)
            .remove(KEY_USERNAME)
            .apply()
    }

    private fun <T> readSafely(block: () -> T): T? = try {
        block()
    } catch (e: GeneralSecurityException) {
        Log.w("TokenStorage", "Failed to decrypt stored data, clearing", e)
        context.deleteSharedPreferences(PREFS_FILE)
        null
    }

    companion object {
        private const val PREFS_FILE = "verbinal_secure_prefs"
        private const val KEY_TOKEN = "auth_token"
        private const val KEY_USERNAME = "username"
        private const val KEY_CRED_USERNAME = "cred_username"
        private const val KEY_CRED_PASSWORD = "cred_password"
    }
}

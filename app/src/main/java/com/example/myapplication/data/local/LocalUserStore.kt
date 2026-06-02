package com.example.myapplication.data.local

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.example.myapplication.data.model.CaregiverProfile
import java.util.concurrent.atomic.AtomicReference

class LocalUserStore(context: Context) {

    private val appContext = context.applicationContext
    private val preferences = createPreferences(appContext)
    private val cachedAuthToken = AtomicReference(readAuthToken(preferences))

    fun loadProfile(defaultProfile: CaregiverProfile): CaregiverProfile {
        return defaultProfile.copy(
            fullName = preferences.readString(ProfileKeys.fullName, defaultProfile.fullName),
            phone = preferences.readString(ProfileKeys.phone, defaultProfile.phone),
            email = preferences.readString(ProfileKeys.email, defaultProfile.email),
            familyLabel = preferences.readString(ProfileKeys.familyLabel, defaultProfile.familyLabel),
            address = preferences.readString(ProfileKeys.address, defaultProfile.address),
            emergencyContact = preferences.readString(ProfileKeys.emergencyContact, defaultProfile.emergencyContact),
            avatarSeed = preferences.getInt(ProfileKeys.avatarSeed, defaultProfile.avatarSeed),
            avatarUri = preferences.readString(ProfileKeys.avatarUri, defaultProfile.avatarUri),
            bio = preferences.readString(ProfileKeys.bio, defaultProfile.bio)
        )
    }

    fun saveProfile(profile: CaregiverProfile) {
        preferences.edit {
            putTrimmedString(ProfileKeys.fullName, profile.fullName)
            putTrimmedString(ProfileKeys.phone, profile.phone)
            putTrimmedString(ProfileKeys.email, profile.email)
            putTrimmedString(ProfileKeys.familyLabel, profile.familyLabel)
            putTrimmedString(ProfileKeys.address, profile.address)
            putTrimmedString(ProfileKeys.emergencyContact, profile.emergencyContact)
            putInt(ProfileKeys.avatarSeed, profile.avatarSeed)
            putTrimmedString(ProfileKeys.avatarUri, profile.avatarUri, preserveBlank = true)
            putTrimmedString(ProfileKeys.bio, profile.bio, preserveBlank = true)
        }
    }

    fun loadRegistrationState(defaultValue: Boolean = false): Boolean {
        return preferences.getBoolean(SessionKeys.isRegistered, defaultValue)
    }

    fun saveRegistrationState(isRegistered: Boolean) {
        preferences.edit(commit = true) {
            putBoolean(SessionKeys.isRegistered, isRegistered)
        }
    }

    fun loadAuthToken(): String? {
        return cachedAuthToken.get()
    }

    fun saveAuthToken(token: String) {
        val sanitizedToken = token.trim()
        if (sanitizedToken.isBlank()) {
            clearAuthToken()
            return
        }

        cachedAuthToken.set(sanitizedToken)
        preferences.edit(commit = true) {
            putString(SessionKeys.authToken, sanitizedToken)
        }
    }

    fun clearAuthToken() {
        cachedAuthToken.set(null)
        preferences.edit(commit = true) {
            remove(SessionKeys.authToken)
        }
    }

    fun loadBackendBaseUrl(defaultValue: String): String {
        return preferences.readString(ConfigKeys.backendBaseUrl, defaultValue)
    }

    fun saveBackendBaseUrl(baseUrl: String) {
        val sanitizedBaseUrl = baseUrl.trim()
        if (sanitizedBaseUrl.isBlank()) return

        preferences.edit(commit = true) {
            putString(ConfigKeys.backendBaseUrl, sanitizedBaseUrl)
        }
    }

    private companion object {
        const val PREFS_NAME = "family_care_user_store"
        const val ENCRYPTED_PREFS_NAME = "family_care_user_store_secure"

        object ProfileKeys {
            const val fullName = "full_name"
            const val phone = "phone"
            const val email = "email"
            const val familyLabel = "family_label"
            const val address = "address"
            const val emergencyContact = "emergency_contact"
            const val avatarSeed = "avatar_seed"
            const val avatarUri = "avatar_uri"
            const val bio = "bio"
        }

        object SessionKeys {
            const val isRegistered = "is_registered"
            const val authToken = "auth_token"
        }

        object ConfigKeys {
            const val backendBaseUrl = "backend_base_url"
        }
    }

    private fun createPreferences(context: Context): SharedPreferences {
        val legacyPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        return runCatching {
            val masterKey = MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()

            EncryptedSharedPreferences.create(
                context,
                ENCRYPTED_PREFS_NAME,
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            ).also { securePreferences ->
                migrateLegacyPreferencesIfNeeded(
                    legacyPreferences = legacyPreferences,
                    securePreferences = securePreferences
                )
            }
        }.getOrElse {
            legacyPreferences
        }
    }

    private fun migrateLegacyPreferencesIfNeeded(
        legacyPreferences: SharedPreferences,
        securePreferences: SharedPreferences
    ) {
        if (legacyPreferences.all.isEmpty() || securePreferences.all.isNotEmpty()) return

        securePreferences.edit(commit = true) {
            legacyPreferences.all.forEach { (key, value) ->
                when (value) {
                    is String -> putString(key, value)
                    is Int -> putInt(key, value)
                    is Boolean -> putBoolean(key, value)
                    is Float -> putFloat(key, value)
                    is Long -> putLong(key, value)
                    is Set<*> -> {
                        @Suppress("UNCHECKED_CAST")
                        putStringSet(key, value.filterIsInstance<String>().toSet())
                    }
                }
            }
        }

        legacyPreferences.edit(commit = true) {
            clear()
        }
    }

    private fun readAuthToken(preferences: SharedPreferences): String? {
        return preferences.getString(SessionKeys.authToken, null)
            ?.trim()
            ?.takeIf { it.isNotEmpty() }
    }

    private fun SharedPreferences.readString(key: String, defaultValue: String): String {
        return getString(key, null)
            ?.trim()
            ?.takeIf { it.isNotEmpty() || defaultValue.isEmpty() }
            ?: defaultValue
    }

    private inline fun SharedPreferences.edit(
        commit: Boolean = false,
        block: SharedPreferences.Editor.() -> Unit
    ) {
        val editor = edit()
        editor.block()
        if (commit) {
            editor.commit()
        } else {
            editor.apply()
        }
    }

    private fun SharedPreferences.Editor.putTrimmedString(
        key: String,
        value: String?,
        preserveBlank: Boolean = false
    ) {
        val sanitizedValue = value?.trim().orEmpty()
        when {
            sanitizedValue.isNotEmpty() -> putString(key, sanitizedValue)
            preserveBlank -> putString(key, "")
            else -> remove(key)
        }
    }
}

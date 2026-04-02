package com.example.myapplication.data.local

import android.content.Context
import com.example.myapplication.data.model.CaregiverProfile

class LocalUserStore(context: Context) {

    private val preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun loadProfile(defaultProfile: CaregiverProfile): CaregiverProfile {
        return defaultProfile.copy(
            fullName = preferences.getString(KEY_FULL_NAME, defaultProfile.fullName) ?: defaultProfile.fullName,
            phone = preferences.getString(KEY_PHONE, defaultProfile.phone) ?: defaultProfile.phone,
            email = preferences.getString(KEY_EMAIL, defaultProfile.email) ?: defaultProfile.email,
            familyLabel = preferences.getString(KEY_FAMILY_LABEL, defaultProfile.familyLabel) ?: defaultProfile.familyLabel,
            address = preferences.getString(KEY_ADDRESS, defaultProfile.address) ?: defaultProfile.address,
            emergencyContact = preferences.getString(KEY_EMERGENCY_CONTACT, defaultProfile.emergencyContact)
                ?: defaultProfile.emergencyContact,
            avatarSeed = preferences.getInt(KEY_AVATAR_SEED, defaultProfile.avatarSeed),
            avatarUri = preferences.getString(KEY_AVATAR_URI, defaultProfile.avatarUri) ?: defaultProfile.avatarUri,
            bio = preferences.getString(KEY_BIO, defaultProfile.bio) ?: defaultProfile.bio
        )
    }

    fun saveProfile(profile: CaregiverProfile) {
        preferences.edit()
            .putString(KEY_FULL_NAME, profile.fullName)
            .putString(KEY_PHONE, profile.phone)
            .putString(KEY_EMAIL, profile.email)
            .putString(KEY_FAMILY_LABEL, profile.familyLabel)
            .putString(KEY_ADDRESS, profile.address)
            .putString(KEY_EMERGENCY_CONTACT, profile.emergencyContact)
            .putInt(KEY_AVATAR_SEED, profile.avatarSeed)
            .putString(KEY_AVATAR_URI, profile.avatarUri)
            .putString(KEY_BIO, profile.bio)
            .apply()
    }

    fun loadRegistrationState(defaultValue: Boolean = false): Boolean {
        return preferences.getBoolean(KEY_IS_REGISTERED, defaultValue)
    }

    fun saveRegistrationState(isRegistered: Boolean) {
        preferences.edit()
            .putBoolean(KEY_IS_REGISTERED, isRegistered)
            .apply()
    }

    private companion object {
        const val PREFS_NAME = "family_care_user_store"
        const val KEY_FULL_NAME = "full_name"
        const val KEY_PHONE = "phone"
        const val KEY_EMAIL = "email"
        const val KEY_FAMILY_LABEL = "family_label"
        const val KEY_ADDRESS = "address"
        const val KEY_EMERGENCY_CONTACT = "emergency_contact"
        const val KEY_AVATAR_SEED = "avatar_seed"
        const val KEY_AVATAR_URI = "avatar_uri"
        const val KEY_BIO = "bio"
        const val KEY_IS_REGISTERED = "is_registered"
    }
}

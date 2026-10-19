package com.example.data.local

import android.content.Context
import android.content.SharedPreferences

class UserPreferences(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("abhi_ai_friend_prefs", Context.MODE_PRIVATE)

    var userName: String
        get() = prefs.getString(KEY_USER_NAME, "Friend") ?: "Friend"
        set(value) = prefs.edit().putString(KEY_USER_NAME, value).apply()

    var activeCompanionId: String
        get() = prefs.getString(KEY_COMPANION_ID, "abhi") ?: "abhi"
        set(value) = prefs.edit().putString(KEY_COMPANION_ID, value).apply()

    var activePersonalityId: String
        get() = prefs.getString(KEY_PERSONALITY_ID, "best_friend") ?: "best_friend"
        set(value) = prefs.edit().putString(KEY_PERSONALITY_ID, value).apply()

    var activeLanguageCode: String
        get() = prefs.getString(KEY_LANGUAGE_CODE, "en") ?: "en"
        set(value) = prefs.edit().putString(KEY_LANGUAGE_CODE, value).apply()

    var isAutoTtsEnabled: Boolean
        get() = prefs.getBoolean(KEY_AUTO_TTS, true)
        set(value) = prefs.edit().putBoolean(KEY_AUTO_TTS, value).apply()

    var voicePitch: Float
        get() = prefs.getFloat(KEY_VOICE_PITCH, 1.0f)
        set(value) = prefs.edit().putFloat(KEY_VOICE_PITCH, value).apply()

    var voiceSpeechRate: Float
        get() = prefs.getFloat(KEY_VOICE_RATE, 1.0f)
        set(value) = prefs.edit().putFloat(KEY_VOICE_RATE, value).apply()

    var friendshipXp: Int
        get() = prefs.getInt(KEY_FRIENDSHIP_XP, 25)
        set(value) = prefs.edit().putInt(KEY_FRIENDSHIP_XP, value).apply()

    var streakDays: Int
        get() = prefs.getInt(KEY_STREAK_DAYS, 3)
        set(value) = prefs.edit().putInt(KEY_STREAK_DAYS, value).apply()

    var userAvatarIndex: Int
        get() = prefs.getInt(KEY_USER_AVATAR, 0)
        set(value) = prefs.edit().putInt(KEY_USER_AVATAR, value).apply()

    companion object {
        private const val KEY_USER_NAME = "user_name"
        private const val KEY_COMPANION_ID = "active_companion_id"
        private const val KEY_PERSONALITY_ID = "active_personality_id"
        private const val KEY_LANGUAGE_CODE = "active_language_code"
        private const val KEY_AUTO_TTS = "auto_tts_enabled"
        private const val KEY_VOICE_PITCH = "voice_pitch"
        private const val KEY_VOICE_RATE = "voice_speech_rate"
        private const val KEY_FRIENDSHIP_XP = "friendship_xp"
        private const val KEY_STREAK_DAYS = "streak_days"
        private const val KEY_USER_AVATAR = "user_avatar_index"
    }
}

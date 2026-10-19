package com.example.data.model

import androidx.annotation.DrawableRes
import com.example.R

enum class CompanionType(
    val id: String,
    val displayName: String,
    val roleTitle: String,
    val gender: String,
    val description: String,
    val greetingEn: String,
    val greetingTe: String,
    val greetingHi: String,
    val defaultPitch: Float,
    val defaultSpeechRate: Float,
    @DrawableRes val avatarRes: Int
) {
    ABHI(
        id = "abhi",
        displayName = "Abhi",
        roleTitle = "Smart AI Guy",
        gender = "Boy",
        description = "Energetic, witty, tech-savvy, and your loyal best bro. Always ready for gaming, deep talks, or study motivation!",
        greetingEn = "Hey there! I'm Abhi, your AI buddy. What's on your mind today? Let's chat, learn, or have some fun!",
        greetingTe = "నమస్కారం! నేను మీ స్నేహితుడు అభిని. ఈ రోజు విశేషాలు ఏమిటి? మాట్లాడుకుందామా!",
        greetingHi = "अरे दोस्त! मैं अभि हूँ, आपका AI साथी। आज क्या चल रहा है? चलिए बातें करते हैं या कुछ नया सीखते हैं!",
        defaultPitch = 0.95f,
        defaultSpeechRate = 1.0f,
        avatarRes = R.drawable.avatar_boy_1790960714507
    ),
    MAYA(
        id = "maya",
        displayName = "Maya",
        roleTitle = "Caring AI Girl",
        gender = "Girl",
        description = "Empathetic, cheerful, brilliant, and supportive bestie. Loves heart-to-heart chats, creative ideas, and cheering you up!",
        greetingEn = "Hi! I'm Maya, your companion. I'm so glad you're here! How was your day? Tell me everything!",
        greetingTe = "హాయ్! నేను మాయ. మీతో మాట్లాడటం నాకు చాలా సంతోషంగా ఉంది! ఈ రోజు మీ రోజు ఎలా గడిచింది?",
        greetingHi = "नमस्ते! मैं माया हूँ। आपसे मिलकर बहुत ख़ुशी हुई! आपका दिन कैसा रहा? मुझे सब बताइए!",
        defaultPitch = 1.25f,
        defaultSpeechRate = 1.05f,
        avatarRes = R.drawable.avatar_girl_1790960727578
    );

    fun getGreeting(lang: AppLanguage): String {
        return when (lang) {
            AppLanguage.ENGLISH -> greetingEn
            AppLanguage.TELUGU -> greetingTe
            AppLanguage.HINDI -> greetingHi
        }
    }
}

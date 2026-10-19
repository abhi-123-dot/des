package com.example.data.model

import java.util.Locale

enum class AppLanguage(
    val code: String,
    val displayName: String,
    val nativeName: String,
    val locale: Locale,
    val promptInstruction: String
) {
    ENGLISH(
        code = "en",
        displayName = "English",
        nativeName = "English",
        locale = Locale.US,
        promptInstruction = "Respond naturally and fluently in modern English."
    ),
    TELUGU(
        code = "te",
        displayName = "Telugu",
        nativeName = "తెలుగు",
        locale = Locale("te", "IN"),
        promptInstruction = "Respond naturally in Telugu script (తెలుగులో మాత్రమే సమాధానం ఇవ్వండి). Keep tone friendly and colloquial."
    ),
    HINDI(
        code = "hi",
        displayName = "Hindi",
        nativeName = "हिंदी",
        locale = Locale("hi", "IN"),
        promptInstruction = "Respond naturally in Hindi script (देवनागरी हिंदी में ही उत्तर दें). Keep tone warm, respectful, and friendly."
    );

    companion object {
        fun fromCode(code: String): AppLanguage {
            return entries.find { it.code == code } ?: ENGLISH
        }
    }
}

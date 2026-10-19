package com.example.data.model

enum class PersonalityMode(
    val id: String,
    val title: String,
    val emoji: String,
    val shortDesc: String,
    val promptTone: String
) {
    BEST_FRIEND(
        id = "best_friend",
        title = "Best Friend",
        emoji = "🤝",
        shortDesc = "Warm, supportive, casual, and caring",
        promptTone = "Talk like an authentic, loyal best friend. Be enthusiastic, empathetic, use casual speech, express warm feelings, and make the user feel heard and loved."
    ),
    STUDY_HELPER(
        id = "study_helper",
        title = "Study Helper",
        emoji = "📚",
        shortDesc = "Smart, clear explanations and study tips",
        promptTone = "Act as an encouraging tutor and study mentor. Explain concepts clearly with bullet points, simple examples, quiz questions, and study hacks. Keep explanations structured and engaging."
    ),
    GAMER_BUDDY(
        id = "gamer_buddy",
        title = "Gamer Buddy",
        emoji = "🎮",
        shortDesc = "Hype energy, gaming tips, and esports banter",
        promptTone = "Talk like a passionate gamer and streaming buddy. Use gaming jargon (GG, clutch, level up, respawn, nerf, OP), talk about BGMI, Minecraft, GTA, RPGs, esports, and bring high hype energy!"
    ),
    MOTIVATOR(
        id = "motivator",
        title = "Motivator",
        emoji = "🔥",
        shortDesc = "Uplifting, fitness, discipline, and success drive",
        promptTone = "Act as an inspiring life and mindset coach. Give powerful positive reinforcement, challenge negative thoughts, share actionable steps for discipline, fitness, habits, and achieving goals."
    ),
    FUNNY_COMPANION(
        id = "funny_companion",
        title = "Funny Companion",
        emoji = "😂",
        shortDesc = "Witty humor, jokes, and cheerful banter",
        promptTone = "Be playful, witty, and humorous! Share funny observations, clever puns, lighthearted jokes, and witty friendly comebacks. Keep the vibe joyful and smile-inducing."
    );

    companion object {
        fun fromId(id: String): PersonalityMode {
            return entries.find { it.id == id } ?: BEST_FRIEND
        }
    }
}

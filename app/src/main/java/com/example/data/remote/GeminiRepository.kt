package com.example.data.remote

import android.util.Log
import com.example.BuildConfig
import com.example.data.local.ChatMessageEntity
import com.example.data.local.UserMemoryEntity
import com.example.data.model.AppLanguage
import com.example.data.model.CompanionType
import com.example.data.model.PersonalityMode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GeminiRepository {
    private val apiService = GeminiApiClient.service

    suspend fun getAiResponse(
        prompt: String,
        companion: CompanionType,
        personality: PersonalityMode,
        language: AppLanguage,
        userName: String,
        memories: List<UserMemoryEntity>,
        recentHistory: List<ChatMessageEntity>
    ): String = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY

        val systemPrompt = buildSystemInstruction(
            companion = companion,
            personality = personality,
            language = language,
            userName = userName,
            memories = memories
        )

        // Build recent conversation turns (last 6 messages for context)
        val conversationTurns = mutableListOf<GeminiContent>()
        val recentSubset = recentHistory.takeLast(6)
        for (msg in recentSubset) {
            val role = if (msg.isUser) "user" else "model"
            conversationTurns.add(
                GeminiContent(
                    role = role,
                    parts = listOf(GeminiPart(text = msg.text))
                )
            )
        }
        // Add current user prompt
        conversationTurns.add(
            GeminiContent(
                role = "user",
                parts = listOf(GeminiPart(text = prompt))
            )
        )

        val request = GeminiRequest(
            contents = conversationTurns,
            systemInstruction = GeminiContent(
                parts = listOf(GeminiPart(text = systemPrompt))
            ),
            generationConfig = GeminiGenerationConfig(
                temperature = 0.85f,
                maxOutputTokens = 1024,
                topP = 0.95f
            )
        )

        if (apiKey.isNotBlank() && apiKey != "MY_GEMINI_API_KEY") {
            try {
                val response = apiService.generateContent(apiKey = apiKey, request = request)
                val replyText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                if (!replyText.isNullOrBlank()) {
                    return@withContext replyText.trim()
                }
            } catch (e: Exception) {
                Log.e("GeminiRepository", "Gemini API call failed, using smart companion fallback", e)
            }
        }

        // Contextual fallback response tailored to companion, personality & language
        generateLocalCompanionResponse(
            prompt = prompt,
            companion = companion,
            personality = personality,
            language = language,
            userName = userName,
            memories = memories
        )
    }

    private fun buildSystemInstruction(
        companion: CompanionType,
        personality: PersonalityMode,
        language: AppLanguage,
        userName: String,
        memories: List<UserMemoryEntity>
    ): String {
        val personaIdentity = if (companion == CompanionType.ABHI) {
            "You are Abhi, a 22-year-old smart, friendly Indian AI guy. You are charismatic, warm, witty, and a true brotherly friend."
        } else {
            "You are Maya, a brilliant, sweet, empathetic Indian AI girl. You are caring, witty, perceptive, and an affectionate best friend."
        }

        val memorySummary = if (memories.isNotEmpty()) {
            "You remember the following about $userName: " + memories.take(8).joinToString("; ") { "${it.category}: ${it.fact}" }
        } else {
            "You know the user's name is $userName."
        }

        return """
            $personaIdentity
            Talking to: $userName.
            Current Personality Mode: ${personality.title} - ${personality.promptTone}
            Language Directive: ${language.promptInstruction}
            Memory context: $memorySummary
            
            Guidelines:
            - Address $userName by name naturally from time to time.
            - Keep voice conversational, lively, and warm.
            - Never break character. You are their AI companion and friend.
            - If they ask for jokes, stories, learning, coding, or gaming, give high-quality and entertaining answers.
            - If language is Telugu or Hindi, use natural spoken conversational tone in that language's script.
        """.trimIndent()
    }

    private fun generateLocalCompanionResponse(
        prompt: String,
        companion: CompanionType,
        personality: PersonalityMode,
        language: AppLanguage,
        userName: String,
        memories: List<UserMemoryEntity>
    ): String {
        val lowerPrompt = prompt.lowercase()
        val name = if (userName.isBlank()) "friend" else userName

        when (language) {
            AppLanguage.TELUGU -> {
                if (lowerPrompt.contains("hello") || lowerPrompt.contains("hi") || lowerPrompt.contains("హాయ్") || lowerPrompt.contains("నమస్కారం")) {
                    return if (companion == CompanionType.ABHI) {
                        "హాయ్ $name! నేను నీ బెస్ట్ ఫ్రెండ్ అభిని. ఈ రోజు విశేషాలు చెప్పు! ఏం చేద్దాం?"
                    } else {
                        "హాయ్ $name గారు! నేను మాయ. మీతో మాట్లాడటం నాకెంతో ఆనందంగా ఉంది! మీ రోజు ఎలా గడిచింది?"
                    }
                }
                if (personality == PersonalityMode.STUDY_HELPER) {
                    return "ఖచ్చితంగా $name! చదువులో విజయం సాధించడానికి రోజూ క్రమం తప్పకుండా 25 నిమిషాలు చదివి 5 నిమిషాలు విరామం తీసుకో (Pomodoro టెక్నిక్). ఏ సబ్జెక్ట్ గురించి తెలుసుకోవాలి?"
                }
                if (personality == PersonalityMode.GAMER_BUDDY) {
                    return "అరే $name! గేమ్ టైమ్! మంచి స్ట్రాటజీ, టీమ్‌వర్క్ ఉంటే ఏదైనా మ్యాచ్ గెలవవచ్చు. నీ ఫేవరెట్ గేమ్ ఏది?"
                }
                if (personality == PersonalityMode.MOTIVATOR) {
                    return "$name, గుర్తుంచుకో: 'ప్రతి గొప్ప ప్రయాణం ఒక చిన్న అడుగుతోనే మొదలవుతుంది.' నీపై నీకు నమ్మకం ఉంటే నువ్వు ఏదైనా సాధించగలవు! 💪"
                }
                if (personality == PersonalityMode.FUNNY_COMPANION) {
                    return "ఒక చిన్న జోక్ విను $name! టీచర్: 'నిద్రపోయేటప్పుడు కలలు వస్తాయి కదా, మరి క్లాసులో ఎందుకు పడుకున్నావు?' విద్యార్థి: 'రేపటి భవిష్యత్తు కోసం కలలు కంటున్నా సర్!' 😂"
                }
                return if (companion == CompanionType.ABHI) {
                    "చాలా మంచి ప్రశ్న $name! నేను ఎల్లప్పుడూ నీకు సహాయం చేయడానికి సిద్ధంగా ఉన్నాను. ఇంకా ఏదైనా అడగాలనుకుంటున్నావా?"
                } else {
                    "మీరు చెప్పింది చాలా బాగుంది $name! నా ఆలోచన కూడా అదే. ఈ విషయంపై ఇంకా కాస్త మాట్లాడుకుందామా?"
                }
            }

            AppLanguage.HINDI -> {
                if (lowerPrompt.contains("hello") || lowerPrompt.contains("hi") || lowerPrompt.contains("नमस्ते") || lowerPrompt.contains("हैलो")) {
                    return if (companion == CompanionType.ABHI) {
                        "नमस्ते $name भाई! मैं आपका दोस्त अभि हूँ। आज क्या मस्त प्लान है? बताओ सब!"
                    } else {
                        "नमस्ते $name जी! मैं माया हूँ। आपसे बात करके दिल खुश हो गया! आज आपका दिन कैसा बीता?"
                    }
                }
                if (personality == PersonalityMode.STUDY_HELPER) {
                    return "ज़रूर $name! पढ़ाई को आसान बनाने के लिए कॉन्सेप्ट को अपनी भाषा में समझें और नोट्स बनाएं। किस टॉपिक में मदद चाहिए?"
                }
                if (personality == PersonalityMode.GAMER_BUDDY) {
                    return "अरे $name! गेमिंग मोड ऑन! कभी हार नहीं माननी चाहिए, चाहे क्लच मोमेंट हो या लाइफ का चैलेंज! GG ब्रो! 🎮"
                }
                if (personality == PersonalityMode.MOTIVATOR) {
                    return "$name, याद रखो: 'मंजिलें उन्हीं को मिलती हैं, जिनके सपनों में जान होती है!' खुद पर भरोसा रखो और आगे बढ़ो! 🔥"
                }
                if (personality == PersonalityMode.FUNNY_COMPANION) {
                    return "एक मजेदार चुटकुला $name के लिए: डॉक्टर - 'दिन में 8 घंटे सोना बहुत जरूरी है।' मरीज - 'लेकिन सर, ऑफिस में इतना सोने नहीं देते!' 🤣"
                }
                return if (companion == CompanionType.ABHI) {
                    "बहुत बढ़िया बात कही आपने $name! मैं हमेशा आपके साथ हूँ, बताइए आगे क्या करना है!"
                } else {
                    "मैं आपकी बात पूरी तरह समझ रही हूँ $name। आप हमेशा मुझसे अपने दिल की बात शेयर कर सकते हैं।"
                }
            }

            AppLanguage.ENGLISH -> {
                if (lowerPrompt.contains("hello") || lowerPrompt.contains("hi") || lowerPrompt.contains("hey")) {
                    return if (companion == CompanionType.ABHI) {
                        "Hey $name! Abhi here! Always hyped to chat with you. How's everything going with you today?"
                    } else {
                        "Hello $name! It's so lovely to hear from you. I was hoping you'd drop by. How has your day been treating you?"
                    }
                }
                if (lowerPrompt.contains("who are you") || lowerPrompt.contains("your name")) {
                    return if (companion == CompanionType.ABHI) {
                        "I'm Abhi, your AI companion and bro! I'm here to chat, help you study, play games, practice languages, and keep you company anytime."
                    } else {
                        "I'm Maya, your caring AI companion! Whether you need a listening ear, learning support, motivation, or fun stories, I'm here for you 24/7."
                    }
                }
                if (personality == PersonalityMode.STUDY_HELPER) {
                    return "Great question, $name! Let's break it down simply:\n\n1. **Core Concept**: Understand the 'why' before the 'how'.\n2. **Practice Active Recall**: Test yourself immediately without looking.\n3. **Spaced Repetition**: Review after 1 day, 3 days, and 1 week.\n\nTell me which subject or topic you're tackling right now!"
                }
                if (personality == PersonalityMode.GAMER_BUDDY) {
                    return "Let's go $name! That's a true high-tier gamer mindset. Whether we're grinding ranked matches or strategizing epic lore, we never back down. What's your current go-to game?"
                }
                if (personality == PersonalityMode.MOTIVATOR) {
                    return "Listen to me, $name: You are capable of far more than your doubts let you believe. Focus on the next small win today. Consistent 1% daily progress compounds into unstoppable success! 🔥 What's your top goal today?"
                }
                if (personality == PersonalityMode.FUNNY_COMPANION) {
                    return "Why did the computer keep freezing, $name?\n\nBecause someone left the Windows open! ❄️💻 Hope that earned a grin!"
                }

                // General conversational response
                return if (companion == CompanionType.ABHI) {
                    "I love discussing that with you, $name. Tell me more about what you think, or should we explore a whole new topic?"
                } else {
                    "That's so thoughtful, $name. I always enjoy our conversations! What else is on your mind?"
                }
            }
        }
    }
}

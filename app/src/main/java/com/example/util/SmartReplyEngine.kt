package com.example.util

import com.example.model.ChatMessage

object SmartReplyEngine {

    /**
     * Generates three context-aware quick-tap response suggestions
     * based on the last AI message and active agent persona.
     */
    fun generateSuggestions(
        lastMessage: ChatMessage?,
        language: String = "en"
    ): List<String> {
        if (lastMessage == null || lastMessage.role != "assistant" || lastMessage.content.isBlank()) {
            return if (language == "tr") {
                listOf("Neler yapabilirsin?", "Bana bir ipucu ver", "Günün haberleri")
            } else {
                listOf("What can you do?", "Explain Quantum Computing", "Give me a daily tip")
            }
        }

        val text = lastMessage.content.lowercase()
        val isTurkish = language == "tr" || text.any { it in "çğışöü" }

        // 1. Code or Technical Implementations
        if (text.contains("```") || text.contains("class ") || text.contains("fun ") || text.contains("val ")) {
            return if (isTurkish) {
                listOf("Adım adım açıklar mısın?", "Birim testlerini yazar mısın?", "Performansı nasıl artırılır?")
            } else {
                listOf("Explain this step-by-step", "Add error handling & unit tests", "How can this be optimized?")
            }
        }

        // 2. Questions posed by the Assistant
        if (lastMessage.content.trim().endsWith("?")) {
            return if (isTurkish) {
                listOf("Evet, lütfen devam et", "Alternatifleri görelim", "Hangisini önerirsin?")
            } else {
                listOf("Yes, please continue", "Show me the alternatives", "Which do you recommend?")
            }
        }

        // 3. Summaries, Bullet Points, or Outlines
        if (text.contains("•") || text.contains("takeaway") || text.contains("özet") || text.contains("1.")) {
            return if (isTurkish) {
                listOf("Bunu görevlerime ekle", "İlk maddeyi detaylandır", "E-posta taslağına dönüştür")
            } else {
                listOf("Elaborate on point #1", "Add this to my Tasks", "Draft an email summary")
            }
        }

        // 4. Web Search or Cited Live News
        if (lastMessage.sources.isNotEmpty() || text.contains("http") || text.contains("source")) {
            return if (isTurkish) {
                listOf("Daha fazla kaynak göster", "En son gelişmeleri özetle", "Farklı görüşler var mı?")
            } else {
                listOf("Tell me more about this", "What are the latest updates?", "Show more detailed sources")
            }
        }

        // 5. Tasks or Scheduling
        if (text.contains("task") || text.contains("görev") || text.contains("deadline") || text.contains("schedule")) {
            return if (isTurkish) {
                listOf("Bunu takvime ekle", "En yüksek öncelik hangisi?", "Zaman tahmini yap")
            } else {
                listOf("Add these to my Tasks", "Which has highest priority?", "Estimate time required")
            }
        }

        // 6. Agent Specific Defaults
        when {
            lastMessage.agentName.contains("ELI5", ignoreCase = true) -> {
                return if (isTurkish) {
                    listOf("Gerçek hayattan bir örnek ver", "Bunu daha da basitleştir", "Neden böyle çalışır?")
                } else {
                    listOf("Give a real-life analogy", "Explain even simpler", "Why does it work this way?")
                }
            }
            lastMessage.agentName.contains("Translator", ignoreCase = true) -> {
                return if (isTurkish) {
                    listOf("Kelime kelime çevir", "Resmi mi yoksa samimi mi?", "Bunu İngilizceye çevir")
                } else {
                    listOf("Is this formal or casual?", "Show word-by-word meaning", "Translate to Turkish")
                }
            }
            lastMessage.agentName.contains("Summarizer", ignoreCase = true) -> {
                return if (isTurkish) {
                    listOf("Tek bir cümleye indir", "Sonraki adımlar neler?", "Detaylı versiyonunu ver")
                } else {
                    listOf("Condense to 1 sentence", "What are the next steps?", "Show full in-depth version")
                }
            }
        }

        // 7. General High-Value Conversational Prompts
        return if (isTurkish) {
            listOf("Somut bir örnek verir misin?", "Daha basit açıkla (ELI5)", "Bunun avantajları neler?")
        } else {
            listOf("Could you give an example?", "Explain simpler (ELI5)", "What are the pros and cons?")
        }
    }
}

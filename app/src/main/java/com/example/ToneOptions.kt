package com.example

enum class ToneOptions(val label: String, val prompt: String) {
    FRIENDLY("Friendly 👋", "warm, casual, natural like texting a friend"),
    FUNNY("Funny 😄", "witty, lighthearted, adds humor naturally"),
    FORMAL("Formal \uD83D\uDC54", "professional, polished, proper grammar"),
    FLIRTY("Flirty \uD83D\uDE0F", "smooth, playful, charming but not creepy"),
    SHORT("Short ⚡", "maximum 1 sentence, straight to the point"),
    SAVAGE("Savage \uD83D\uDE08", "bold, confident, zero filter (still appropriate)"),
    CARING("Caring \uD83E\uDD17", "empathetic, warm, emotionally supportive")
}

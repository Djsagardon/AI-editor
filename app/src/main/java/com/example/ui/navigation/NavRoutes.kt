package com.example.ui.navigation

enum class Screen(val route: String, val title: String) {
    HOME("home", "Home"),
    STUDIO("studio", "Studio"),
    CHAT("chat", "AI Chat"),
    VOICE("voice", "Voice AI"),
    TEMPLATES("templates", "Templates"),
    MEMORY("memory", "Memory & Docs"),
    PROFILE_ADMIN("profile_admin", "Settings")
}

package com.example.model

enum class ScriptType(val displayName: String, val badgeColorHex: Long) {
    SERVER_SCRIPT("سكربت خادم (Server)", 0xFF3B82F6),
    LOCAL_SCRIPT("سكربت عميل (LocalScript)", 0xFF10B981),
    MODULE_SCRIPT("سكربت نمطي (ModuleScript)", 0xFFF59E0B)
}

data class RobloxScript(
    val id: String = java.util.UUID.randomUUID().toString(),
    val title: String,
    val scriptType: ScriptType = ScriptType.SERVER_SCRIPT,
    val location: String = "ServerScriptService",
    val description: String = "",
    val code: String,
    val setupSteps: List<String> = emptyList(),
    val tags: List<String> = emptyList(),
    val createdAt: Long = System.currentTimeMillis()
)

enum class LogLevel {
    PRINT,
    WARN,
    ERROR,
    SUCCESS
}

data class ConsoleLog(
    val id: String = java.util.UUID.randomUUID().toString(),
    val message: String,
    val level: LogLevel = LogLevel.PRINT,
    val timestamp: String,
    val source: String = "RobloxStudio"
)

data class TemplateItem(
    val id: String,
    val title: String,
    val category: String,
    val iconEmoji: String,
    val shortDescription: String,
    val script: RobloxScript
)

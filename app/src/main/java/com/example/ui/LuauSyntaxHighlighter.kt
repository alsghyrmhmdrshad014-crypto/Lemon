package com.example.ui

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.example.ui.theme.LuauComment
import com.example.ui.theme.LuauFunction
import com.example.ui.theme.LuauKeyword
import com.example.ui.theme.LuauNumber
import com.example.ui.theme.LuauString
import com.example.ui.theme.LuauVariable
import java.util.regex.Pattern

object LuauSyntaxHighlighter {

    private val KEYWORDS = setOf(
        "local", "function", "end", "then", "if", "else", "elseif",
        "return", "for", "in", "do", "while", "repeat", "until",
        "and", "or", "not", "nil", "true", "false", "type", "export"
    )

    private val ROBLOX_CALLS = setOf(
        "game", "workspace", "script", "Instance", "Vector3", "CFrame",
        "Color3", "UDim2", "Enum", "task", "pcall", "math", "table", "string",
        "GetService", "FindFirstChild", "WaitForChild", "Connect", "Destroy",
        "FireServer", "InvokeServer", "new", "wait", "spawn", "print", "warn", "error"
    )

    fun highlight(code: String): AnnotatedString {
        return buildAnnotatedString {
            append(code)

            val lines = code.split("\n")
            var currentIndex = 0

            for (line in lines) {
                val commentIndex = line.indexOf("--")
                val codePortion = if (commentIndex >= 0) line.substring(0, commentIndex) else line

                // Highlight strings in codePortion
                val stringMatcher = Pattern.compile("(\"[^\"]*\"|'[^']*')").matcher(codePortion)
                while (stringMatcher.find()) {
                    addStyle(
                        SpanStyle(color = LuauString),
                        currentIndex + stringMatcher.start(),
                        currentIndex + stringMatcher.end()
                    )
                }

                // Highlight tokens/keywords
                val tokenMatcher = Pattern.compile("[a-zA-Z_][a-zA-Z0-9_]*").matcher(codePortion)
                while (tokenMatcher.find()) {
                    val token = tokenMatcher.group()
                    val start = currentIndex + tokenMatcher.start()
                    val end = currentIndex + tokenMatcher.end()

                    if (KEYWORDS.contains(token)) {
                        addStyle(
                            SpanStyle(color = LuauKeyword, fontWeight = FontWeight.Bold),
                            start,
                            end
                        )
                    } else if (ROBLOX_CALLS.contains(token)) {
                        addStyle(
                            SpanStyle(color = LuauFunction, fontWeight = FontWeight.Medium),
                            start,
                            end
                        )
                    }
                }

                // Highlight numbers
                val numberMatcher = Pattern.compile("\\b\\d+(\\.\\d+)?\\b").matcher(codePortion)
                while (numberMatcher.find()) {
                    addStyle(
                        SpanStyle(color = LuauNumber),
                        currentIndex + numberMatcher.start(),
                        currentIndex + numberMatcher.end()
                    )
                }

                // Highlight comments
                if (commentIndex >= 0) {
                    addStyle(
                        SpanStyle(color = LuauComment, fontWeight = FontWeight.Normal),
                        currentIndex + commentIndex,
                        currentIndex + line.length
                    )
                }

                currentIndex += line.length + 1 // +1 for \n
            }
        }
    }
}

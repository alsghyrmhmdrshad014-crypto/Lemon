package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.RobloxScript
import com.example.model.ScriptType
import com.example.network.LemonadeEngine
import com.example.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GeneratorScreen(
    onTestInSimulator: (RobloxScript) -> Unit,
    onSaveScript: (RobloxScript) -> Unit,
    initialPrompt: String = "",
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    var promptText by remember { mutableStateOf(initialPrompt) }
    var selectedScriptType by remember { mutableStateOf(ScriptType.SERVER_SCRIPT) }
    var selectedCategory by remember { mutableStateOf("آليات اللعب") }
    var isGenerating by remember { mutableStateOf(false) }
    var generatedScript by remember { mutableStateOf<RobloxScript?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val context = androidx.compose.ui.platform.LocalContext.current
    var showApiKeyDialog by remember { mutableStateOf(false) }
    var dialogApiKeyInput by remember { mutableStateOf(LemonadeEngine.customApiKey) }
    var isTestingDialogKey by remember { mutableStateOf(false) }
    var dialogTestMessage by remember { mutableStateOf<String?>(null) }
    var dialogTestSuccess by remember { mutableStateOf(false) }

    val quickPrompts = listOf(
        "نظام تفقيس الحيوانات الأليفة مع نسب ندرة متعددة",
        "حفظ النقود والجواهر تلقائياً بـ DataStore2",
        "نظام قتال بالسيوف والكومبو بـ Raycast",
        "واجهة متجر متحركة مع زر فتح وإغلاق وشراء",
        "أوامر أدمن للدردشة :fly و :speed و :heal",
        "ماكينة تايكون مع سير ناقل وإسقاط خامات"
    )

    if (showApiKeyDialog) {
        AlertDialog(
            onDismissRequest = { showApiKeyDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Key, contentDescription = null, tint = LemonYellow, modifier = Modifier.size(22.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("إعداد مفتاح Gemini API", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            },
            text = {
                Column {
                    Text(
                        text = "أدخل مفتاح Google Gemini API الخاص بك لتوليد الأكواد سحابياً بأحدث نماذج الذكاء الاصطناعي (Gemini 3.5 Flash).",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                        lineHeight = 18.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = dialogApiKeyInput,
                        onValueChange = { dialogApiKeyInput = it },
                        modifier = Modifier.fillMaxWidth().testTag("dialog_api_key_input"),
                        label = { Text("مفتاح Gemini API", color = TextMuted) },
                        placeholder = { Text("AIzaSy...", color = TextMuted) },
                        trailingIcon = {
                            if (dialogApiKeyInput.isNotEmpty()) {
                                IconButton(onClick = {
                                    dialogApiKeyInput = ""
                                    dialogTestMessage = null
                                }) {
                                    Icon(imageVector = Icons.Default.Clear, contentDescription = "مسح", tint = TextMuted)
                                }
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = LemonYellow,
                            unfocusedBorderColor = StudioBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            cursorColor = LemonYellow
                        ),
                        shape = RoundedCornerShape(10.dp),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Paste from clipboard button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                                val clipText = clipboard.primaryClip?.getItemAt(0)?.text?.toString() ?: ""
                                if (clipText.isNotBlank()) {
                                    dialogApiKeyInput = clipText.trim()
                                    dialogTestMessage = null
                                    android.widget.Toast.makeText(context, "تم لصق المفتاح من الحافظة!", android.widget.Toast.LENGTH_SHORT).show()
                                } else {
                                    android.widget.Toast.makeText(context, "الحافظة فارغة، انسخ المفتاح أولاً", android.widget.Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier.weight(1f).testTag("dialog_paste_key_button"),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = LemonYellow),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                        ) {
                            Icon(imageVector = Icons.Default.ContentPaste, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("📋 لصق من الحافظة", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        if (dialogApiKeyInput.isNotBlank()) {
                            OutlinedButton(
                                onClick = {
                                    isTestingDialogKey = true
                                    dialogTestMessage = null
                                    coroutineScope.launch {
                                        val (success, msg) = LemonadeEngine.testApiKey(dialogApiKeyInput)
                                        isTestingDialogKey = false
                                        dialogTestSuccess = success
                                        dialogTestMessage = msg
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                enabled = !isTestingDialogKey,
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = NeonLime),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                            ) {
                                if (isTestingDialogKey) {
                                    CircularProgressIndicator(modifier = Modifier.size(14.dp), color = NeonLime, strokeWidth = 2.dp)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("فحص...", fontSize = 11.sp)
                                } else {
                                    Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("فحص المفتاح", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(
                            onClick = {
                                val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                                val clip = android.content.ClipData.newPlainText("AI Studio URL", "https://aistudio.google.com/app/apikey")
                                clipboard.setPrimaryClip(clip)
                                android.widget.Toast.makeText(context, "تم نسخ رابط إنشاء المفتاح للحافظة!", android.widget.Toast.LENGTH_SHORT).show()
                            }
                        ) {
                            Text("رابط الحصول على مفتاح مجاني", fontSize = 11.sp, color = NeonLime)
                        }

                        if (LemonadeEngine.customApiKey.isNotBlank()) {
                            TextButton(
                                onClick = {
                                    dialogApiKeyInput = ""
                                    LemonadeEngine.saveApiKey(context, "")
                                    dialogTestMessage = null
                                    android.widget.Toast.makeText(context, "تمت إزالة المفتاح والعودة للمحرك المدمج", android.widget.Toast.LENGTH_SHORT).show()
                                    showApiKeyDialog = false
                                }
                            ) {
                                Text("إزالة المفتاح", fontSize = 11.sp, color = RobloxRed)
                            }
                        }
                    }

                    dialogTestMessage?.let { msg ->
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = (if (dialogTestSuccess) "✔ " else "✖ ") + msg,
                            color = if (dialogTestSuccess) RobloxGreen else RobloxRed,
                            fontSize = 11.sp,
                            lineHeight = 15.sp
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        LemonadeEngine.saveApiKey(context, dialogApiKeyInput)
                        android.widget.Toast.makeText(context, "تم حفظ المفتاح بنجاح!", android.widget.Toast.LENGTH_SHORT).show()
                        showApiKeyDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = LemonYellow, contentColor = StudioBlack),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("حفظ المفتاح", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showApiKeyDialog = false }) {
                    Text("إغلاق", color = TextSecondary)
                }
            },
            containerColor = StudioCardBg,
            shape = RoundedCornerShape(16.dp)
        )
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hero Card / Lemonade AI Banner
        item {
            Card(
                modifier = Modifier.fillMaxWidth().testTag("hero_banner"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = StudioCardBg),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = androidx.compose.ui.graphics.SolidColor(StudioBorder)
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(LemonYellow),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Bolt,
                                    contentDescription = null,
                                    tint = StudioBlack,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "استوديو Lemonade.gg الذكي",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "مولّد أكواد روبلوكس بلغة Luau",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = NeonLime
                                )
                            }
                        }

                        // AI Status Badge with Click Action
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = StudioDarkBg,
                            border = CardDefaults.outlinedCardBorder().copy(
                                brush = androidx.compose.ui.graphics.SolidColor(
                                    if (LemonadeEngine.getEffectiveApiKey().isNotEmpty()) RobloxGreen.copy(alpha = 0.5f) else LemonYellow.copy(alpha = 0.5f)
                                )
                            ),
                            modifier = Modifier
                                .clickable {
                                    dialogApiKeyInput = LemonadeEngine.customApiKey
                                    dialogTestMessage = null
                                    showApiKeyDialog = true
                                }
                                .testTag("ai_status_badge")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(if (LemonadeEngine.getEffectiveApiKey().isNotEmpty()) RobloxGreen else LemonYellow)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (LemonadeEngine.getEffectiveApiKey().isNotEmpty()) "Gemini AI" else "المحرك الذكي",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (LemonadeEngine.getEffectiveApiKey().isNotEmpty()) RobloxGreen else LemonYellow,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(imageVector = Icons.Default.Key, contentDescription = null, tint = LemonYellow, modifier = Modifier.size(12.dp))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "اكتب فكرتك وسيقوم الذكاء الاصطناعي بتوليد كود Luau كامل ونظيف وخالٍ من الأخطاء وجاهز للنسخ إلى استوديو روبلوكس فوراً.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                        lineHeight = 18.sp
                    )
                }
            }
        }

        // Engine Mode Choice & Add Key Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        dialogApiKeyInput = LemonadeEngine.customApiKey
                        dialogTestMessage = null
                        showApiKeyDialog = true
                    }
                    .testTag("engine_mode_selector_card"),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = StudioCardBg),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = androidx.compose.ui.graphics.SolidColor(
                        if (LemonadeEngine.getEffectiveApiKey().isNotEmpty()) RobloxGreen else StudioBorder
                    )
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = if (LemonadeEngine.getEffectiveApiKey().isNotEmpty()) RobloxGreen.copy(alpha = 0.2f) else LemonYellow.copy(alpha = 0.2f),
                            modifier = Modifier.size(38.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = if (LemonadeEngine.getEffectiveApiKey().isNotEmpty()) Icons.Default.CloudDone else Icons.Default.VpnKey,
                                    contentDescription = null,
                                    tint = if (LemonadeEngine.getEffectiveApiKey().isNotEmpty()) RobloxGreen else LemonYellow,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = if (LemonadeEngine.getEffectiveApiKey().isNotEmpty())
                                    "محرك السحابة: مفتاح Gemini مفعّل"
                                else
                                    "إضافة مفتاح Gemini API (اختياري)",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = if (LemonadeEngine.getEffectiveApiKey().isNotEmpty())
                                    "متصل بالسحابة • اضغط لتعديل أو حذف المفتاح"
                                else
                                    "يعمل حالياً بالمحرك المدمج مجاناً • اضغط هنا لإضافة مفتاح",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary,
                                fontSize = 11.sp
                            )
                        }
                    }

                    FilledTonalButton(
                        onClick = {
                            dialogApiKeyInput = LemonadeEngine.customApiKey
                            dialogTestMessage = null
                            showApiKeyDialog = true
                        },
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = if (LemonadeEngine.getEffectiveApiKey().isNotEmpty()) RobloxGreen.copy(alpha = 0.15f) else LemonYellow.copy(alpha = 0.15f),
                            contentColor = if (LemonadeEngine.getEffectiveApiKey().isNotEmpty()) RobloxGreen else LemonYellow
                        ),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = if (LemonadeEngine.getEffectiveApiKey().isNotEmpty()) "تعديل" else "إضافة مفتاح",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }

        // Script Type Selector
        item {
            Column {
                Text(
                    text = "نوع السكربت المطلوب:",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ScriptType.values().forEach { type ->
                        val isSelected = selectedScriptType == type
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { selectedScriptType = type }
                                .testTag("script_type_${type.name}"),
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) Color(type.badgeColorHex).copy(alpha = 0.25f) else StudioCardBg,
                            border = CardDefaults.outlinedCardBorder().copy(
                                brush = androidx.compose.ui.graphics.SolidColor(
                                    if (isSelected) Color(type.badgeColorHex) else StudioBorder
                                )
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = when (type) {
                                        ScriptType.SERVER_SCRIPT -> "خادم (Server)"
                                        ScriptType.LOCAL_SCRIPT -> "عميل (Client)"
                                        ScriptType.MODULE_SCRIPT -> "نمطي (Module)"
                                    },
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) Color(type.badgeColorHex) else TextSecondary,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }
        }

        // Prompt Input Field
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = StudioCardBg),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = androidx.compose.ui.graphics.SolidColor(StudioBorder)
                )
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "ماذا تريد أن تبرمج في روبلوكس؟",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = promptText,
                        onValueChange = { promptText = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 90.dp)
                            .testTag("prompt_input_field"),
                        placeholder = {
                            Text(
                                "مثال: اصنع نظام قتال بالسيوف مع كومبو وضرر، أو واجهة متجر متحركة مع زر شراء...",
                                color = TextMuted,
                                fontSize = 13.sp
                            )
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = LemonYellow,
                            unfocusedBorderColor = StudioBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            cursorColor = LemonYellow
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Generate Button
                    Button(
                        onClick = {
                            if (promptText.isNotBlank() && !isGenerating) {
                                isGenerating = true
                                errorMessage = null
                                coroutineScope.launch {
                                    try {
                                        val result = LemonadeEngine.generateRobloxCode(
                                            prompt = promptText,
                                            targetType = selectedScriptType,
                                            selectedCategory = selectedCategory
                                        )
                                        generatedScript = result
                                    } catch (e: Exception) {
                                        errorMessage = e.localizedMessage ?: "حدث خطأ أثناء توليد الكود"
                                    } finally {
                                        isGenerating = false
                                    }
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("generate_luau_button"),
                        enabled = promptText.isNotBlank() && !isGenerating,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = LemonYellow,
                            contentColor = Color(0xFF1E1E1E),
                            disabledContainerColor = StudioBorder,
                            disabledContentColor = TextMuted
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        if (isGenerating) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = Color(0xFF1E1E1E),
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("الذكاء الاصطناعي يقوم بكتابة الكود الآن...", fontWeight = FontWeight.Bold)
                        } else {
                            Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("توليد كود روبلوكس Luau", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Quick Ideas / Template Chips
        item {
            Column {
                Text(
                    text = "أفكار واقتراحات سريعة:",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(quickPrompts) { prompt ->
                        SuggestionChip(
                            onClick = { promptText = prompt },
                            label = { Text(prompt, fontSize = 12.sp, color = TextPrimary) },
                            colors = SuggestionChipDefaults.suggestionChipColors(
                                containerColor = StudioCardBg
                            ),
                            border = SuggestionChipDefaults.suggestionChipBorder(
                                enabled = true,
                                borderColor = StudioBorder
                            ),
                            shape = RoundedCornerShape(8.dp)
                        )
                    }
                }
            }
        }

        // Error message if any
        if (errorMessage != null) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = RobloxRed.copy(alpha = 0.15f)),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = androidx.compose.ui.graphics.SolidColor(RobloxRed)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(imageVector = Icons.Default.ErrorOutline, contentDescription = null, tint = RobloxRed)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = errorMessage ?: "", color = RobloxRed, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }

        // Generated Script Output
        val script = generatedScript
        if (script != null) {
            item {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "كود Luau المُولّد:",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = NeonLime
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = RobloxGreen, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = "جاهز للنسخ", color = RobloxGreen, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    CodeViewerCard(
                        script = script,
                        onTestInSimulator = onTestInSimulator,
                        onSaveScript = onSaveScript
                    )
                }
            }
        }
    }
}

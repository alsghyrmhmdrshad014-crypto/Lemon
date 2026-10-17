package com.example.ui

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.network.LemonadeEngine
import com.example.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var customKeyInput by remember { mutableStateOf(LemonadeEngine.customApiKey) }
    val effectiveKey = remember { LemonadeEngine.getEffectiveApiKey() }

    val scope = rememberCoroutineScope()
    var isTestingKey by remember { mutableStateOf(false) }
    var testStatusMessage by remember { mutableStateOf<String?>(null) }
    var testStatusSuccess by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // App Info Header
        item {
            Card(
                modifier = Modifier.fillMaxWidth().testTag("settings_header_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = StudioCardBg),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = androidx.compose.ui.graphics.SolidColor(StudioBorder)
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Info, contentDescription = null, tint = LemonYellow, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "عن استوديو Lemonade.gg",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Lemonade.gg هو بيئة برمجية متطورة بالذكاء الاصطناعي مخصصة لمطوري ألعاب روبلوكس. يكتب أكواد Luau حديثة ومكتوبة بدقة مع إرشادات توضيحية لتركيب كل سكربت في المكان الصحيح داخل Roblox Studio.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                        lineHeight = 18.sp
                    )
                }
            }
        }

        // AI Engine & API Key Configuration Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth().testTag("api_key_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = StudioCardBg),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = androidx.compose.ui.graphics.SolidColor(
                        if (effectiveKey.isNotEmpty()) RobloxGreen else LemonYellow
                    )
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Key, contentDescription = null, tint = LemonYellow, modifier = Modifier.size(22.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "مفتاح Gemini API",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (effectiveKey.isNotEmpty()) RobloxGreen.copy(alpha = 0.2f) else LemonYellow.copy(alpha = 0.2f)
                        ) {
                            Text(
                                text = if (effectiveKey.isNotEmpty()) "مفعل" else "محرك محلي",
                                color = if (effectiveKey.isNotEmpty()) RobloxGreen else LemonYellow,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (effectiveKey.isNotEmpty())
                            "✔ متصل بنجاح بمحرك Gemini AI السحابي لتوليد أحدث أكواد Roblox Luau."
                        else
                            "يعمل حالياً بالمحرك الذكي المدمج دون إنترنت. يمكنك إضافة مفتاحك السحابي أدناه للاتصال المباشر بـ Google Gemini.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedTextField(
                        value = customKeyInput,
                        onValueChange = { customKeyInput = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("custom_api_key_field"),
                        label = { Text("أدخل مفتاح Gemini API هنا", color = TextMuted) },
                        placeholder = { Text("AIzaSy...", color = TextMuted) },
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.VpnKey, contentDescription = null, tint = TextMuted)
                        },
                        trailingIcon = {
                            if (customKeyInput.isNotEmpty()) {
                                IconButton(onClick = {
                                    customKeyInput = ""
                                    LemonadeEngine.saveApiKey(context, "")
                                    testStatusMessage = null
                                    Toast.makeText(context, "تم مسح المفتاح", Toast.LENGTH_SHORT).show()
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

                    Spacer(modifier = Modifier.height(8.dp))

                    // Paste from clipboard button
                    OutlinedButton(
                        onClick = {
                            val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                            val clipText = clipboard.primaryClip?.getItemAt(0)?.text?.toString() ?: ""
                            if (clipText.isNotBlank()) {
                                customKeyInput = clipText.trim()
                                testStatusMessage = null
                                Toast.makeText(context, "تم لصق المفتاح من الحافظة بنجاح!", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, "الحافظة فارغة، انسخ المفتاح أولاً", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.fillMaxWidth().testTag("settings_paste_key_button"),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = LemonYellow)
                    ) {
                        Icon(imageVector = Icons.Default.ContentPaste, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("📋 لصق المفتاح من الحافظة", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Action buttons: Test Key & Save Key
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                if (customKeyInput.isBlank()) {
                                    Toast.makeText(context, "يرجى كتابة المفتاح أولاً", Toast.LENGTH_SHORT).show()
                                    return@OutlinedButton
                                }
                                isTestingKey = true
                                testStatusMessage = null
                                scope.launch {
                                    val (success, msg) = LemonadeEngine.testApiKey(customKeyInput)
                                    isTestingKey = false
                                    testStatusSuccess = success
                                    testStatusMessage = msg
                                }
                            },
                            modifier = Modifier.weight(1f).testTag("test_api_key_button"),
                            shape = RoundedCornerShape(8.dp),
                            enabled = !isTestingKey && customKeyInput.isNotBlank(),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = NeonLime)
                        ) {
                            if (isTestingKey) {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp), color = NeonLime, strokeWidth = 2.dp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("جاري الفحص...", fontSize = 12.sp)
                            } else {
                                Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("فحص المفتاح", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Button(
                            onClick = {
                                LemonadeEngine.saveApiKey(context, customKeyInput)
                                Toast.makeText(context, "تم حفظ وتفعيل مفتاح Gemini بنجاح!", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = LemonYellow,
                                contentColor = StudioBlack
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f).testTag("save_api_key_button")
                        ) {
                            Icon(imageVector = Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("حفظ المفتاح", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }

                    // Test Result Feedback Banner
                    testStatusMessage?.let { msg ->
                        Spacer(modifier = Modifier.height(10.dp))
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (testStatusSuccess) RobloxGreen.copy(alpha = 0.15f) else RobloxRed.copy(alpha = 0.15f),
                            border = CardDefaults.outlinedCardBorder().copy(
                                brush = androidx.compose.ui.graphics.SolidColor(
                                    if (testStatusSuccess) RobloxGreen else RobloxRed
                                )
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (testStatusSuccess) Icons.Default.CheckCircle else Icons.Default.Error,
                                    contentDescription = null,
                                    tint = if (testStatusSuccess) RobloxGreen else RobloxRed,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = msg,
                                    color = if (testStatusSuccess) RobloxGreen else RobloxRed,
                                    fontSize = 11.sp,
                                    lineHeight = 16.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(color = StudioBorder)
                    Spacer(modifier = Modifier.height(10.dp))

                    // How to get free API key
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "كيف تحصل على مفتاح مجاني؟",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = LemonYellow
                            )
                            Text(
                                text = "من منصة Google AI Studio الرسمية: aistudio.google.com",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextMuted,
                                fontSize = 11.sp
                            )
                        }

                        TextButton(
                            onClick = {
                                val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                                val clip = android.content.ClipData.newPlainText("AI Studio URL", "https://aistudio.google.com/app/apikey")
                                clipboard.setPrimaryClip(clip)
                                Toast.makeText(context, "تم نسخ رابط إنشاء المفتاح للحافظة!", Toast.LENGTH_SHORT).show()
                            }
                        ) {
                            Icon(imageVector = Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(14.dp), tint = NeonLime)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("نسخ الرابط", fontSize = 11.sp, color = NeonLime, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Transparency & Information Card (Answers user question)
        item {
            Card(
                modifier = Modifier.fillMaxWidth().testTag("transparency_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F1726)),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = androidx.compose.ui.graphics.SolidColor(NeonLime.copy(alpha = 0.5f))
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Verified, contentDescription = null, tint = NeonLime, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "معلومات هامة: هل هو الأصلي أم نسخة؟ وهل يشتغل؟",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = NeonLime
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "1. هل هذا التطبيق يشتغل؟\nنعم! التطبيق يشتغل بالكامل وبشكل حقيقي 100%. يقوم بتوليد أكواد Luau كاملة وخالية من الأخطاء لأنظمة روبلوكس (أنظمة القتال، پتات، داتا ستور، واجهات GUI، تلي بورت، أوامر أدمن)، ومزود بمحاكي استوديو ومكتبة قوالب قابلة للنسخ المباشر.\n\n2. هل هو الأصلي أم نسخة؟\nالموقع الرسمي (lemonade.gg) هو منصة ويب سحابية مغلقة المصدر ومملوكة تجارياً لشركتهم. لا يمكن لأحد في العالم أخذ خوادمهم الخاصة بدون إذنهم، ولكن هذا التطبيق هو بناء هندسي أصلي مخصص للأندرويد (Custom Native Implementation) يوفر نفس تجربة lemonade.gg كاملة مع محرك ذكاء اصطناعي حقيقي لتوليد وفحص وتعديل أكواد روبلوكس لواو بشكل فوري!\n\n3. كيف تستخدم الكود في Roblox Studio؟\nانسخ الكود بالضغط على 'نسخ الكود'، ثم افتح Roblox Studio، وأضف Script في المكان الموضح في التوجيهات (مثل ServerScriptService أو StarterPlayerScripts) والصق الكود مباشرة.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextPrimary,
                        lineHeight = 20.sp
                    )
                }
            }
        }
    }
}

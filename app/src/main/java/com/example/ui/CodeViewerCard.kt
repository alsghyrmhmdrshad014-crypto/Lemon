package com.example.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.RobloxScript
import com.example.model.ScriptType
import com.example.ui.theme.*

@Composable
fun CodeViewerCard(
    script: RobloxScript,
    modifier: Modifier = Modifier,
    onTestInSimulator: (RobloxScript) -> Unit = {},
    onSaveScript: (RobloxScript) -> Unit = {}
) {
    val context = LocalContext.current
    var showInstructions by remember { mutableStateOf(false) }
    var isSaved by remember { mutableStateOf(false) }

    val lineCount = remember(script.code) { script.code.lines().size }
    val highlightedCode = remember(script.code) { LuauSyntaxHighlighter.highlight(script.code) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("code_viewer_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = StudioCardBg),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(StudioBorder))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header: Title & Badges
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = script.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Folder,
                            contentDescription = "المسار في الاستوديو",
                            tint = LemonYellow,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = script.location,
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary,
                            maxLines = 1
                        )
                    }
                }

                // Script Type Badge
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(script.scriptType.badgeColorHex).copy(alpha = 0.2f),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = androidx.compose.ui.graphics.SolidColor(Color(script.scriptType.badgeColorHex))
                    )
                ) {
                    Text(
                        text = when (script.scriptType) {
                            ScriptType.SERVER_SCRIPT -> "سكربت خادم"
                            ScriptType.LOCAL_SCRIPT -> "سكربت عميل"
                            ScriptType.MODULE_SCRIPT -> "سكربت نمطي"
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(script.scriptType.badgeColorHex),
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Studio Code Editor Terminal Container
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 180.dp, max = 340.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(StudioBlack)
                    .border(1.dp, StudioBorder, RoundedCornerShape(12.dp))
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    // Terminal Top Bar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF0F1522))
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(Color(0xFFFF5F56)))
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(Color(0xFFFFBD2E)))
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(Color(0xFF27C93F)))
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "${script.title}.luau",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextMuted,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        Text(
                            text = "$lineCount سطر",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextMuted
                        )
                    }

                    // Code Content with Line Numbers
                    val verticalScroll = rememberScrollState()
                    val horizontalScroll = rememberScrollState()

                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(verticalScroll)
                            .padding(vertical = 8.dp)
                    ) {
                        // Line numbers column
                        Column(
                            modifier = Modifier
                                .background(Color(0xFF090D14))
                                .padding(horizontal = 8.dp),
                            horizontalAlignment = Alignment.End
                        ) {
                            for (i in 1..lineCount) {
                                Text(
                                    text = "$i",
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 11.sp,
                                    color = TextMuted.copy(alpha = 0.5f),
                                    lineHeight = 18.sp
                                )
                            }
                        }

                        // Code column
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .horizontalScroll(horizontalScroll)
                                .padding(horizontal = 12.dp)
                        ) {
                            Text(
                                text = highlightedCode,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 12.sp,
                                lineHeight = 18.sp,
                                color = TextPrimary
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action Buttons Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Copy Button
                Button(
                    onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = ClipData.newPlainText("Lemonade Luau Script", script.code)
                        clipboard.setPrimaryClip(clip)
                        Toast.makeText(context, "تم نسخ الكود للحافظة بنجاح!", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.weight(1f).testTag("copy_code_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = LemonYellow,
                        contentColor = Color(0xFF1E1E1E)
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(imageVector = Icons.Default.ContentCopy, contentDescription = "نسخ", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "نسخ الكود", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }

                // Test in Simulator Button
                FilledTonalButton(
                    onClick = { onTestInSimulator(script) },
                    modifier = Modifier.weight(1f).testTag("simulate_code_button"),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = StudioBorder,
                        contentColor = NeonLime
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(imageVector = Icons.Default.PlayArrow, contentDescription = "تجربة", modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "تجربة بالمحاكي", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                }

                // Save Script Button
                IconButton(
                    onClick = {
                        onSaveScript(script)
                        isSaved = true
                        Toast.makeText(context, "تم الحفظ في محفوظاتي!", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.testTag("save_script_icon_button")
                ) {
                    Icon(
                        imageVector = if (isSaved) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                        contentDescription = "حفظ",
                        tint = if (isSaved) LemonYellow else TextSecondary
                    )
                }

                // Share Button
                IconButton(
                    onClick = {
                        val shareIntent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_TEXT, "-- ${script.title}\n-- الموقع: ${script.location}\n\n${script.code}")
                            type = "text/plain"
                        }
                        context.startActivity(Intent.createChooser(shareIntent, "مشاركة سكربت روبلوكس"))
                    },
                    modifier = Modifier.testTag("share_script_icon_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "مشاركة",
                        tint = TextSecondary
                    )
                }
            }

            // Setup Instructions Accordion Toggle
            if (script.setupSteps.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                TextButton(
                    onClick = { showInstructions = !showInstructions },
                    modifier = Modifier.fillMaxWidth().testTag("toggle_setup_instructions"),
                    colors = ButtonDefaults.textButtonColors(contentColor = NeonLime)
                ) {
                    Icon(
                        imageVector = if (showInstructions) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (showInstructions) "إخفاء طريقة التركيب" else "طريقة التركيب في Roblox Studio (اضغط للعرض)",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                AnimatedVisibility(visible = showInstructions) {
                    Surface(
                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF0F172A),
                        border = CardDefaults.outlinedCardBorder().copy(
                            brush = androidx.compose.ui.graphics.SolidColor(StudioBorder)
                        )
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "خطوات التركيب في Roblox Studio:",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = LemonYellow
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            script.setupSteps.forEachIndexed { idx, step ->
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Text(
                                        text = "${idx + 1}.",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Bold,
                                        color = NeonLime,
                                        modifier = Modifier.width(22.dp)
                                    )
                                    Text(
                                        text = step,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextSecondary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

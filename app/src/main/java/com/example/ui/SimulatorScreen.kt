package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.PresetTemplates
import com.example.model.ConsoleLog
import com.example.model.LogLevel
import com.example.model.RobloxScript
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun SimulatorScreen(
    currentScript: RobloxScript?,
    modifier: Modifier = Modifier
) {
    val activeScript = remember(currentScript) {
        currentScript ?: PresetTemplates.allTemplates.first().script
    }

    var logs by remember {
        mutableStateOf(
            listOf(
                ConsoleLog(
                    message = "بدء جلسة الاستوديو (Roblox Studio Engine v0.640)",
                    level = LogLevel.PRINT,
                    timestamp = "00:00.01",
                    source = "Engine"
                ),
                ConsoleLog(
                    message = "تم تحميل السكربت: ${activeScript.title} في ${activeScript.location}",
                    level = LogLevel.SUCCESS,
                    timestamp = "00:00.04",
                    source = "StudioPlugin"
                ),
                ConsoleLog(
                    message = "[Lemonade]: تم ربط الأحداث بنجاح (Players, ReplicatedStorage, RunService)",
                    level = LogLevel.PRINT,
                    timestamp = "00:00.06",
                    source = activeScript.title
                )
            )
        )
    }

    var selectedFilter by remember { mutableStateOf("الكل") }

    fun addLog(msg: String, level: LogLevel) {
        val sdf = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        val time = sdf.format(Date())
        logs = logs + ConsoleLog(
            message = msg,
            level = level,
            timestamp = time,
            source = activeScript.title
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        // Top Info Bar
        Card(
            modifier = Modifier.fillMaxWidth().testTag("simulator_info_card"),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = StudioCardBg),
            border = CardDefaults.outlinedCardBorder().copy(
                brush = androidx.compose.ui.graphics.SolidColor(StudioBorder)
            )
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "محاكي Roblox Studio المباشر",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "قيد الاختبار: ${activeScript.title} (${activeScript.location})",
                        style = MaterialTheme.typography.bodySmall,
                        color = LemonYellow,
                        maxLines = 1
                    )
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = RobloxGreen.copy(alpha = 0.2f),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = androidx.compose.ui.graphics.SolidColor(RobloxGreen)
                    )
                ) {
                    Text(
                        text = "شغّال",
                        style = MaterialTheme.typography.labelSmall,
                        color = RobloxGreen,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Virtual Studio Explorer Preview
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0C111A)),
            border = CardDefaults.outlinedCardBorder().copy(
                brush = androidx.compose.ui.graphics.SolidColor(StudioBorder)
            )
        ) {
            Column(modifier = Modifier.padding(10.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.AccountTree, contentDescription = null, tint = LemonYellow, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "شجرة ملفات الاستوديو (Explorer)", style = MaterialTheme.typography.labelMedium, color = TextPrimary, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf("Workspace", "Players", "ReplicatedStorage", "ServerScriptService", "StarterGui").forEach { folder ->
                        val isTarget = activeScript.location.startsWith(folder)
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (isTarget) LemonYellow.copy(alpha = 0.2f) else StudioCardBg,
                            border = CardDefaults.outlinedCardBorder().copy(
                                brush = androidx.compose.ui.graphics.SolidColor(if (isTarget) LemonYellow else StudioBorder)
                            )
                        ) {
                            Text(
                                text = if (isTarget) "📁 $folder ★" else "📁 $folder",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isTarget) LemonYellow else TextSecondary,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
                                fontSize = 10.sp
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Simulation Action Buttons
        Text(
            text = "مشغّلات المحاكاة التفاعلية:",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
        Spacer(modifier = Modifier.height(6.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            FilledTonalButton(
                onClick = {
                    addLog("PlayerAdded: انضم اللاعب 'Player_98241' إلى الخادم", LogLevel.PRINT)
                    addLog("[Lemonade]: تم إنشاء مجلد leaderstats للاعب بنجاح", LogLevel.SUCCESS)
                    addLog("DataStore: تم جلب 2,450 نقطة و 15 جوهرة", LogLevel.PRINT)
                },
                modifier = Modifier.weight(1f).testTag("trigger_player_join"),
                colors = ButtonDefaults.filledTonalButtonColors(containerColor = StudioCardBg, contentColor = TextPrimary),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp)
            ) {
                Text("👤 دخول لاعب", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }

            FilledTonalButton(
                onClick = {
                    addLog("إرسال حدث RemoteEvent: ضربة سلاح / تنفيذ حركة", LogLevel.PRINT)
                    addLog("[Lemonade]: كشف إصابة الهدف 'TrainingDummy'! تطبيق 25 ضرر", LogLevel.SUCCESS)
                },
                modifier = Modifier.weight(1f).testTag("trigger_action"),
                colors = ButtonDefaults.filledTonalButtonColors(containerColor = StudioCardBg, contentColor = NeonLime),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp)
            ) {
                Text("⚔️ تنفيذ ضربة", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }

            FilledTonalButton(
                onClick = {
                    addLog("جاري حفظ بيانات اللاعبين المتصلين...", LogLevel.PRINT)
                    addLog("[Lemonade]: تم حفظ البيانات بنجاح في DataStoreService -> تمام", LogLevel.SUCCESS)
                },
                modifier = Modifier.weight(1f).testTag("trigger_save"),
                colors = ButtonDefaults.filledTonalButtonColors(containerColor = StudioCardBg, contentColor = LemonYellow),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp)
            ) {
                Text("💾 حفظ البيانات", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }

            FilledTonalButton(
                onClick = {
                    addLog("[تحذير Lemonade]: تم تفعيل فترة التهدئة للحدث! تم تجاهل السبام", LogLevel.WARN)
                },
                modifier = Modifier.weight(1f).testTag("trigger_warn"),
                colors = ButtonDefaults.filledTonalButtonColors(containerColor = StudioCardBg, contentColor = RobloxOrange),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp)
            ) {
                Text("⚠️ تجربة التهدئة", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Output Console Box
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .testTag("output_console_card"),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = StudioBlack),
            border = CardDefaults.outlinedCardBorder().copy(
                brush = androidx.compose.ui.graphics.SolidColor(StudioBorder)
            )
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Console Header & Filters
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF0F1522))
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Terminal, contentDescription = null, tint = LemonYellow, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "سجل مخرجات الاستوديو (Output Log)", style = MaterialTheme.typography.labelMedium, color = TextPrimary, fontWeight = FontWeight.Bold)
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = { logs = emptyList() },
                            modifier = Modifier.size(28.dp).testTag("clear_console_button")
                        ) {
                            Icon(imageVector = Icons.Default.DeleteSweep, contentDescription = "مسح", tint = TextMuted, modifier = Modifier.size(16.dp))
                        }
                    }
                }

                // Filter chips row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF0A0E17))
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    listOf("الكل", "المخرجات", "التحذيرات", "الأخطاء").forEach { filter ->
                        val isSelected = selectedFilter == filter
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = if (isSelected) LemonYellow.copy(alpha = 0.2f) else Color.Transparent,
                            modifier = Modifier.padding(2.dp)
                        ) {
                            Text(
                                text = filter,
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 10.sp,
                                color = if (isSelected) LemonYellow else TextMuted,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                modifier = Modifier
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                    .testTag("filter_$filter")
                            )
                        }
                    }
                }

                // Log Messages LazyColumn
                val filteredLogs = remember(logs, selectedFilter) {
                    when (selectedFilter) {
                        "المخرجات" -> logs.filter { it.level == LogLevel.PRINT || it.level == LogLevel.SUCCESS }
                        "التحذيرات" -> logs.filter { it.level == LogLevel.WARN }
                        "الأخطاء" -> logs.filter { it.level == LogLevel.ERROR }
                        else -> logs
                    }
                }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    if (filteredLogs.isEmpty()) {
                        item {
                            Text(
                                text = "-- سجل مخرجات الاستوديو فارغ حالياً --",
                                color = TextMuted,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                    }
                    items(filteredLogs) { log ->
                        val color = when (log.level) {
                            LogLevel.PRINT -> TextPrimary
                            LogLevel.WARN -> RobloxOrange
                            LogLevel.ERROR -> RobloxRed
                            LogLevel.SUCCESS -> RobloxGreen
                        }
                        val prefix = when (log.level) {
                            LogLevel.PRINT -> "ℹ"
                            LogLevel.WARN -> "⚠"
                            LogLevel.ERROR -> "✖"
                            LogLevel.SUCCESS -> "✔"
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.Top
                        ) {
                            Text(
                                text = "${log.timestamp} ",
                                color = TextMuted,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp
                            )
                            Text(
                                text = "$prefix ${log.message}",
                                color = color,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

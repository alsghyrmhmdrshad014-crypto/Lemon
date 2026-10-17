package com.example.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.PresetTemplates
import com.example.model.RobloxScript
import com.example.model.TemplateItem
import com.example.ui.theme.*

@Composable
fun TemplatesScreen(
    onSelectTemplateForGenerator: (RobloxScript) -> Unit,
    onTestInSimulator: (RobloxScript) -> Unit,
    onSaveScript: (RobloxScript) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedCategory by remember { mutableStateOf("الكل") }
    var expandedTemplateId by remember { mutableStateOf<String?>(null) }

    val categories = listOf("الكل", "الاقتصاد وحفظ البيانات", "القتال والأكشن", "الحيوانات الأليفة", "التايكون والبناء", "الأدمن والأدوات", "التفاعل والأبواب")

    val filteredTemplates = remember(selectedCategory) {
        if (selectedCategory == "الكل") {
            PresetTemplates.allTemplates
        } else {
            PresetTemplates.allTemplates.filter { it.category == selectedCategory }
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Column {
                Text(
                    text = "قوالب أنظمة روبلوكس الجاهزة",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = "سكربتات Luau مختبرة ومكتوبة باحترافية جاهزة للاستخدام في Roblox Studio فوراً.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
        }

        // Category Filter Row
        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(categories) { cat ->
                    val isSelected = selectedCategory == cat
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedCategory = cat },
                        label = { Text(cat, fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = LemonYellow,
                            selectedLabelColor = StudioBlack,
                            containerColor = StudioCardBg,
                            labelColor = TextSecondary
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isSelected,
                            borderColor = if (isSelected) LemonYellow else StudioBorder
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("template_category_$cat")
                    )
                }
            }
        }

        // Templates List
        items(filteredTemplates) { item ->
            val isExpanded = expandedTemplateId == item.id

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("template_card_${item.id}"),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = StudioCardBg),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = androidx.compose.ui.graphics.SolidColor(
                        if (isExpanded) LemonYellow else StudioBorder
                    )
                )
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                expandedTemplateId = if (isExpanded) null else item.id
                            },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = item.iconEmoji,
                            fontSize = 28.sp,
                            modifier = Modifier.padding(end = 12.dp)
                        )

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = item.title,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = item.shortDescription,
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary,
                                maxLines = if (isExpanded) 4 else 2
                            )
                        }

                        Icon(
                            imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = null,
                            tint = LemonYellow
                        )
                    }

                    if (isExpanded) {
                        Spacer(modifier = Modifier.height(14.dp))
                        CodeViewerCard(
                            script = item.script,
                            onTestInSimulator = onTestInSimulator,
                            onSaveScript = onSaveScript
                        )
                    }
                }
            }
        }
    }
}

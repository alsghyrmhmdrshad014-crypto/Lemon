package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ScriptRepository
import com.example.model.RobloxScript
import com.example.ui.theme.*

enum class LemonadeNavTab(val title: String, val icon: ImageVector) {
    GENERATOR("المولّد", Icons.Default.AutoAwesome),
    SIMULATOR("المحاكي", Icons.Default.Terminal),
    TEMPLATES("القوالب", Icons.Default.Widgets),
    CONNECT("ربط الاستوديو", Icons.Default.Link),
    SAVED("محفوظاتي", Icons.Default.Bookmarks),
    SETTINGS("معلومات", Icons.Default.Settings)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LemonadeApp() {
    val context = LocalContext.current
    val repository = remember { ScriptRepository(context) }
    val savedScripts by repository.savedScripts.collectAsState()

    var currentTab by remember { mutableStateOf(LemonadeNavTab.GENERATOR) }
    var activeScriptForSimulator by remember { mutableStateOf<RobloxScript?>(null) }

    BackHandler(enabled = currentTab != LemonadeNavTab.GENERATOR) {
        currentTab = LemonadeNavTab.GENERATOR
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = StudioDarkBg,
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(LemonYellow),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "🍋",
                                fontSize = 18.sp
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "lemonade",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp,
                                    color = TextPrimary
                                )
                                Text(
                                    text = ".gg",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp,
                                    color = LemonYellow
                                )
                            }
                            Text(
                                text = "استوديو روبلوكس الذكي",
                                style = MaterialTheme.typography.labelSmall,
                                color = NeonLime,
                                fontSize = 10.sp
                            )
                        }
                    }
                },
                actions = {
                    FilledTonalButton(
                        onClick = { currentTab = LemonadeNavTab.CONNECT },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = LemonYellow.copy(alpha = 0.2f),
                            contentColor = LemonYellow
                        ),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        modifier = Modifier.padding(end = 8.dp).testTag("top_bar_connect_button")
                    ) {
                        Icon(imageVector = Icons.Default.Link, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "ربط الاستوديو", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = StudioBlack,
                    titleContentColor = TextPrimary
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = StudioBlack,
                contentColor = TextSecondary,
                tonalElevation = 8.dp,
                modifier = Modifier.testTag("lemonade_bottom_nav")
            ) {
                LemonadeNavTab.values().forEach { tab ->
                    val isSelected = currentTab == tab
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { currentTab = tab },
                        icon = {
                            Icon(
                                imageVector = tab.icon,
                                contentDescription = tab.title,
                                tint = if (isSelected) LemonYellow else TextSecondary
                            )
                        },
                        label = {
                            Text(
                                text = tab.title,
                                color = if (isSelected) LemonYellow else TextSecondary,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 9.sp,
                                maxLines = 1
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            indicatorColor = LemonYellow.copy(alpha = 0.15f),
                            selectedIconColor = LemonYellow,
                            selectedTextColor = LemonYellow,
                            unselectedIconColor = TextSecondary,
                            unselectedTextColor = TextSecondary
                        ),
                        modifier = Modifier.testTag("nav_tab_${tab.name}")
                    )
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (currentTab) {
                LemonadeNavTab.GENERATOR -> {
                    GeneratorScreen(
                        onTestInSimulator = { script ->
                            activeScriptForSimulator = script
                            currentTab = LemonadeNavTab.SIMULATOR
                        },
                        onSaveScript = { script ->
                            repository.saveScript(script)
                        }
                    )
                }
                LemonadeNavTab.SIMULATOR -> {
                    SimulatorScreen(
                        currentScript = activeScriptForSimulator
                    )
                }
                LemonadeNavTab.TEMPLATES -> {
                    TemplatesScreen(
                        onSelectTemplateForGenerator = { script ->
                            activeScriptForSimulator = script
                            currentTab = LemonadeNavTab.GENERATOR
                        },
                        onTestInSimulator = { script ->
                            activeScriptForSimulator = script
                            currentTab = LemonadeNavTab.SIMULATOR
                        },
                        onSaveScript = { script ->
                            repository.saveScript(script)
                        }
                    )
                }
                LemonadeNavTab.CONNECT -> {
                    StudioConnectScreen()
                }
                LemonadeNavTab.SAVED -> {
                    SavedScriptsScreen(
                        savedScripts = savedScripts,
                        onDeleteScript = { id ->
                            repository.deleteScript(id)
                        },
                        onTestInSimulator = { script ->
                            activeScriptForSimulator = script
                            currentTab = LemonadeNavTab.SIMULATOR
                        }
                    )
                }
                LemonadeNavTab.SETTINGS -> {
                    SettingsScreen()
                }
            }
        }
    }
}

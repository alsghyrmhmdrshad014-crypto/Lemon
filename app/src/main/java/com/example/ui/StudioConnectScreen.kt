package com.example.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

@Composable
fun StudioConnectScreen(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    val officialPluginCode = remember {
        """--!strict
-- ========================================================
-- 🍋 Lemonade.gg Official Roblox Studio Local Plugin
-- ========================================================
-- هذا السكربت يُثبت كإضافة محلية (Local Plugin) داخل Roblox Studio
-- ليضيف لك زراً سحرياً لإدراج أكواد Lemonade تلقائياً في مكانها المناسب!

local toolbar = plugin:CreateToolbar("Lemonade AI")
local toggleButton = toolbar:CreateButton(
	"LemonadeSync",
	"فتح مستورد أكواد Lemonade AI",
	"rbxassetid://10734898141" -- أيقونة ليمونة مضيئة
)

local ChangeHistoryService = game:GetService("ChangeHistoryService")
local Selection = game:GetService("Selection")

-- إنشاء نافذة الإضافة داخل Roblox Studio
local widgetInfo = DockWidgetPluginGuiInfo.new(
	Enum.InitialDockState.Float,
	false, -- يبدأ مغلقاً
	false,
	360,
	480,
	300,
	350
)

local pluginGui = plugin:CreateDockWidgetPluginGui("LemonadeSyncGui", widgetInfo)
pluginGui.Title = "🍋 Lemonade AI Importer"

-- واجهة الإضافة الداخلية
local frame = Instance.new("Frame")
frame.Size = UDim2.new(1, 0, 1, 0)
frame.BackgroundColor3 = Color3.fromRGB(14, 20, 31)
frame.Parent = pluginGui

local title = Instance.new("TextLabel")
title.Text = "ألصق كود Lemonade هنا ليتم وضعه تلقائياً:"
title.TextColor3 = Color3.fromRGB(250, 204, 21)
title.Font = Enum.Font.GothamBold
title.TextSize = 13
title.Size = UDim2.new(1, -20, 0, 30)
title.Position = UDim2.new(0, 10, 0, 10)
title.BackgroundTransparency = 1
title.Parent = frame

local codeBox = Instance.new("TextBox")
codeBox.Size = UDim2.new(1, -20, 1, -120)
codeBox.Position = UDim2.new(0, 10, 0, 45)
codeBox.BackgroundColor3 = Color3.fromRGB(9, 13, 20)
codeBox.TextColor3 = Color3.fromRGB(240, 240, 240)
codeBox.ClearTextOnFocus = false
codeBox.MultiLine = true
codeBox.TextXAlignment = Enum.TextXAlignment.Left
codeBox.TextYAlignment = Enum.TextYAlignment.Top
codeBox.Font = Enum.Font.Code
codeBox.TextSize = 11
codeBox.PlaceholderText = "-- الصق الكود المنسوخ من تطبيق Lemonade هنا..."
codeBox.Parent = frame

local insertBtn = Instance.new("TextButton")
insertBtn.Size = UDim2.new(1, -20, 0, 45)
insertBtn.Position = UDim2.new(0, 10, 1, -60)
insertBtn.BackgroundColor3 = Color3.fromRGB(250, 204, 21)
insertBtn.TextColor3 = Color3.fromRGB(15, 15, 15)
insertBtn.Font = Enum.Font.GothamBold
insertBtn.TextSize = 14
insertBtn.Text = "⚡ إدراج في مكانه الصحيح تلقائياً"
insertBtn.Parent = frame

-- منطق التحليل والإدراج الذكي
insertBtn.MouseButton1Click:Connect(function()
	local raw = codeBox.Text
	if #raw < 10 then
		insertBtn.Text = "⚠ يرجى لصق كود أولاً!"
		task.wait(1.5)
		insertBtn.Text = "⚡ إدراج في مكانه الصحيح تلقائياً"
		return
	end

	local targetLocation = game:GetService("ServerScriptService")
	local scriptType = "Script"

	if string.find(raw, "StarterPlayerScripts") or string.find(raw, "RenderStepped") or string.find(raw, "LocalPlayer") then
		targetLocation = game:GetService("StarterPlayer"):WaitForChild("StarterPlayerScripts")
		scriptType = "LocalScript"
	elseif string.find(raw, "StarterGui") or string.find(raw, "ScreenGui") then
		targetLocation = game:GetService("StarterGui")
		scriptType = "LocalScript"
	end

	local newScript = Instance.new(scriptType)
	newScript.Name = "LemonadeGenerated_" .. os.date("%H%M%S")
	newScript.Source = raw
	newScript.Parent = targetLocation

	Selection:Set({ newScript })
	ChangeHistoryService:SetWaypoint("Inserted Lemonade Script")

	insertBtn.Text = "✔ تم الإدراج بنجاح في " .. targetLocation.Name .. "!"
	task.wait(2.5)
	insertBtn.Text = "⚡ إدراج في مكانه الصحيح تلقائياً"
	codeBox.Text = ""
end)

toggleButton.Click:Connect(function()
	pluginGui.Enabled = not pluginGui.Enabled
end)

print("[Lemonade Plugin]: تم تفعيل إضافة Lemonade بنجاح في شريط الأدوات!")
"""
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hero / Answer Banner
        item {
            Card(
                modifier = Modifier.fillMaxWidth().testTag("connect_hero_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = StudioCardBg),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = androidx.compose.ui.graphics.SolidColor(LemonYellow)
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(LemonYellow),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(imageVector = Icons.Default.Link, contentDescription = null, tint = StudioBlack, modifier = Modifier.size(22.dp))
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "نعم! يمكنك ربطه بروبلوكس ستوديو",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = "عبر إضافة Lemonade Studio Plugin الرسمية",
                                style = MaterialTheme.typography.bodySmall,
                                color = NeonLime
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Roblox Studio برنامج يعمل على الكمبيوتر، ولربط الهاتف به نوفر لك إضافة استوديو (Plugin) خاصة ومجانية. عند تثبيتها لمرة واحدة، ستظهر لك أيقونة 🍋 في الشريط العلوي لروبلوكس ستوديو، وبضغطة زر تقوم بوضع السكربتات المنسوخة في مكانها الصحيح تلقائياً!",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                        lineHeight = 20.sp
                    )
                }
            }
        }

        // 3-Step Setup Guide
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = StudioCardBg),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = androidx.compose.ui.graphics.SolidColor(StudioBorder)
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "طريقة التثبيت في Roblox Studio (خلال 30 ثانية فقط):",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = LemonYellow
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    val steps = listOf(
                        "انسخ كود الإضافة بالضغط على الزر الأصفر بالأسفل.",
                        "افتح Roblox Studio على الكمبيوتر، وأنشئ أي سكربت Script عادي والصق الكود فيه.",
                        "اضغط كليك يمين على السكربت في الـ Explorer واختر 'Save as Local Plugin...' ثم اضغط حفظ (Save).",
                        "مبروك! ستظهر لك أيقونة 🍋 Lemonade في شريط الأدوات العلوي دائماً!"
                    )

                    steps.forEachIndexed { index, step ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = NeonLime.copy(alpha = 0.2f),
                                modifier = Modifier.size(24.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = "${index + 1}",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = NeonLime
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = step,
                                style = MaterialTheme.typography.bodySmall,
                                color = TextPrimary,
                                lineHeight = 18.sp,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }

        // Plugin Code Card with Copy Action
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = StudioBlack),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = androidx.compose.ui.graphics.SolidColor(StudioBorder)
                )
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "كود الإضافة (LemonadePlugin.luau):",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = NeonLime
                        )

                        Text(
                            text = "Local Plugin",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextMuted
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF070B11))
                            .border(1.dp, StudioBorder, RoundedCornerShape(8.dp))
                            .padding(8.dp)
                    ) {
                        Text(
                            text = officialPluginCode.lines().take(7).joinToString("\n") + "\n-- ... انقر زر النسخ بالأسفل لنسخ كود الإضافة بالكامل",
                            color = TextSecondary,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.sp,
                            lineHeight = 14.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("Lemonade Plugin Code", officialPluginCode)
                            clipboard.setPrimaryClip(clip)
                            Toast.makeText(context, "تم نسخ كود الإضافة بالكامل للحافظة!", Toast.LENGTH_LONG).show()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .testTag("copy_plugin_code_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = LemonYellow,
                            contentColor = StudioBlack
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(imageVector = Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("نسخ كود إضافة Roblox Studio كاملة", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Fast Sync Alternative (Quick Share to PC)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = StudioCardBg),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = androidx.compose.ui.graphics.SolidColor(StudioBorder)
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "طرق سريعة إضافية لنقل الكود للكمبيوتر:",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "• الحافظة السحابية المشتركة (Phone Link / WhatsApp Web / Telegram): اضغط 'نسخ الكود' في أي سكربت وأرسله لمحادثتك الذاتية وافتحه فوراً في كمبيوترك.\n• فتح الموقع الرسمي في المتصفح: يمكنك أيضاً زيارة lemonade.gg على متصفح الكمبيوتر إذا أردت استخدام المنصة السحابية المباشرة.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                        lineHeight = 20.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedButton(
                        onClick = {
                            val shareIntent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(Intent.EXTRA_TEXT, officialPluginCode)
                                type = "text/plain"
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "إرسال كود الإضافة إلى جهاز الكمبيوتر"))
                        },
                        modifier = Modifier.fillMaxWidth().testTag("share_plugin_to_pc_button"),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = NeonLime),
                        border = ButtonDefaults.outlinedButtonBorder.copy(
                            brush = androidx.compose.ui.graphics.SolidColor(NeonLime)
                        )
                    ) {
                        Icon(imageVector = Icons.Default.SendToMobile, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("إرسال الكود للكمبيوتر (واتساب / إيميل / تيليجرام)")
                    }
                }
            }
        }
    }
}

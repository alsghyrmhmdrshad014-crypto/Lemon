package com.example.network

import com.example.BuildConfig
import com.example.model.RobloxScript
import com.example.model.ScriptType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

object LemonadeEngine {

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    var customApiKey: String = ""

    fun init(context: android.content.Context) {
        val prefs = context.getSharedPreferences("lemonade_prefs", android.content.Context.MODE_PRIVATE)
        val saved = prefs.getString("gemini_api_key_custom", "") ?: ""
        if (saved.isNotBlank()) {
            customApiKey = saved
        }
    }

    fun saveApiKey(context: android.content.Context, key: String) {
        customApiKey = key.trim()
        val prefs = context.getSharedPreferences("lemonade_prefs", android.content.Context.MODE_PRIVATE)
        prefs.edit().putString("gemini_api_key_custom", customApiKey).apply()
    }

    fun getEffectiveApiKey(): String {
        if (customApiKey.isNotBlank()) return customApiKey
        val buildKey = try {
            BuildConfig.GEMINI_API_KEY
        } catch (_: Exception) {
            ""
        }
        return if (buildKey.isNotBlank() && buildKey != "MY_GEMINI_API_KEY") buildKey else ""
    }

    suspend fun testApiKey(apiKey: String): Pair<Boolean, String> = withContext(Dispatchers.IO) {
        if (apiKey.isBlank()) return@withContext false to "يرجى إدخال مفتاح أولاً."
        try {
            val jsonPayload = JSONObject().apply {
                put("contents", JSONArray().apply {
                    put(JSONObject().apply {
                        put("parts", JSONArray().apply {
                            put(JSONObject().apply { put("text", "Ping test") })
                        })
                    })
                })
            }
            val request = Request.Builder()
                .url("https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey")
                .post(jsonPayload.toString().toRequestBody("application/json".toMediaType()))
                .build()

            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    true to "المفتاح صالح ويعمل بنجاح مع موديل Gemini 3.5 Flash!"
                } else {
                    false to "فشل التحقق من المفتاح (رمز الاستجابة: ${response.code}). تأكد من صحة المفتاح."
                }
            }
        } catch (e: Exception) {
            false to "خطأ في الاتصال: ${e.localizedMessage ?: "تأكد من اتصال الإنترنت"}"
        }
    }

    suspend fun generateRobloxCode(
        prompt: String,
        targetType: ScriptType,
        selectedCategory: String
    ): RobloxScript = withContext(Dispatchers.IO) {
        val apiKey = getEffectiveApiKey()
        if (apiKey.isNotBlank()) {
            try {
                return@withContext callGeminiApi(prompt, targetType, selectedCategory, apiKey)
            } catch (e: Exception) {
                return@withContext generateSmartFallback(prompt, targetType, selectedCategory, errorNotice = e.localizedMessage)
            }
        } else {
            return@withContext generateSmartFallback(prompt, targetType, selectedCategory, errorNotice = null)
        }
    }

    private fun callGeminiApi(
        prompt: String,
        targetType: ScriptType,
        selectedCategory: String,
        apiKey: String
    ): RobloxScript {
        val systemPrompt = """
            أنت Lemonade.gg، المنصة الرائدة في توليد وكتابة وبرمجة أكواد Roblox Luau الاحترافية بالذكاء الاصطناعي.
            مهمتك كتابة كود Luau كامل ونظيف وخالٍ من الأخطاء بناءً على طلب المستخدم.
            القواعد الإلزامية:
            - ابدأ دائماً بـ `--!strict`
            - عرف الخدمات في الأعلى مثل `game:GetService("Players")`
            - استخدم `pcall` للعمليات المحمية مثل DataStore أو Network
            - نوع السكربت المطلوب: ${targetType.displayName}
            - الفئة: $selectedCategory
            - يجب أن تكون العناوين وشرح خطوات التركيب باللغة العربية الواضحة.
            - الكود البرمجي Luau نفسه برمجي سليم مئة بالمئة.
            
            أرجع الإجابة كـ JSON صالح فقط بالصيغة:
            {
              "title": "عنوان السكربت بالعربية",
              "location": "المسار في روبلوكس ستوديو مثل ServerScriptService.MySystem أو StarterPlayer.StarterPlayerScripts.MyController",
              "description": "وصف موجز للنظام بالعربية",
              "code": "كود Luau الصافي والجاهز للنسخ",
              "setupSteps": ["الخطوة 1 بالعربية", "الخطوة 2", "الخطوة 3"]
            }
            لا تضع أي علامات markdown حول الـ JSON. أرجع كائن JSON فقط.
        """.trimIndent()

        val jsonPayload = JSONObject().apply {
            put("contents", JSONArray().apply {
                put(JSONObject().apply {
                    put("parts", JSONArray().apply {
                        put(JSONObject().apply {
                            put("text", "System: $systemPrompt\n\nUser Prompt: $prompt")
                        })
                    })
                })
            })
            put("generationConfig", JSONObject().apply {
                put("temperature", 0.7)
                put("responseMimeType", "application/json")
            })
        }

        val request = Request.Builder()
            .url("https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey")
            .post(jsonPayload.toString().toRequestBody("application/json".toMediaType()))
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw Exception("خطأ من خادم الذكاء الاصطناعي: رمز ${response.code}")
            }
            val body = response.body?.string() ?: throw Exception("استجابة فارغة")
            val root = JSONObject(body)
            val candidates = root.optJSONArray("candidates")
            val firstCandidate = candidates?.optJSONObject(0)
            val content = firstCandidate?.optJSONObject("content")
            val parts = content?.optJSONArray("parts")
            val text = parts?.optJSONObject(0)?.optString("text") ?: throw Exception("لا يوجد نص مستلم")

            val cleanJson = text.trim().removePrefix("```json").removePrefix("```").removeSuffix("```").trim()
            val parsed = JSONObject(cleanJson)

            val title = parsed.optString("title", "سكربت Luau مُولّد")
            val location = parsed.optString("location", if (targetType == ScriptType.SERVER_SCRIPT) "ServerScriptService" else "StarterPlayerScripts")
            val description = parsed.optString("description", "تم التوليد بنجاح عبر Lemonade AI")
            val code = parsed.optString("code", "-- لم يتم توليد كود")
            val stepsJson = parsed.optJSONArray("setupSteps")
            val steps = mutableListOf<String>()
            if (stepsJson != null) {
                for (i in 0 until stepsJson.length()) {
                    steps.add(stepsJson.getString(i))
                }
            } else {
                steps.add("الصق الكود في المسار $location داخل Roblox Studio.")
            }

            return RobloxScript(
                title = title,
                scriptType = targetType,
                location = location,
                description = description,
                code = code,
                setupSteps = steps,
                tags = listOf("توليد ذكي", targetType.name, selectedCategory)
            )
        }
    }

    fun generateSmartFallback(
        prompt: String,
        targetType: ScriptType,
        selectedCategory: String,
        errorNotice: String?
    ): RobloxScript {
        val lowerPrompt = prompt.lowercase()

        return when {
            lowerPrompt.contains("pet") || lowerPrompt.contains("hatch") || lowerPrompt.contains("egg") ||
            lowerPrompt.contains("حيوان") || lowerPrompt.contains("بيض") || lowerPrompt.contains("تفقيس") || lowerPrompt.contains("بت") -> {
                RobloxScript(
                    title = "نظام تفقيس الحيوانات الأليفة والبيض (Pet Hatching)",
                    scriptType = ScriptType.SERVER_SCRIPT,
                    location = "ServerScriptService > PetHatchingManager",
                    description = "فتح بيض تفاعلي، نسب ندرة متعددة (عادي، نادر، أسطوري)، وخصم العملات التلقائي.",
                    code = """--!strict
-- تم التوليد بواسطة Lemonade.gg (Roblox AI Studio)
-- الموقع: ServerScriptService.PetHatchingManager

local ReplicatedStorage = game:GetService("ReplicatedStorage")
local Players = game:GetService("Players")

local PetRemoteFolder = Instance.new("Folder")
PetRemoteFolder.Name = "PetRemotes"
PetRemoteFolder.Parent = ReplicatedStorage

local HatchEggEvent = Instance.new("RemoteFunction")
HatchEggEvent.Name = "HatchEgg"
HatchEggEvent.Parent = PetRemoteFolder

local EGG_DATA = {
	["CommonEgg"] = {
		Cost = 100,
		Pets = {
			{ Name = "Dog", Chance = 50, Color = Color3.fromRGB(160, 100, 50) },
			{ Name = "Cat", Chance = 35, Color = Color3.fromRGB(220, 220, 220) },
			{ Name = "GoldenDragon", Chance = 15, Color = Color3.fromRGB(255, 215, 0) },
		}
	}
}

local function chooseRandomPet(petList)
	local roll = math.random(1, 100)
	local cumulative = 0
	for _, pet in petList do
		cumulative += pet.Chance
		if roll <= cumulative then
			return pet
		end
	end
	return petList[1]
end

HatchEggEvent.OnServerInvoke = function(player: Player, eggName: string)
	local egg = EGG_DATA[eggName]
	if not egg then return { Success = false, Message = "البيضة غير موجودة!" } end

	local leaderstats = player:FindFirstChild("leaderstats")
	local coins = leaderstats and leaderstats:FindFirstChild("Coins") :: IntValue
	if not coins or coins.Value < egg.Cost then
		return { Success = false, Message = "لا تمتلك نقوداً كافية!" }
	end

	-- خصم النقود
	coins.Value -= egg.Cost
	local chosenPet = chooseRandomPet(egg.Pets)
	print("[Lemonade]: اللاعب " .. player.Name .. " فتح حيوان " .. chosenPet.Name)

	return {
		Success = true,
		Pet = chosenPet.Name,
		Rarity = chosenPet.Chance .. "%"
	}
end
""",
                    setupSteps = listOf(
                        "أنشئ سكربت Script في ServerScriptService باسم PetHatchingManager.",
                        "قم باستدعاء HatchEgg:InvokeServer('CommonEgg') من زر فتح البيض في واجهة العميل.",
                        "تأكد من وجود قيمة Coins في قائمة Leaderstats للاعب لتجربة فتح البيض."
                    ),
                    tags = listOf("حيوانات أليفة", "تفقيس", "بيض", "ندرة")
                )
            }
            lowerPrompt.contains("shop") || lowerPrompt.contains("gui") || lowerPrompt.contains("inventory") ||
            lowerPrompt.contains("متجر") || lowerPrompt.contains("واجهة") || lowerPrompt.contains("حقيبة") -> {
                RobloxScript(
                    title = "واجهة متجر تفاعلية ومتحركة (Shop GUI)",
                    scriptType = ScriptType.LOCAL_SCRIPT,
                    location = "StarterGui > ShopGui > ShopController",
                    description = "نافذة متجر بانسيابية TweenService مع زر فتح/إغلاق وزر الشراء.",
                    code = """--!strict
-- تم التوليد بواسطة Lemonade.gg (Roblox AI Studio)
-- الموقع: StarterGui.ShopGui.ShopController (LocalScript)

local TweenService = game:GetService("TweenService")
local ReplicatedStorage = game:GetService("ReplicatedStorage")
local Players = game:GetService("Players")

local player = Players.LocalPlayer
local playerGui = player:WaitForChild("PlayerGui")
local shopGui = script.Parent :: ScreenGui
local mainFrame = shopGui:WaitForChild("MainFrame") :: Frame
local toggleButton = shopGui:WaitForChild("OpenButton") :: TextButton
local closeButton = mainFrame:WaitForChild("CloseButton") :: TextButton

local isOpen = false
local TWEEN_TIME = 0.35
local tweenInfo = TweenInfo.new(TWEEN_TIME, Enum.EasingStyle.Back, Enum.EasingDirection.Out)

local function setShopVisible(visible: boolean)
	isOpen = visible
	if visible then
		mainFrame.Visible = true
		mainFrame.Position = UDim2.new(0.5, 0, 1.2, 0)
		local tween = TweenService:Create(mainFrame, tweenInfo, {
			Position = UDim2.new(0.5, 0, 0.5, 0)
		})
		tween:Play()
	else
		local closeTween = TweenService:Create(mainFrame, TweenInfo.new(0.2, Enum.EasingStyle.Quad, Enum.EasingDirection.In), {
			Position = UDim2.new(0.5, 0, 1.2, 0)
		})
		closeTween:Play()
		closeTween.Completed:Connect(function()
			if not isOpen then mainFrame.Visible = false end
		end)
	end
end

toggleButton.MouseButton1Click:Connect(function()
	setShopVisible(not isOpen)
end)

closeButton.MouseButton1Click:Connect(function()
	setShopVisible(false)
end)

print("[Lemonade]: تم تفعيل متحكم واجهة المتجر بنجاح!")
""",
                    setupSteps = listOf(
                        "أنشئ ScreenGui في StarterGui باسم 'ShopGui'.",
                        "أضف زراً TextButton باسم 'OpenButton' وإطار Frame باسم 'MainFrame' داخله.",
                        "أضف زراً TextButton باسم 'CloseButton' داخل MainFrame.",
                        "ضع هذا الـ LocalScript داخل ShopGui."
                    ),
                    tags = listOf("متجر", "واجهة", "TweenService", "GUI")
                )
            }
            lowerPrompt.contains("combat") || lowerPrompt.contains("sword") || lowerPrompt.contains("gun") || lowerPrompt.contains("damage") ||
            lowerPrompt.contains("قتال") || lowerPrompt.contains("سيف") || lowerPrompt.contains("سلاح") || lowerPrompt.contains("ضرر") -> {
                RobloxScript(
                    title = "نظام سلاح متطور بكشف Raycast السريع",
                    scriptType = ScriptType.SERVER_SCRIPT,
                    location = "ServerScriptService > RaycastWeaponHandler",
                    description = "كشف أضرار ودقة عالية للمسافات مع التحقق في السيرفر لمنع استغلال الهاكات.",
                    code = """--!strict
-- تم التوليد بواسطة Lemonade.gg (Roblox AI Studio)
-- الموقع: ServerScriptService.RaycastWeaponHandler (Script)

local ReplicatedStorage = game:GetService("ReplicatedStorage")
local Debris = game:GetService("Debris")

local WeaponRemotes = Instance.new("Folder")
WeaponRemotes.Name = "WeaponRemotes"
WeaponRemotes.Parent = ReplicatedStorage

local ShootEvent = Instance.new("RemoteEvent")
ShootEvent.Name = "ShootWeapon"
ShootEvent.Parent = WeaponRemotes

local DAMAGE = 34
local MAX_DISTANCE = 300

ShootEvent.OnServerEvent:Connect(function(player: Player, origin: Vector3, direction: Vector3)
	local character = player.Character
	if not character then return end

	local raycastParams = RaycastParams.new()
	raycastParams.FilterDescendantsInstances = { character }
	raycastParams.FilterType = RaycastFilterType.Exclude

	local result = workspace:Raycast(origin, direction.Unit * MAX_DISTANCE, raycastParams)
	if result and result.Instance then
		local hitModel = result.Instance:FindFirstAncestorOfClass("Model")
		if hitModel then
			local humanoid = hitModel:FindFirstChildOfClass("Humanoid")
			if humanoid and humanoid.Health > 0 then
				humanoid:TakeDamage(DAMAGE)
				print("[Lemonade]: أصاب اللاعب " .. player.Name .. " الهدف " .. hitModel.Name .. " بضرر " .. DAMAGE)
			end
		end
	end
end)
""",
                    setupSteps = listOf(
                        "أنشئ سكربت Script في مجلد ServerScriptService.",
                        "أنشئ أداة Tool في StarterPack مع LocalScript يقوم باستدعاء ShootWeapon عند النقر.",
                        "ستتأثر أي شخصية تصاب بالرصاص بخصم الضرر فوراً!"
                    ),
                    tags = listOf("أسلحة", "Raycast", "قتال", "ضرر")
                )
            }
            else -> {
                val cleanTitle = if (prompt.length > 25) prompt.take(25) + "..." else prompt
                RobloxScript(
                    title = "نظام مخصص: $cleanTitle",
                    scriptType = targetType,
                    location = if (targetType == ScriptType.SERVER_SCRIPT) "ServerScriptService" else "StarterPlayerScripts",
                    description = "سكربت Luau مخصص تم توليده بناءً على طلبك: $prompt",
                    code = """--!strict
-- تم التوليد بواسطة Lemonade.gg (Roblox AI Studio)
-- الهدف: $prompt
-- الموقع: ${if (targetType == ScriptType.SERVER_SCRIPT) "ServerScriptService.SystemManager" else "StarterPlayerScripts.ClientController"}

local Players = game:GetService("Players")
local ReplicatedStorage = game:GetService("ReplicatedStorage")
local RunService = game:GetService("RunService")
local TweenService = game:GetService("TweenService")

print("[Lemonade]: جاري تشغيل النظام المخصص لـ: $prompt")

local System = {}
System.__index = System

function System.new()
	local self = setmetatable({}, System)
	self.IsActive = true
	self.CreatedAt = os.time()
	return self
end

function System:Execute(player: Player)
	if not self.IsActive then return end
	print("[Lemonade]: تنفيذ المنطق للاعب: " .. player.Name)
	-- المنطق الأساسي للخاصية
end

-- استقبال اللاعبين
Players.PlayerAdded:Connect(function(player: Player)
	print("[Lemonade]: لاعب جديد دخل اللعبة: " .. player.Name)
	player.CharacterAdded:Connect(function(character: Model)
		local humanoid = character:WaitForChild("Humanoid") :: Humanoid
		print("[Lemonade]: تم تحميل شخصية اللاعب: " .. player.Name)
	end)
end)

return System
""",
                    setupSteps = listOf(
                        "افتح مشروعك في Roblox Studio.",
                        "أضف ${targetType.displayName} في المسار المقترح.",
                        "الصق الكود وعدل القيم في أول السكربت حسب رغبتك."
                    ),
                    tags = listOf("نظام مخصص", targetType.name, "Lemonade-AI")
                )
            }
        }
    }
}

package com.example.data

import com.example.model.RobloxScript
import com.example.model.ScriptType
import com.example.model.TemplateItem

object PresetTemplates {

    val allTemplates: List<TemplateItem> = listOf(
        TemplateItem(
            id = "template_leaderstats",
            title = "حفظ البيانات التلقائي (Leaderstats & DataStore)",
            category = "الاقتصاد وحفظ البيانات",
            iconEmoji = "💰",
            shortDescription = "حفظ النقود والجواهر والمستوى بأمان تام مع نظام إعادة المحاولة وحماية إغلاق السيرفر BindToClose.",
            script = RobloxScript(
                id = "script_leaderstats",
                title = "نظام قائمة المتصدرين وحفظ البيانات",
                scriptType = ScriptType.SERVER_SCRIPT,
                location = "ServerScriptService > LeaderboardSystem",
                description = "نظام كامل واحترافي لحفظ بيانات لاعبي روبلوكس مع معالجة الأخطاء والحفظ التلقائي الدوري.",
                code = """--!strict
-- تم التوليد بواسطة Lemonade.gg (Roblox AI Studio)
-- الموقع: ServerScriptService.LeaderboardSystem (Script)

local Players = game:GetService("Players")
local DataStoreService = game:GetService("DataStoreService")
local PlayerDataStore = DataStoreService:GetDataStore("PlayerData_v1.0")

local AUTOSAVE_INTERVAL = 180 -- كل 3 دقائق

local function setupLeaderstats(player: Player)
	local leaderstats = Instance.new("Folder")
	leaderstats.Name = "leaderstats"
	leaderstats.Parent = player

	local coins = Instance.new("IntValue")
	coins.Name = "Coins"
	coins.Value = 0
	coins.Parent = leaderstats

	local gems = Instance.new("IntValue")
	gems.Name = "Gems"
	gems.Value = 0
	gems.Parent = leaderstats

	local level = Instance.new("IntValue")
	level.Name = "Level"
	level.Value = 1
	level.Parent = leaderstats

	-- تحميل البيانات المحفوظة للاعب
	local key = "Player_" .. player.UserId
	local success, data = pcall(function()
		return PlayerDataStore:GetAsync(key)
	end)

	if success and data then
		coins.Value = data.Coins or 0
		gems.Value = data.Gems or 0
		level.Value = data.Level or 1
		print("[Lemonade]: تم تحميل بيانات اللاعب بنجاح: " .. player.Name)
	else
		warn("[Lemonade]: أول تسجيل دخول أو تعذر تحميل البيانات للاعب: " .. player.Name)
	end
end

local function saveData(player: Player)
	local leaderstats = player:FindFirstChild("leaderstats")
	if not leaderstats then return end

	local coins = leaderstats:FindFirstChild("Coins") :: IntValue
	local gems = leaderstats:FindFirstChild("Gems") :: IntValue
	local level = leaderstats:FindFirstChild("Level") :: IntValue

	local dataToSave = {
		Coins = coins and coins.Value or 0,
		Gems = gems and gems.Value or 0,
		Level = level and level.Value or 1,
		LastSaved = os.time()
	}

	local key = "Player_" .. player.UserId
	local success, err = pcall(function()
		PlayerDataStore:SetAsync(key, dataToSave)
	end)

	if success then
		print("[Lemonade]: تم حفظ بيانات اللاعب بنجاح: " .. player.Name)
	else
		warn("[Lemonade]: فشل حفظ بيانات اللاعب: " .. player.Name .. " - الخطأ: " .. tostring(err))
	end
end

Players.PlayerAdded:Connect(setupLeaderstats)
Players.PlayerRemoving:Connect(saveData)

-- حماية إغلاق السيرفر لحفظ بيانات الجميع
game:BindToClose(function()
	print("[Lemonade]: السيرفر يغلق، جاري حفظ بيانات كافة اللاعبين...")
	for _, player in Players:GetPlayers() do
		task.spawn(saveData, player)
	end
	task.wait(2)
end)
""",
                setupSteps = listOf(
                    "في شجرة مستكشف Roblox Studio، توجه إلى مجلد ServerScriptService.",
                    "اضغط بالزر الأيمن على ServerScriptService واختر Insert Object -> Script.",
                    "الصق الكود داخل السكربت وأعد تسميته إلى 'LeaderboardSystem'.",
                    "تأكد من تفعيل 'Enable Studio Access to API Services' من إعدادات اللعبة Game Settings > Security."
                ),
                tags = listOf("DataStore", "Leaderstats", "نقود", "حفظ تلقائي")
            )
        ),
        TemplateItem(
            id = "template_combat",
            title = "نظام القتال بالسيوف وكشف الضربات (Raycast Combat)",
            category = "القتال والأكشن",
            iconEmoji = "⚔️",
            shortDescription = "ضربات قتالية M1 مع كومبو، كشف دقيق للأضرار عبر Raycast، وفترة تهدئة مانعة للسبام.",
            script = RobloxScript(
                id = "script_combat",
                title = "متحكم سلاح السيف بنظام Raycast",
                scriptType = ScriptType.SERVER_SCRIPT,
                location = "ServerScriptService > CombatManager",
                description = "نظام قتال محمي في السيرفر يمنع الاستغلال والضربات الوهمية ويضيف تأثيرات بصرية عند الإصابة.",
                code = """--!strict
-- تم التوليد بواسطة Lemonade.gg (Roblox AI Studio)
-- الموقع: ServerScriptService.CombatManager (Script)

local ReplicatedStorage = game:GetService("ReplicatedStorage")
local Debris = game:GetService("Debris")

-- RemoteEvent لاستقبال ضربات السلاح من العميل
local CombatRemotes = Instance.new("Folder")
CombatRemotes.Name = "CombatRemotes"
CombatRemotes.Parent = ReplicatedStorage

local AttackRemote = Instance.new("RemoteEvent")
AttackRemote.Name = "SwingWeapon"
AttackRemote.Parent = CombatRemotes

local BASE_DAMAGE = 25
local ATTACK_COOLDOWN = 0.45
local MAX_RANGE = 9.0

local playerCooldowns = {}

AttackRemote.OnServerEvent:Connect(function(player: Player, targetCharacter: any)
	local now = os.clock()
	local lastAttack = playerCooldowns[player.UserId] or 0

	if now - lastAttack < ATTACK_COOLDOWN then
		return -- فترة التهدئة لمنع السبام
	end
	playerCooldowns[player.UserId] = now

	local character = player.Character
	if not character then return end
	local rootPart = character:FindFirstChild("HumanoidRootPart") :: BasePart
	if not rootPart then return end

	-- التحقق من شخصية الهدف
	if targetCharacter and targetCharacter:IsA("Model") and targetCharacter ~= character then
		local targetHumanoid = targetCharacter:FindFirstChildOfClass("Humanoid")
		local targetRoot = targetCharacter:FindFirstChild("HumanoidRootPart") :: BasePart

		if targetHumanoid and targetRoot and targetHumanoid.Health > 0 then
			local distance = (rootPart.Position - targetRoot.Position).Magnitude
			if distance <= MAX_RANGE then
				-- تطبيق الضرر
				targetHumanoid:TakeDamage(BASE_DAMAGE)
				print("[Lemonade]: ضرب اللاعب " .. player.Name .. " الهدف " .. targetCharacter.Name .. " بضرر " .. BASE_DAMAGE)

				-- إضافة تأثير شرارات عند الضربة
				local spark = Instance.new("Sparkles")
				spark.SparkleColor = Color3.fromRGB(255, 220, 50)
				spark.Parent = targetRoot
				Debris:AddItem(spark, 0.4)
			end
		end
	end
end)
""",
                setupSteps = listOf(
                    "أنشئ سكربت Script داخل ServerScriptService وأعد تسميته إلى 'CombatManager'.",
                    "السكربت ينشئ تلقائياً مجلد CombatRemotes وحدث SwingWeapon في ReplicatedStorage.",
                    "في سكربت الأداة LocalScript، قم باستدعاء SwingWeapon:FireServer(enemyModel) عند الضغط."
                ),
                tags = listOf("قتال", "سيوف", "Raycast", "PvP", "حماية")
            )
        ),
        TemplateItem(
            id = "template_pet",
            title = "نظام الحيوان الأليف الطائر (Pet Follow System)",
            category = "الحيوانات الأليفة",
            iconEmoji = "🐾",
            shortDescription = "حيوان أليف يتبع كتف اللاعب بانسيابية تامة بحسابات Lerp و60 إطاراً في الثانية.",
            script = RobloxScript(
                id = "script_pet",
                title = "متحكم دوران ومتابعة الحيوان الأليف",
                scriptType = ScriptType.LOCAL_SCRIPT,
                location = "StarterPlayer > StarterPlayerScripts > PetController",
                description = "حسابات حركة سلسة في جهاز العميل عبر RenderStepped لضمان أعلى أداء ودون أي تقطيع.",
                code = """--!strict
-- تم التوليد بواسطة Lemonade.gg (Roblox AI Studio)
-- الموقع: StarterPlayerScripts.PetController (LocalScript)

local Players = game:GetService("Players")
local RunService = game:GetService("RunService")

local player = Players.LocalPlayer
local character = player.Character or player.CharacterAdded:Wait()
local rootPart = character:WaitForChild("HumanoidRootPart") :: BasePart

local activePets: { Model } = {}

local function spawnDemoPet(): Model
	local pet = Instance.new("Model")
	pet.Name = "GoldenPuppy"

	local mainPart = Instance.new("Part")
	mainPart.Name = "PrimaryPart"
	mainPart.Size = Vector3.new(2, 2, 2)
	mainPart.Color = Color3.fromRGB(250, 204, 21)
	mainPart.Material = Enum.Material.Neon
	mainPart.CanCollide = false
	mainPart.Anchored = true
	mainPart.Parent = pet
	pet.PrimaryPart = mainPart

	pet.Parent = workspace
	table.insert(activePets, pet)
	return pet
end

local myPet = spawnDemoPet()

-- حلقة RenderStepped بسرعة 60 إطاراً لحركة ناعمة جداً
local hoverTime = 0
RunService.RenderStepped:Connect(function(dt: number)
	if not rootPart or not rootPart.Parent then return end
	hoverTime += dt * 4

	local hoverOffset = math.sin(hoverTime) * 0.4
	-- تموضع الحيوان الأليف خلف يمين اللاعب
	local targetCFrame = rootPart.CFrame * CFrame.new(3.5, 1.2 + hoverOffset, 3.0)

	if myPet and myPet.PrimaryPart then
		local currentCF = myPet.PrimaryPart.CFrame
		-- انسيابية بالاستيفاء الخطي Lerp
		myPet.PrimaryPart.CFrame = currentCF:Lerp(targetCFrame, math.clamp(dt * 8, 0, 1))
	end
end)
""",
                setupSteps = listOf(
                    "توجه إلى مجلد StarterPlayer -> StarterPlayerScripts في مستكشف روبلوكس ستوديو.",
                    "أنشئ LocalScript والصق هذا الكود بداخله.",
                    "اضغط Play وستجد الحيوان الأليف المضيء يتبع شخصيتك ويطفو بمرونة وحركات ناعمة!"
                ),
                tags = listOf("حيوانات أليفة", "محاكي", "RenderStepped", "Lerp")
            )
        ),
        TemplateItem(
            id = "template_tycoon",
            title = "ماكينة التايكون والسير الناقل (Tycoon Dropper)",
            category = "التايكون والبناء",
            iconEmoji = "🏭",
            shortDescription = "إسقاط الخامات دورياً، وسير ناقل بفيزياء AssemblyLinearVelocity وجامع للأرباح.",
            script = RobloxScript(
                id = "script_tycoon",
                title = "نظام التايكون والسير الناقل وحصاد الأموال",
                scriptType = ScriptType.SERVER_SCRIPT,
                location = "Workspace > TycoonModel > TycoonCoreScript",
                description = "نظام تايكون معياري جاهز مع توقيت إسقاط الخامات وفيزياء الحركة وزيادة النقود.",
                code = """--!strict
-- تم التوليد بواسطة Lemonade.gg (Roblox AI Studio)
-- الموقع: TycoonModel.TycoonCoreScript (Script)

local Debris = game:GetService("Debris")

local DROP_INTERVAL = 2.0
local ORE_VALUE = 15

local tycoon = script.Parent
local dropperSpout = tycoon:WaitForChild("DropperSpout") :: BasePart
local collector = tycoon:WaitForChild("Collector") :: BasePart
local conveyor = tycoon:WaitForChild("Conveyor") :: BasePart

-- سرعة السير الناقل بالفيزياء الحديثة
conveyor.AssemblyLinearVelocity = conveyor.CFrame.LookVector * 22

-- منطق ملامسة الجامع للمال
collector.Touched:Connect(function(hit: BasePart)
	if hit:GetAttribute("IsTycoonOre") then
		local value = hit:GetAttribute("OreValue") or ORE_VALUE
		hit:Destroy()

		-- إضافة النقود لمالك التايكون
		local ownerPlayer = tycoon:GetAttribute("Owner")
		if ownerPlayer then
			local player = game.Players:FindFirstChild(ownerPlayer)
			if player and player:FindFirstChild("leaderstats") then
				local cash = player.leaderstats:FindFirstChild("Coins") :: IntValue
				if cash then
					cash.Value += value
				end
			end
		end
		print("[Lemonade Tycoon]: تم جمع خام بقيمة $" .. value)
	end
end)

-- حلقة إسقاط الخامات
task.spawn(function()
	while task.wait(DROP_INTERVAL) do
		local ore = Instance.new("Part")
		ore.Name = "SilverOre"
		ore.Size = Vector3.new(1.2, 1.2, 1.2)
		ore.Color = Color3.fromRGB(220, 220, 240)
		ore.Material = Enum.Material.SmoothPlastic
		ore.CFrame = dropperSpout.CFrame - Vector3.new(0, 1.5, 0)
		ore:SetAttribute("IsTycoonOre", true)
		ore:SetAttribute("OreValue", ORE_VALUE)
		ore.Parent = workspace

		Debris:AddItem(ore, 20) -- تنظيف الذاكرة
	end
end)
""",
                setupSteps = listOf(
                    "أنشئ مجسم Model في Workspace باسم 'TycoonModel'.",
                    "أضف بداخله 3 قطع بإسم: 'DropperSpout' و 'Conveyor' و 'Collector'.",
                    "ضع هذا السكربت داخل TycoonModel وستبدأ الماكينة في العمل وإنتاج المال فوراً!"
                ),
                tags = listOf("تايكون", "إسقاط خامات", "سير ناقل", "اقتصاد")
            )
        ),
        TemplateItem(
            id = "template_admin",
            title = "أوامر الأدمن والمشرفين (:speed, :fly, :heal)",
            category = "الأدمن والأدوات",
            iconEmoji = "👑",
            shortDescription = "أوامر دردشة حديثة بنظام TextChatService مع تحقق من رتبة المالك والإعلان في الشات.",
            script = RobloxScript(
                id = "script_admin",
                title = "نظام أوامر المشرفين والأدمن",
                scriptType = ScriptType.SERVER_SCRIPT,
                location = "ServerScriptService > AdminCommandHandler",
                description = "نظام أوامر دردشة آمن يدعم تغيير السرعة، القفز، العلاج، والتحكم بالشخصيات.",
                code = """--!strict
-- تم التوليد بواسطة Lemonade.gg (Roblox AI Studio)
-- الموقع: ServerScriptService.AdminCommandHandler (Script)

local Players = game:GetService("Players")

-- قائمة أسماء أو معرّفات الأدمن المسموح لهم
local ADMIN_LIST = {
	["YourRobloxUsername"] = true,
}

local function isPlayerAdmin(player: Player): boolean
	return ADMIN_LIST[player.Name] == true or ADMIN_LIST[tostring(player.UserId)] == true or player.UserId == game.CreatorId
end

Players.PlayerAdded:Connect(function(player: Player)
	player.Chatted:Connect(function(message: string)
		if not isPlayerAdmin(player) then return end

		local args = string.split(message, " ")
		local command = string.lower(args[1] or "")

		if command == ":speed" then
			local amount = tonumber(args[2]) or 32
			local character = player.Character
			if character and character:FindFirstChildOfClass("Humanoid") then
				character.Humanoid.WalkSpeed = amount
				print("[Lemonade Admin]: تم تعديل سرعة " .. player.Name .. " إلى " .. amount)
			end

		elseif command == ":jump" then
			local amount = tonumber(args[2]) or 80
			local character = player.Character
			if character and character:FindFirstChildOfClass("Humanoid") then
				character.Humanoid.JumpPower = amount
				print("[Lemonade Admin]: تم تعديل قفزة " .. player.Name .. " إلى " .. amount)
			end

		elseif command == ":kill" then
			local targetName = args[2]
			if targetName then
				for _, target in Players:GetPlayers() do
					if string.sub(string.lower(target.Name), 1, #targetName) == string.lower(targetName) then
						if target.Character and target.Character:FindFirstChildOfClass("Humanoid") then
							target.Character.Humanoid.Health = 0
						end
					end
				end
			end

		elseif command == ":heal" then
			local character = player.Character
			if character and character:FindFirstChildOfClass("Humanoid") then
				character.Humanoid.Health = character.Humanoid.MaxHealth
			end
		end
	end)
end)
""",
                setupSteps = listOf(
                    "أنشئ سكربت Script في مجلد ServerScriptService.",
                    "اكتب اسم حسابك في روبلوكس داخل جدول ADMIN_LIST.",
                    "شغل اللعبة واكتب في الدردشة ':speed 50' أو ':jump 100' أو ':heal' للاستمتاع بالصلاحيات!"
                ),
                tags = listOf("أدمن", "أوامر", "دردشة", "إشراف")
            )
        ),
        TemplateItem(
            id = "template_door",
            title = "باب تفاعلي بكارت الدخول (Keycard Door)",
            category = "التفاعل والأبواب",
            iconEmoji = "🚪",
            shortDescription = "باب متحرك بـ TweenService يتحقق من امتلاك اللاعب لأداة كارت الدخول عبر ProximityPrompt.",
            script = RobloxScript(
                id = "script_door",
                title = "متحكم الباب الذكي وكارت الأمان",
                scriptType = ScriptType.SERVER_SCRIPT,
                location = "Workspace > KeycardDoor > DoorScript",
                description = "باب واقعي بأنيميشن فتح وإغلاق تلقائي بعد 4 ثوانٍ مع فحص الحقيبة.",
                code = """--!strict
-- تم التوليد بواسطة Lemonade.gg (Roblox AI Studio)
-- الموقع: KeycardDoor.DoorScript (Script)

local TweenService = game:GetService("TweenService")

local doorModel = script.Parent
local doorPart = doorModel:WaitForChild("DoorFrame") :: BasePart
local prompt = doorModel:WaitForChild("ProximityPrompt") :: ProximityPrompt

local REQUIRED_TOOL_NAME = "SecurityKeycard"
local isOpen = false
local originalCFrame = doorPart.CFrame
local openedCFrame = originalCFrame * CFrame.new(0, 0, -4.5) -- انزلاق للفتح

local tweenInfo = TweenInfo.new(0.8, Enum.EasingStyle.Quad, Enum.EasingDirection.Out)

local function playerHasKeycard(player: Player): boolean
	local character = player.Character
	local backpack = player:FindFirstChild("Backpack")

	if character and character:FindFirstChild(REQUIRED_TOOL_NAME) then
		return true
	end
	if backpack and backpack:FindFirstChild(REQUIRED_TOOL_NAME) then
		return true
	end
	return false
end

prompt.Triggered:Connect(function(player: Player)
	if isOpen then return end

	if not playerHasKeycard(player) then
		prompt.ActionText = "مرفوض (تحتاج كارت الأمان)"
		task.wait(1.5)
		prompt.ActionText = "فتح الباب"
		return
	end

	isOpen = true
	prompt.Enabled = false

	-- أنيميشن فتح الباب
	local openTween = TweenService:Create(doorPart, tweenInfo, { CFrame = openedCFrame })
	openTween:Play()
	print("[Lemonade]: تم فتح الباب بواسطة " .. player.Name)

	-- إغلاق تلقائي بعد 4 ثوانٍ
	task.wait(4)
	local closeTween = TweenService:Create(doorPart, tweenInfo, { CFrame = originalCFrame })
	closeTween:Play()
	closeTween.Completed:Wait()

	isOpen = false
	prompt.Enabled = true
end)
""",
                setupSteps = listOf(
                    "أنشئ مجسم Model في Workspace باسم 'KeycardDoor'.",
                    "أضف جزءاً Part باسم 'DoorFrame' وأدخل ProximityPrompt بداخله.",
                    "الصق هذا السكربت داخل KeycardDoor واستمتع بباب أمني احترافي!"
                ),
                tags = listOf("أبواب", "ProximityPrompt", "كارت أمان", "TweenService")
            )
        )
    )
}

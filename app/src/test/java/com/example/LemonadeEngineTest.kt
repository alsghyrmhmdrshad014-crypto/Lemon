package com.example

import com.example.data.PresetTemplates
import com.example.model.ScriptType
import com.example.network.LemonadeEngine
import org.junit.Assert.*
import org.junit.Test

class LemonadeEngineTest {

    @Test
    fun testPresetTemplatesNotEmpty() {
        val templates = PresetTemplates.allTemplates
        assertTrue("Preset templates should not be empty", templates.isNotEmpty())
        assertTrue("Should contain leaderstats template", templates.any { it.title.contains("Leaderstats") })
        assertTrue("Should contain combat template", templates.any { it.title.contains("Combat") })
    }

    @Test
    fun testSmartFallbackGeneration() {
        val petScript = LemonadeEngine.generateSmartFallback(
            prompt = "Make a pet egg hatching simulator",
            targetType = ScriptType.SERVER_SCRIPT,
            selectedCategory = "Pets",
            errorNotice = null
        )
        assertNotNull(petScript)
        assertTrue(petScript.code.contains("--!strict"))
        assertTrue(petScript.code.contains("HatchEgg"))

        val arabicCombatScript = LemonadeEngine.generateSmartFallback(
            prompt = "اصنع نظام قتال بالسيوف وضرر",
            targetType = ScriptType.SERVER_SCRIPT,
            selectedCategory = "قتال",
            errorNotice = null
        )
        assertNotNull(arabicCombatScript)
        assertTrue(arabicCombatScript.code.contains("Raycast"))
    }
}

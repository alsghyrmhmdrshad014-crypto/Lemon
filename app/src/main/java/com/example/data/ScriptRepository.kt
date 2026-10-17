package com.example.data

import android.content.Context
import android.content.SharedPreferences
import com.example.model.RobloxScript
import com.example.model.ScriptType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject

class ScriptRepository(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("lemonade_scripts_db", Context.MODE_PRIVATE)
    private val _savedScripts = MutableStateFlow<List<RobloxScript>>(emptyList())
    val savedScripts: StateFlow<List<RobloxScript>> = _savedScripts.asStateFlow()

    init {
        loadSavedScripts()
    }

    private fun loadSavedScripts() {
        val rawJson = prefs.getString("saved_scripts_key", null)
        if (rawJson != null) {
            try {
                val array = JSONArray(rawJson)
                val list = mutableListOf<RobloxScript>()
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    list.add(
                        RobloxScript(
                            id = obj.getString("id"),
                            title = obj.getString("title"),
                            scriptType = ScriptType.valueOf(obj.optString("scriptType", ScriptType.SERVER_SCRIPT.name)),
                            location = obj.optString("location", "ServerScriptService"),
                            description = obj.optString("description", ""),
                            code = obj.getString("code"),
                            setupSteps = parseJsonStringList(obj.optJSONArray("setupSteps")),
                            tags = parseJsonStringList(obj.optJSONArray("tags")),
                            createdAt = obj.optLong("createdAt", System.currentTimeMillis())
                        )
                    )
                }
                _savedScripts.value = list
                return
            } catch (_: Exception) {
                // If parse fails, initialize with presets
            }
        }

        // Initialize with default template favorites
        val defaults = PresetTemplates.allTemplates.take(3).map { it.script }
        _savedScripts.value = defaults
        persistScripts(defaults)
    }

    private fun parseJsonStringList(array: JSONArray?): List<String> {
        if (array == null) return emptyList()
        val list = mutableListOf<String>()
        for (i in 0 until array.length()) {
            list.add(array.getString(i))
        }
        return list
    }

    private fun persistScripts(list: List<RobloxScript>) {
        val array = JSONArray()
        for (s in list) {
            val obj = JSONObject().apply {
                put("id", s.id)
                put("title", s.title)
                put("scriptType", s.scriptType.name)
                put("location", s.location)
                put("description", s.description)
                put("code", s.code)
                put("createdAt", s.createdAt)
                put("setupSteps", JSONArray(s.setupSteps))
                put("tags", JSONArray(s.tags))
            }
            array.put(obj)
        }
        prefs.edit().putString("saved_scripts_key", array.toString()).apply()
    }

    fun saveScript(script: RobloxScript) {
        val current = _savedScripts.value.toMutableList()
        val existingIndex = current.indexOfFirst { it.id == script.id }
        if (existingIndex >= 0) {
            current[existingIndex] = script
        } else {
            current.add(0, script)
        }
        _savedScripts.value = current
        persistScripts(current)
    }

    fun deleteScript(id: String) {
        val current = _savedScripts.value.filter { it.id != id }
        _savedScripts.value = current
        persistScripts(current)
    }
}

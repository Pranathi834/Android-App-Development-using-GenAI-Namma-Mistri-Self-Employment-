package com.example.nammamistri.screens

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

data class CalculationResult(
    val type: String,
    val result: String,
    val timestamp: Long = System.currentTimeMillis()
)

object DataStore {
    private const val PREF_NAME = "namma_mistri_data"
    private const val KEY_WORKERS = "workers"
    private const val KEY_RATES = "rates"
    private const val KEY_DAYS = "days_map"
    private const val KEY_CALC_RESULTS = "calc_results"
    private const val KEY_PHOTOS = "photos_map"
    private val gson = Gson()

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    // ---- WORKERS ----
    fun saveWorkers(context: Context, workers: List<Worker>) {
        val json = gson.toJson(workers)
        getPrefs(context).edit().putString(KEY_WORKERS, json).apply()
    }

    fun loadWorkers(context: Context): List<Worker> {
        val json = getPrefs(context).getString(KEY_WORKERS, null) ?: return emptyList()
        return try {
            val type = object : TypeToken<List<Worker>>() {}.type
            gson.fromJson(json, type)
        } catch (e: Exception) {
            emptyList()
        }
    }

    // ---- RATES ----
    fun saveRates(context: Context, rates: List<MaterialRate>) {
        val json = gson.toJson(rates)
        getPrefs(context).edit().putString(KEY_RATES, json).apply()
    }

    fun loadRates(context: Context): List<MaterialRate>? {
        val json = getPrefs(context).getString(KEY_RATES, null) ?: return null
        return try {
            val type = object : TypeToken<List<MaterialRate>>() {}.type
            gson.fromJson(json, type)
        } catch (e: Exception) {
            null
        }
    }

    // ---- DAYS MAP ----
    fun saveDaysMap(context: Context, daysMap: Map<String, Int>) {
        val json = gson.toJson(daysMap)
        getPrefs(context).edit().putString(KEY_DAYS, json).apply()
    }

    fun loadDaysMap(context: Context): Map<String, Int> {
        val json = getPrefs(context).getString(KEY_DAYS, null) ?: return emptyMap()
        return try {
            val type = object : TypeToken<Map<String, Int>>() {}.type
            gson.fromJson(json, type)
        } catch (e: Exception) {
            emptyMap()
        }
    }

    // ---- CALCULATOR RESULTS ----
    fun saveCalculationResult(context: Context, result: CalculationResult) {
        val allResults = loadAllCalculationResults(context).toMutableList()
        allResults.add(0, result)
        val json = gson.toJson(allResults)
        getPrefs(context).edit().putString(KEY_CALC_RESULTS, json).apply()
    }

    fun loadAllCalculationResults(context: Context): List<CalculationResult> {
        val json = getPrefs(context).getString(KEY_CALC_RESULTS, null) ?: return emptyList()
        return try {
            val type = object : TypeToken<List<CalculationResult>>() {}.type
            gson.fromJson(json, type)
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun loadLatestCalculation(context: Context, type: String): CalculationResult? {
        return loadAllCalculationResults(context).find { it.type == type }
    }

    fun deleteCalculation(context: Context, timestamp: Long) {
        val allResults = loadAllCalculationResults(context)
            .filter { it.timestamp != timestamp }
        val json = gson.toJson(allResults)
        getPrefs(context).edit().putString(KEY_CALC_RESULTS, json).apply()
    }

    // ---- PHOTOS ----
    fun savePhotos(context: Context, photosMap: Map<String, List<String>>) {
        val json = gson.toJson(photosMap)
        getPrefs(context).edit().putString(KEY_PHOTOS, json).apply()
    }

    fun loadPhotos(context: Context): Map<String, List<String>> {
        val json = getPrefs(context).getString(KEY_PHOTOS, null) ?: return emptyMap()
        return try {
            val type = object : TypeToken<Map<String, List<String>>>() {}.type
            gson.fromJson(json, type)
        } catch (e: Exception) {
            emptyMap()
        }
    }
}
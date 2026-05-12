package com.example.nammamistri.database

import android.content.Context

class AppRepository(context: Context) {

    private val db = DatabaseHelper(context)

    // ==================== WORKERS ====================

    fun addWorker(
        name: String,
        phone: String = "",
        skill: String = "",
        dailyRate: Double = 0.0
    ): Long {
        return db.insertWorker(
            LaborData(
                name = name,
                phone = phone,
                skill = skill,
                dailyRate = dailyRate,
                dateAdded = System.currentTimeMillis()
            )
        )
    }

    fun getWorkers(): List<LaborData> = db.getAllWorkers()

    fun updateDays(id: Int, days: Int, dailyRate: Double) {
        db.updateDaysWorked(id, days, dailyRate)
    }

    fun removeWorker(id: Int) = db.deleteWorker(id)

    fun totalWages(): Double = db.getTotalWages()

    // ==================== CALCULATIONS ====================

    fun saveCalculation(type: String, result: String): Long {
        return db.insertCalculation(
            CalculationData(
                type = type,
                result = result,
                timestamp = System.currentTimeMillis()
            )
        )
    }

    fun getCalculations(type: String): List<CalculationData> {
        return db.getCalculationsByType(type)
    }

    fun getAllCalculations(): List<CalculationData> {
        val list = mutableListOf<CalculationData>()
        listOf("BRICK", "SLAB", "PLASTER", "TILES").forEach { type ->
            list.addAll(db.getCalculationsByType(type))
        }
        return list.sortedByDescending { it.timestamp }
    }

    fun removeCalculation(id: Int) = db.deleteCalculation(id)

    // ==================== RATES ====================

    fun saveRate(name: String, unit: String, price: Double) {
        db.insertOrUpdateRate(RateData(name = name, unit = unit, price = price))
    }

    fun getRates(): List<RateData> = db.getAllRates()

    // ==================== PHOTOS ====================

    fun savePhoto(category: String, uri: String): Long {
        return db.insertPhoto(
            PhotoData(
                category = category,
                uri = uri,
                timestamp = System.currentTimeMillis()
            )
        )
    }

    fun getPhotos(): Map<String, List<String>> = db.getAllPhotos()

    fun removePhoto(uri: String) = db.deletePhoto(uri)
}
package com.example.nammamistri.database

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

data class LaborData(
    val id: Int = 0,
    val name: String,
    val phone: String = "",
    val skill: String = "",
    val dailyRate: Double = 0.0,
    val daysWorked: Int = 0,
    val totalEarnings: Double = 0.0,
    val notes: String = "",
    val isActive: Boolean = true,
    val dateAdded: Long = System.currentTimeMillis()
)

data class CalculationData(
    val id: Int = 0,
    val type: String,
    val result: String,
    val timestamp: Long = System.currentTimeMillis()
)

data class RateData(
    val id: Int = 0,
    val name: String,
    val unit: String,
    val price: Double
)

data class PhotoData(
    val id: Int = 0,
    val category: String,
    val uri: String,
    val timestamp: Long = System.currentTimeMillis()
)

class DatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, "nammamistri.db", null, 1) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS workers (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT NOT NULL,
                phone TEXT DEFAULT '',
                skill TEXT DEFAULT '',
                dailyRate REAL DEFAULT 0.0,
                daysWorked INTEGER DEFAULT 0,
                totalEarnings REAL DEFAULT 0.0,
                notes TEXT DEFAULT '',
                isActive INTEGER DEFAULT 1,
                dateAdded INTEGER DEFAULT 0
            )
        """.trimIndent())

        db.execSQL("""
            CREATE TABLE IF NOT EXISTS calculations (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                type TEXT NOT NULL,
                result TEXT NOT NULL,
                timestamp INTEGER DEFAULT 0
            )
        """.trimIndent())

        db.execSQL("""
            CREATE TABLE IF NOT EXISTS rates (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT NOT NULL,
                unit TEXT DEFAULT '',
                price REAL DEFAULT 0.0
            )
        """.trimIndent())

        db.execSQL("""
            CREATE TABLE IF NOT EXISTS photos (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                category TEXT NOT NULL,
                uri TEXT NOT NULL,
                timestamp INTEGER DEFAULT 0
            )
        """.trimIndent())
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS workers")
        db.execSQL("DROP TABLE IF EXISTS calculations")
        db.execSQL("DROP TABLE IF EXISTS rates")
        db.execSQL("DROP TABLE IF EXISTS photos")
        onCreate(db)
    }

    // ==================== WORKERS ====================
    fun insertWorker(labor: LaborData): Long {
        val values = ContentValues().apply {
            put("name", labor.name)
            put("phone", labor.phone)
            put("skill", labor.skill)
            put("dailyRate", labor.dailyRate)
            put("daysWorked", labor.daysWorked)
            put("totalEarnings", labor.totalEarnings)
            put("notes", labor.notes)
            put("isActive", if (labor.isActive) 1 else 0)
            put("dateAdded", labor.dateAdded)
        }
        return writableDatabase.insert("workers", null, values)
    }

    fun getAllWorkers(): List<LaborData> {
        val list = mutableListOf<LaborData>()
        val cursor = readableDatabase.rawQuery(
            "SELECT * FROM workers ORDER BY dateAdded DESC", null
        )
        if (cursor.moveToFirst()) {
            do {
                list.add(LaborData(
                    id = cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                    name = cursor.getString(cursor.getColumnIndexOrThrow("name")),
                    phone = cursor.getString(cursor.getColumnIndexOrThrow("phone")),
                    skill = cursor.getString(cursor.getColumnIndexOrThrow("skill")),
                    dailyRate = cursor.getDouble(cursor.getColumnIndexOrThrow("dailyRate")),
                    daysWorked = cursor.getInt(cursor.getColumnIndexOrThrow("daysWorked")),
                    totalEarnings = cursor.getDouble(cursor.getColumnIndexOrThrow("totalEarnings")),
                    notes = cursor.getString(cursor.getColumnIndexOrThrow("notes")),
                    isActive = cursor.getInt(cursor.getColumnIndexOrThrow("isActive")) == 1,
                    dateAdded = cursor.getLong(cursor.getColumnIndexOrThrow("dateAdded"))
                ))
            } while (cursor.moveToNext())
        }
        cursor.close()
        return list
    }

    fun updateDaysWorked(id: Int, days: Int, dailyRate: Double) {
        val values = ContentValues().apply {
            put("daysWorked", days)
            put("totalEarnings", dailyRate * days)
        }
        writableDatabase.update("workers", values, "id = ?", arrayOf(id.toString()))
    }

    fun deleteWorker(id: Int) {
        writableDatabase.delete("workers", "id = ?", arrayOf(id.toString()))
    }

    fun getTotalWages(): Double {
        val cursor = readableDatabase.rawQuery(
            "SELECT SUM(totalEarnings) FROM workers WHERE isActive = 1", null
        )
        var total = 0.0
        if (cursor.moveToFirst()) total = cursor.getDouble(0)
        cursor.close()
        return total
    }

    // ==================== CALCULATIONS ====================
    fun insertCalculation(calc: CalculationData): Long {
        val values = ContentValues().apply {
            put("type", calc.type)
            put("result", calc.result)
            put("timestamp", calc.timestamp)
        }
        return writableDatabase.insert("calculations", null, values)
    }

    fun getCalculationsByType(type: String): List<CalculationData> {
        val list = mutableListOf<CalculationData>()
        val cursor = readableDatabase.rawQuery(
            "SELECT * FROM calculations WHERE type = ? ORDER BY timestamp DESC",
            arrayOf(type)
        )
        if (cursor.moveToFirst()) {
            do {
                list.add(CalculationData(
                    id = cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                    type = cursor.getString(cursor.getColumnIndexOrThrow("type")),
                    result = cursor.getString(cursor.getColumnIndexOrThrow("result")),
                    timestamp = cursor.getLong(cursor.getColumnIndexOrThrow("timestamp"))
                ))
            } while (cursor.moveToNext())
        }
        cursor.close()
        return list
    }

    fun deleteCalculation(id: Int) {
        writableDatabase.delete("calculations", "id = ?", arrayOf(id.toString()))
    }

    // ==================== RATES ====================
    fun insertOrUpdateRate(rate: RateData) {
        val values = ContentValues().apply {
            put("name", rate.name)
            put("unit", rate.unit)
            put("price", rate.price)
        }
        val cursor = readableDatabase.rawQuery(
            "SELECT id FROM rates WHERE name = ?", arrayOf(rate.name)
        )
        if (cursor.moveToFirst()) {
            val id = cursor.getInt(0)
            writableDatabase.update("rates", values, "id = ?", arrayOf(id.toString()))
        } else {
            writableDatabase.insert("rates", null, values)
        }
        cursor.close()
    }

    fun getAllRates(): List<RateData> {
        val list = mutableListOf<RateData>()
        val cursor = readableDatabase.rawQuery("SELECT * FROM rates", null)
        if (cursor.moveToFirst()) {
            do {
                list.add(RateData(
                    id = cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                    name = cursor.getString(cursor.getColumnIndexOrThrow("name")),
                    unit = cursor.getString(cursor.getColumnIndexOrThrow("unit")),
                    price = cursor.getDouble(cursor.getColumnIndexOrThrow("price"))
                ))
            } while (cursor.moveToNext())
        }
        cursor.close()
        return list
    }

    // ==================== PHOTOS ====================
    fun insertPhoto(photo: PhotoData): Long {
        val values = ContentValues().apply {
            put("category", photo.category)
            put("uri", photo.uri)
            put("timestamp", photo.timestamp)
        }
        return writableDatabase.insert("photos", null, values)
    }

    fun getAllPhotos(): Map<String, List<String>> {
        val map = mutableMapOf<String, MutableList<String>>()
        val cursor = readableDatabase.rawQuery(
            "SELECT * FROM photos ORDER BY timestamp DESC", null
        )
        if (cursor.moveToFirst()) {
            do {
                val category = cursor.getString(cursor.getColumnIndexOrThrow("category"))
                val uri = cursor.getString(cursor.getColumnIndexOrThrow("uri"))
                map.getOrPut(category) { mutableListOf() }.add(uri)
            } while (cursor.moveToNext())
        }
        cursor.close()
        return map
    }

    fun deletePhoto(uri: String) {
        writableDatabase.delete("photos", "uri = ?", arrayOf(uri))
    }
}
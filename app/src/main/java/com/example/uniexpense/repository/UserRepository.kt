package com.example.uniexpense.repository

import android.content.ContentValues
import android.content.Context
import com.example.uniexpense.database.DatabaseHelper
import com.example.uniexpense.model.User

class UserRepository(context: Context) {
    private val dbHelper = DatabaseHelper.getInstance(context)

    fun emailExists(email: String): Boolean {
        val db = dbHelper.readableDatabase
        db.rawQuery("SELECT id FROM users WHERE email = ? LIMIT 1", arrayOf(email)).use { c ->
            return c.moveToFirst()
        }
    }

    fun insertUser(name: String, email: String, passwordHash: String, language: String): Long {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put("name", name)
            put("email", email)
            put("password_hash", passwordHash)
            put("language", language)
        }
        return db.insert("users", null, values)
    }

    fun findByEmail(email: String): User? {
        val db = dbHelper.readableDatabase
        db.rawQuery(
            "SELECT id, name, email, password_hash, language FROM users WHERE email = ? LIMIT 1",
            arrayOf(email)
        ).use { c ->
            if (!c.moveToFirst()) return null
            return User(
                id = c.getLong(0),
                name = c.getString(1),
                email = c.getString(2),
                passwordHash = c.getString(3),
                language = c.getString(4)
            )
        }
    }

    fun findById(userId: Long): User? {
        val db = dbHelper.readableDatabase
        db.rawQuery(
            "SELECT id, name, email, password_hash, language FROM users WHERE id = ? LIMIT 1",
            arrayOf(userId.toString())
        ).use { c ->
            if (!c.moveToFirst()) return null
            return User(
                id = c.getLong(0),
                name = c.getString(1),
                email = c.getString(2),
                passwordHash = c.getString(3),
                language = c.getString(4)
            )
        }
    }

    fun updateProfile(userId: Long, name: String, email: String): Boolean {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put("name", name)
            put("email", email)
        }
        return db.update("users", values, "id = ?", arrayOf(userId.toString())) > 0
    }

    fun updateLanguage(userId: Long, languageCode: String): Boolean {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply { put("language", languageCode) }
        return db.update("users", values, "id = ?", arrayOf(userId.toString())) > 0
    }

    fun updatePasswordHash(userId: Long, newHash: String): Boolean {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply { put("password_hash", newHash) }
        return db.update("users", values, "id = ?", arrayOf(userId.toString())) > 0
    }
}

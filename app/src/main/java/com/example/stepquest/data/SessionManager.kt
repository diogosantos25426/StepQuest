package com.example.stepquest.data

import android.content.Context
import android.content.SharedPreferences

class SessionManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("StepQuestPrefs", Context.MODE_PRIVATE)

    companion object {
        const val USER_ID = "user_id"
        const val USERNAME = "username"
    }

    fun saveUserSession(userId: Int, username: String) {
        prefs.edit().apply {
            putInt(USER_ID, userId)
            putString(USERNAME, username)
            apply()
        }
    }

    fun getUserId(): Int {
        return prefs.getInt(USER_ID, -1)
    }

    fun getUsername(): String? {
        return prefs.getString(USERNAME, null)
    }

    fun isLoggedIn(): Boolean {
        return getUserId() != -1
    }

    fun logout() {
        prefs.edit().clear().apply()
    }
}
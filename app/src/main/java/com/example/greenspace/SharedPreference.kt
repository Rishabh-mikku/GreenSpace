package com.example.greenspace

import android.content.Context
import android.content.SharedPreferences

object SharedPreference {
    private const val PREF_NAME = "user_pref"
    private const val KEY_EMAIL = "email"
    private const val KEY_USERNAME = "username"
    private const val KEY_PROFILE_PIC = "profile_pic"

    private fun getSharedPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    fun setEmail(context: Context, email: String) {
        val editor = getSharedPreferences(context).edit()
        editor.putString(KEY_EMAIL, email)
        editor.apply()
    }

    fun getEmail(context: Context): String? {
        return getSharedPreferences(context).getString(KEY_EMAIL, null)
    }

    fun setUsername(context: Context, username: String) {
        val editor = getSharedPreferences(context).edit()
        editor.putString(KEY_USERNAME, username)
        editor.apply()
    }

    fun getUsername(context: Context): String? {
        return getSharedPreferences(context).getString(KEY_USERNAME, null)
    }

    fun setProfilePic(context: Context, profilePic: String) {
        val editor = getSharedPreferences(context).edit()
        editor.putString(KEY_PROFILE_PIC, profilePic)
        editor.apply()
    }

    fun getProfilePic(context: Context): String? {
        return getSharedPreferences(context).getString(KEY_PROFILE_PIC, null)
    }
}
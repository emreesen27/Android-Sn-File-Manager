package com.sn.snfilemanager.di

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.sn.snfilemanager.BuildConfig
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

enum class PrefsTag(val tag: String) {
    FIRST_RUN("FIRST_RUN"),
    FILTER_IMAGES("FILTER_IMAGES")
}

@Singleton
class MySharedPreferences @Inject constructor(@ApplicationContext context: Context) {

    private val gson: Gson = Gson()

    private val prefs =
        context.getSharedPreferences(BuildConfig.APPLICATION_ID, Context.MODE_PRIVATE)

    fun putBoolean(prefsTag: PrefsTag, data: Boolean) =
        prefs.edit().putBoolean(prefsTag.tag, data).apply()

    fun getBoolean(prefsTag: PrefsTag): Boolean = prefs.getBoolean(prefsTag.tag, false)

    fun putString(prefsTag: PrefsTag, data: String) =
        prefs.edit().putString(prefsTag.tag, data).apply()

    fun getString(prefsTag: PrefsTag): String? = prefs.getString(prefsTag.tag, "")

    fun putStringArray(prefsTag: PrefsTag, data: MutableSet<String>) =
        prefs.edit().putStringSet(prefsTag.tag, data).apply()

    fun getStringArray(prefsTag: PrefsTag): MutableSet<String>? =
        prefs.getStringSet(prefsTag.tag, null)

    fun putSerializedData(prefsTag: PrefsTag, data: Map<String, String>) =
        prefs.edit().putString(prefsTag.tag, gson.toJson(data)).apply()

    fun getSerializedData(prefsTag: PrefsTag): Map<String, String> {
        val value = prefs.getString(prefsTag.tag, null)
        val type = object : TypeToken<HashMap<String, String>>() {}.type
        return if (value != null) {
            gson.fromJson<HashMap<String, String>>(value, type)
        } else {
            HashMap()
        }
    }

}
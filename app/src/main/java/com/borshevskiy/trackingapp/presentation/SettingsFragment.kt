package com.borshevskiy.trackingapp.presentation

import android.graphics.Color
import android.os.Bundle
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.borshevskiy.trackingapp.R

class SettingsFragment : PreferenceFragmentCompat() {

    private lateinit var timePrefs: Preference
    private lateinit var colorPrefs: Preference

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.main_preference, rootKey)
        initTimePrefs()
        initColorPrefs()
    }

    private fun initTimePrefs() {
        timePrefs = findPreference("update_time_key")!!
        val names = resources.getStringArray(R.array.time_update_names)
        val values = resources.getStringArray(R.array.time_update_values)
        val cache = timePrefs.preferenceManager.sharedPreferences
        timePrefs.onPreferenceChangeListener = Preference.OnPreferenceChangeListener {
                preference, newValue ->
            preference.title = "Update time: ${names[values.indexOf(newValue)]}"
            true }
        if(cache?.contains("update_time_key") == true) {
            "Update time: ${names[values.indexOf(cache.getString("update_time_key", "3000"))]}".also { timePrefs.title = it }
        }
    }

    private fun initColorPrefs() {
        colorPrefs = findPreference("color_key")!!
        val names = resources.getStringArray(R.array.color_names)
        val values = resources.getStringArray(R.array.color_values)
        val cache = colorPrefs.preferenceManager.sharedPreferences
        colorPrefs.onPreferenceChangeListener = Preference.OnPreferenceChangeListener {
                preference, newValue ->
            preference.title = "Track color: ${names[values.indexOf(newValue)]}"
            preference.icon?.setTint(Color.parseColor(names[values.indexOf(newValue)]))
            true }
        if(cache?.contains("color_key") == true) {
            "Track color: ${names[values.indexOf(cache.getString("color_key", "#FF009EDA"))]}".also { colorPrefs.title = it }
            colorPrefs.icon?.setTint(Color.parseColor(names[values.indexOf(cache.getString("color_key", "#FF009EDA"))]))
        }
    }
}
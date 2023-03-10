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
        timePrefs.onPreferenceChangeListener = Preference.OnPreferenceChangeListener {
                preference, newValue ->
            preference.title = "Update time: ${names[values.indexOf(newValue)]}"
            true }
        with(timePrefs.preferenceManager.sharedPreferences) {
            if(this?.contains("update_time_key") == true) {
                "Update time: ${names[values.indexOf(getString("update_time_key", "3000"))]}".also { timePrefs.title = it }
            }
        }
    }

    private fun initColorPrefs() {
        colorPrefs = findPreference("color_key")!!
        val names = resources.getStringArray(R.array.color_names)
        val values = resources.getStringArray(R.array.color_values)
        colorPrefs.onPreferenceChangeListener = Preference.OnPreferenceChangeListener {
                preference, newValue ->
            preference.title = "Track color: ${names[values.indexOf(newValue)]}"
            preference.icon?.setTint(Color.parseColor(names[values.indexOf(newValue)]))
            true }
        with(colorPrefs.preferenceManager.sharedPreferences) {
            if(this?.contains("color_key") == true) {
                "Track color: ${names[values.indexOf(getString("color_key", "#FF009EDA"))]}".also { colorPrefs.title = it }
                colorPrefs.icon?.setTint(android.graphics.Color.parseColor(names[values.indexOf(getString("color_key", "#FF009EDA"))]))
            }
        }
    }
}
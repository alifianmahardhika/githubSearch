package com.example.githubconsumerapp

import android.content.SharedPreferences
import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreference

class SettingPreferenceFragment: PreferenceFragmentCompat(),
    SharedPreferences.OnSharedPreferenceChangeListener {
    private lateinit var switchName : String
    private lateinit var switchPreference: SwitchPreference
    private lateinit var alarmReceiver: AlarmReceiver

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.preferences)
        init()
        setSummary()
    }

    private fun init() {
        switchName = resources.getString(R.string.key_reminder)
        switchPreference = findPreference<SwitchPreference>(switchName) as SwitchPreference
        alarmReceiver = AlarmReceiver()
    }

    private fun setSummary() {
        val sharedPref = preferenceManager.sharedPreferences
        switchPreference.isChecked = sharedPref.getBoolean(switchName, false)
    }

    override fun onResume() {
        super.onResume()
        preferenceScreen.sharedPreferences.registerOnSharedPreferenceChangeListener(this)
    }
    override fun onPause() {
        super.onPause()
        preferenceScreen.sharedPreferences.unregisterOnSharedPreferenceChangeListener(this)
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {
        if (key == switchName) {
            val switchStatus = sharedPreferences.getBoolean(switchName, false)
            if (switchStatus){
                val repeatTime = "09:00"
                val repeatMessage = "Let's find user on Github"
                val mtoast = resources.getString(R.string.reminder_on)
                context?.let {
                    alarmReceiver.setRepeatingAlarm(
                        it, AlarmReceiver.TYPE_REPEATING,
                        repeatTime, repeatMessage, mtoast)
                }
                } else {
                val mtoast = resources.getString(R.string.reminder_off)
                context?.let { alarmReceiver.cancelAlarm(it, mtoast) }
            }
        }
    }
}
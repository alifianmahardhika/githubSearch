package com.example.githubconsumerapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class SettingPreferencesActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setting_preferences)

        supportFragmentManager.beginTransaction().add(R.id.setting_holder, SettingPreferenceFragment()).commit()
    }
}
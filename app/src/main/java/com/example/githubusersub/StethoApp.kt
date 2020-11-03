package com.example.githubusersub

import android.app.Application
import com.facebook.stetho.Stetho

class StethoApp : Application() {
    override fun onCreate() {
        super.onCreate()
        Stetho.initializeWithDefaults(this)
    }
}
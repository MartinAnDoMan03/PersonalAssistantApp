package com.example.yourassistantyora

import android.app.Application
import com.cloudinary.android.MediaManager

class CloudinaryApp : Application() {
    override fun onCreate() {
        super.onCreate()

        val config = com.cloudinary.Configuration(
            mapOf(
                "cloud_name" to "cloud_name",
                "api_key" to "api_key",
                "api_secret" to "api_secret"
            )
        )

        MediaManager.init(this, config)
    }
}

package com.example.yourassistantyora

import android.app.Application
import com.cloudinary.android.MediaManager

class CloudinaryApp : Application() {
    override fun onCreate() {
        super.onCreate()

        val config = com.cloudinary.Configuration(
            mapOf(
                "cloud_name" to "deiquzgmn",
                "api_key" to "585322474414964",
                "api_secret" to "AEhpSnXJXwDKIyJHgW_6mGlVdJI"
            )
        )

        MediaManager.init(this, config)
    }
}

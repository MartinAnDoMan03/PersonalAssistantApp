package com.example.yourassistantyora

import android.app.Application
import com.cloudinary.android.MediaManager

class Cloudinary : Application() {
    override fun onCreate() {
        super.onCreate()
        val config = mutableMapOf<String, String>()
        config["cloud_name"] = "deiquzgmn" // GANTI DENGAN CLOUD NAME ANDA
        config["api_key"] = "585322474414964" // GANTI DENGAN API KEY ANDA
        config["api_secret"] = "AEhpSnXJXwDKIyJHgW_6mGlVdJI" // GANTI DENGAN API SECRET ANDA
        MediaManager.init(this, config)
    }
}

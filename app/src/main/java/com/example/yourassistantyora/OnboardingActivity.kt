package com.example.yourassistantyora

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.cardview.widget.CardView

class OnboardingActivity : AppCompatActivity() {

    private lateinit var indicator1: View
    private lateinit var indicator2: View
    private lateinit var iconLogo: ImageView
    private lateinit var tvTitle: TextView
    private lateinit var tvDescription: TextView
    private lateinit var actionButtonsContainer: LinearLayout
    private lateinit var btnContinue: AppCompatButton
    private lateinit var btnBack: TextView
    private lateinit var btnSkip: TextView

    private var currentPage = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_onboarding)

        // Initialize views
        indicator1 = findViewById(R.id.indicator1)
        indicator2 = findViewById(R.id.indicator2)
        iconLogo = findViewById(R.id.iconLogo)
        tvTitle = findViewById(R.id.tvTitle)
        tvDescription = findViewById(R.id.tvDescription)
        actionButtonsContainer = findViewById(R.id.actionButtonsContainer)
        btnContinue = findViewById(R.id.btnContinue)
        btnBack = findViewById(R.id.btnBack)
        btnSkip = findViewById(R.id.btnSkip)

        // Button click listeners
        btnContinue.setOnClickListener {
            if (currentPage == 0) {
                showPage2()
            } else {
                finishOnboarding()
            }
        }

        btnBack.setOnClickListener {
            showPage1()
        }

        btnSkip.setOnClickListener {
            finishOnboarding()
        }

        // Show first page
        showPage1()
    }

    private fun showPage1() {
        currentPage = 0

        // Update indicators
        indicator1.setBackgroundColor(getColor(R.color.primary))
        indicator2.setBackgroundColor(getColor(R.color.gray_light))

        // Update content
        tvTitle.text = "Welcome to Yora! 👋"
        tvDescription.text = "Your all-in-one productivity companion. Let's get you setup and ready to achieve more!"
        actionButtonsContainer.visibility = View.GONE

        // Update buttons
        btnBack.visibility = View.GONE
        btnContinue.text = "Continue →"
    }

    private fun showPage2() {
        currentPage = 1

        // Update indicators
        indicator1.setBackgroundColor(getColor(R.color.gray_light))
        indicator2.setBackgroundColor(getColor(R.color.primary))

        // Update content
        tvTitle.text = "Organize Your Life"
        tvDescription.text = "Create tasks, take notes, and collaborate with your team - all in one place."
        actionButtonsContainer.visibility = View.VISIBLE

        // Update buttons
        btnBack.visibility = View.VISIBLE
        btnContinue.text = "Continue →"
    }

    private fun finishOnboarding() {
        // Tandai bahwa user sudah pernah melihat onboarding
        val prefs = getSharedPreferences("YoraPrefs", Context.MODE_PRIVATE)
        prefs.edit().putBoolean("has_seen_onboarding", true).apply()

        // Navigate to login
        val intent = Intent(this, MainActivity::class.java).apply {
            putExtra("START_DESTINATION", "login")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        finish()
    }
}
package com.example.yourassistantyora

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Intent
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat

class SplashActivity : AppCompatActivity() {

    private lateinit var logoImageView: ImageView
    private lateinit var appNameTextView: TextView
    private lateinit var taglineTextView: TextView
    private lateinit var rootLayout: ConstraintLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        logoImageView = findViewById(R.id.logoImageView)
        appNameTextView = findViewById(R.id.appNameTextView)
        taglineTextView = findViewById(R.id.taglineTextView)
        rootLayout = findViewById(R.id.rootLayout)

        // Mulai animasi splash
        startSplashAnimation()
    }

    private fun startSplashAnimation() {
        // Stage 1: Logo berputar dan membesar (0-800ms)
        Handler(Looper.getMainLooper()).postDelayed({
            animateStage1()
        }, 300)

        // Stage 2: Logo berbentuk rounded square dengan checkmark (800-1600ms)
        Handler(Looper.getMainLooper()).postDelayed({
            animateStage2()
        }, 1100)

        // Stage 3: Background berubah gradient dan teks muncul (1600-2400ms)
        Handler(Looper.getMainLooper()).postDelayed({
            animateStage3()
        }, 1900)

        // Pindah ke MainActivity setelah animasi selesai
        Handler(Looper.getMainLooper()).postDelayed({
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }, 3500)
    }

    private fun animateStage1() {
        // Rotasi logo
        val rotateAnimator = ObjectAnimator.ofFloat(logoImageView, "rotation", 0f, 360f)
        rotateAnimator.duration = 600
        rotateAnimator.interpolator = AccelerateDecelerateInterpolator()

        // Scale up logo
        val scaleXAnimator = ObjectAnimator.ofFloat(logoImageView, "scaleX", 1f, 1.3f)
        val scaleYAnimator = ObjectAnimator.ofFloat(logoImageView, "scaleY", 1f, 1.3f)
        scaleXAnimator.duration = 600
        scaleYAnimator.duration = 600

        val animatorSet = AnimatorSet()
        animatorSet.playTogether(rotateAnimator, scaleXAnimator, scaleYAnimator)
        animatorSet.start()
    }

    private fun animateStage2() {
        // Ubah gambar logo ke checkmark
        logoImageView.setImageResource(R.drawable.ic_checkmark)

        // Scale animation
        val scaleXAnimator = ObjectAnimator.ofFloat(logoImageView, "scaleX", 1.3f, 1.5f)
        val scaleYAnimator = ObjectAnimator.ofFloat(logoImageView, "scaleY", 1.3f, 1.5f)
        scaleXAnimator.duration = 400
        scaleYAnimator.duration = 400

        val animatorSet = AnimatorSet()
        animatorSet.playTogether(scaleXAnimator, scaleYAnimator)
        animatorSet.start()
    }

    private fun animateStage3() {
        // Ubah background ke gradient
        val gradientDrawable = GradientDrawable(
            GradientDrawable.Orientation.TL_BR,
            intArrayOf(
                ContextCompat.getColor(this, R.color.splash_gradient_start),
                ContextCompat.getColor(this, R.color.splash_gradient_end)
            )
        )
        rootLayout.background = gradientDrawable

        // Fade in background
        val backgroundAlpha = ValueAnimator.ofInt(0, 255)
        backgroundAlpha.duration = 600
        backgroundAlpha.addUpdateListener { animator ->
            gradientDrawable.alpha = animator.animatedValue as Int
        }
        backgroundAlpha.start()

        // Tampilkan teks dengan fade in
        appNameTextView.visibility = View.VISIBLE
        taglineTextView.visibility = View.VISIBLE

        val fadeInText = ObjectAnimator.ofFloat(appNameTextView, "alpha", 0f, 1f)
        val fadeInTagline = ObjectAnimator.ofFloat(taglineTextView, "alpha", 0f, 1f)
        fadeInText.duration = 600
        fadeInTagline.duration = 600

        val textAnimatorSet = AnimatorSet()
        textAnimatorSet.playTogether(fadeInText, fadeInTagline)
        textAnimatorSet.start()
    }
}

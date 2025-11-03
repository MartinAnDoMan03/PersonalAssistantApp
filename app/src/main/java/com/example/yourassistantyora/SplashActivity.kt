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
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.ImageView
import android.widget.TextView
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.OvershootInterpolator
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import android.util.Log

class SplashActivity : AppCompatActivity() {

    private lateinit var logoImageView: ImageView
    private lateinit var logoContainer: FrameLayout
    private lateinit var textContainer: LinearLayout
    private lateinit var appNameTextView: TextView
    private lateinit var taglineTextView: TextView
    private lateinit var rootLayout: ConstraintLayout

    private lateinit var particleView: ParticleView

    // Shadow views
    private lateinit var shadowView1: View
    private lateinit var shadowView2: View
    private lateinit var shadowView3: View
    private lateinit var shadowView4: View
    private lateinit var shadowView5: View
    private lateinit var shadowView6: View
    private lateinit var shadowView7: View
    private lateinit var shadowView8: View
    private lateinit var shadowView9: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        logoImageView = findViewById(R.id.logoImageView)
        logoContainer = findViewById(R.id.logoContainer)
        textContainer = findViewById(R.id.textContainer)
        appNameTextView = findViewById(R.id.appNameTextView)
        taglineTextView = findViewById(R.id.taglineTextView)
        rootLayout = findViewById(R.id.rootLayout)

        // Setup shadows
        shadowView1 = findViewById(R.id.shadowView1)
        shadowView2 = findViewById(R.id.shadowView2)
        shadowView3 = findViewById(R.id.shadowView3)
        shadowView4 = findViewById(R.id.shadowView4)
        shadowView5 = findViewById(R.id.shadowView5)
        shadowView6 = findViewById(R.id.shadowView6)
        shadowView7 = findViewById(R.id.shadowView7)
        shadowView8 = findViewById(R.id.shadowView8)
        shadowView9 = findViewById(R.id.shadowView9)

        particleView = findViewById(R.id.particleView)

        setupShadows()

        // Mulai animasi splash
        startSplashAnimation()
    }

    private fun setupShadows() {
        val density = resources.displayMetrics.density
        // Offsets untuk drop shadow ke arah bottom-right: outer layer (1) offset terbesar, inner (9) terkecil
        val xOffsetsDp = listOf(3f, 2.7f, 2.4f, 2.1f, 1.8f, 1.5f, 1.2f, 0.9f, 0.6f)
        val yOffsetsDp = listOf(4.5f, 4f, 3.5f, 3f, 2.5f, 2f, 1.5f, 1f, 0.5f)
        // Optional: alpha lebih transparan untuk outer layer
        val alphas = listOf(0.15f, 0.2f, 0.25f, 0.3f, 0.35f, 0.4f, 0.5f, 0.6f, 0.7f)

        val shadowViews = listOf(shadowView1, shadowView2, shadowView3, shadowView4, shadowView5,
            shadowView6, shadowView7, shadowView8, shadowView9)
        for ((index, view) in shadowViews.withIndex()) {
            view.translationX = xOffsetsDp[index] * density
            view.translationY = yOffsetsDp[index] * density
            view.alpha = alphas[index]
        }
        // Logo tidak perlu offset
    }

    private fun startSplashAnimation() {
        // Stage 1: Logo berputar dan membesar (0-800ms)
        Handler(Looper.getMainLooper()).postDelayed({
            animateStage1()
        }, 300)

        // Stage 2: Logo scale lagi (800-1600ms)
        Handler(Looper.getMainLooper()).postDelayed({
            animateStage2()
        }, 1100)

        // Stage 3: Background gradient, logo naik, teks muncul dari logo (1600-2400ms)
        Handler(Looper.getMainLooper()).postDelayed({
            animateStage3()
        }, 1900)

        // Pindah ke LoginActivity setelah animasi selesai
        Handler(Looper.getMainLooper()).postDelayed({
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            finish()
        }, 3800)
    }

    private fun animateStage1() {
        // Rotasi dan scale logoContainer
        val rotateAnimator = ObjectAnimator.ofFloat(logoContainer, "rotation", 0f, 360f)
        val scaleXAnimator = ObjectAnimator.ofFloat(logoContainer, "scaleX", 1f, 1.3f)
        val scaleYAnimator = ObjectAnimator.ofFloat(logoContainer, "scaleY", 1f, 1.3f)

// Glow Animator: Lebih intens untuk efek visible
        val glowAnimator = ValueAnimator.ofFloat(0f, 1f)
        glowAnimator.duration = 600
        glowAnimator.interpolator = AccelerateDecelerateInterpolator()
        glowAnimator.addUpdateListener { animator ->
            val glowIntensity = animator.animatedValue as Float
            val currentAlpha = (0.1f + glowIntensity * 0.8f)  // Dari 0.1 ke 0.9 – lebih dramatis!
            // Apply ke outer 3 layers biar glow "berlapis"
            shadowView1.alpha = currentAlpha
            shadowView2.alpha = currentAlpha * 0.8f  // Inner sedikit lebih redup
            shadowView3.alpha = currentAlpha * 0.6f
            android.util.Log.d("GlowDebug", "Stage1 Alpha: $currentAlpha")  // Debug log
        }
        // Reset alpha setelah animasi selesai (biar gak stuck)
        glowAnimator.addListener(object : android.animation.AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: android.animation.Animator) {
                shadowView1.alpha = 0.15f  // Kembali ke setup awal
                shadowView2.alpha = 0.2f
                shadowView3.alpha = 0.25f
            }
        })

        rotateAnimator.duration = 600
        scaleXAnimator.duration = 600
        scaleYAnimator.duration = 600

        rotateAnimator.interpolator = AccelerateDecelerateInterpolator()
        scaleXAnimator.interpolator = AccelerateDecelerateInterpolator()
        scaleYAnimator.interpolator = AccelerateDecelerateInterpolator()

        val animatorSet = AnimatorSet()
        // TAMBAHAN: Play together dengan glowAnimator
        animatorSet.playTogether(rotateAnimator, scaleXAnimator, scaleYAnimator, glowAnimator)
        animatorSet.start()
    }

    private fun animateStage2() {
        // Scale logoContainer lagi
        val scaleXAnimator = ObjectAnimator.ofFloat(logoContainer, "scaleX", 1.3f, 1.5f)
        val scaleYAnimator = ObjectAnimator.ofFloat(logoContainer, "scaleY", 1.3f, 1.5f)

// Glow Animator: Sama seperti stage1, tapi duration 400ms
        val glowAnimator = ValueAnimator.ofFloat(0f, 1f)
        glowAnimator.duration = 400
        glowAnimator.interpolator = AccelerateDecelerateInterpolator()
        glowAnimator.addUpdateListener { animator ->
            val glowIntensity = animator.animatedValue as Float
            val currentAlpha = (0.1f + glowIntensity * 0.8f)
            shadowView1.alpha = currentAlpha
            shadowView2.alpha = currentAlpha * 0.8f
            shadowView3.alpha = currentAlpha * 0.6f
            android.util.Log.d("GlowDebug", "Stage2 Alpha: $currentAlpha")
        }
        glowAnimator.addListener(object : android.animation.AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: android.animation.Animator) {
                shadowView1.alpha = 0.15f
                shadowView2.alpha = 0.2f
                shadowView3.alpha = 0.25f
            }
        })

        scaleXAnimator.duration = 400
        scaleYAnimator.duration = 400

        scaleXAnimator.interpolator = AccelerateDecelerateInterpolator()
        scaleYAnimator.interpolator = AccelerateDecelerateInterpolator()

        val animatorSet = AnimatorSet()
        // TAMBAHAN: Play together dengan glowAnimator
        animatorSet.playTogether(scaleXAnimator, scaleYAnimator, glowAnimator)
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

        // Ganti ikon menjadi versi putih
        logoImageView.setImageResource(R.drawable.ic_checkmark_putih)

        // Fade in background
        val backgroundAlpha = ValueAnimator.ofInt(0, 255)
        backgroundAlpha.duration = 600
        backgroundAlpha.interpolator = AccelerateDecelerateInterpolator()
        backgroundAlpha.addUpdateListener { animator ->
            gradientDrawable.alpha = animator.animatedValue as Int
        }
        backgroundAlpha.start()

        // Logo naik ke atas dan mengecil sedikit
        val logoMoveUp = ObjectAnimator.ofFloat(logoContainer, "translationY", 0f, -120f)
        val logoScaleDownX = ObjectAnimator.ofFloat(logoContainer, "scaleX", 1.5f, 1.2f)
        val logoScaleDownY = ObjectAnimator.ofFloat(logoContainer, "scaleY", 1.5f, 1.2f)

        logoMoveUp.duration = 600
        logoScaleDownX.duration = 600
        logoScaleDownY.duration = 600

        logoMoveUp.interpolator = AccelerateDecelerateInterpolator()
        logoScaleDownX.interpolator = AccelerateDecelerateInterpolator()
        logoScaleDownY.interpolator = AccelerateDecelerateInterpolator()

        val logoAnimatorSet = AnimatorSet()
        logoAnimatorSet.playTogether(logoMoveUp, logoScaleDownX, logoScaleDownY)
        logoAnimatorSet.start()

        // Hitung dynamic offset berdasarkan logo height
        val displayMetrics = resources.displayMetrics
        val logoApproxHeight = (displayMetrics.widthPixels * 0.25f * 1.2f) / displayMetrics.density
        val textStartY = - (120f + logoApproxHeight / 2)
        val textEndY = logoApproxHeight / 2 + 50f

        // Teks muncul dari posisi logo (zoom out + fade in)
        Handler(Looper.getMainLooper()).postDelayed({
            textContainer.visibility = View.VISIBLE

            // Hitung posisi logo center untuk particles
            val logoLocation = IntArray(2)
            logoContainer.getLocationOnScreen(logoLocation)
            val logoCenterX = logoLocation[0] + logoContainer.width / 2f
            val logoCenterY = logoLocation[1] + logoContainer.height / 2f

            // Start particles dengan delay kecil
            Handler(Looper.getMainLooper()).postDelayed({
                particleView.visibility = View.VISIBLE
                particleView.startBurstFromLogo(logoCenterX, logoCenterY, 800L)
            }, 50)

            // Reset posisi teks
            textContainer.scaleX = 0f
            textContainer.scaleY = 0f
            textContainer.translationX = 0f
            textContainer.translationY = textStartY

            val textScaleX = ObjectAnimator.ofFloat(textContainer, "scaleX", 0f, 1f)
            val textScaleY = ObjectAnimator.ofFloat(textContainer, "scaleY", 0f, 1f)
            val textFadeIn = ObjectAnimator.ofFloat(textContainer, "alpha", 0f, 1f)
            val textMoveDown = ObjectAnimator.ofFloat(textContainer, "translationY", textStartY, textEndY)

            textScaleX.duration = 700
            textScaleY.duration = 700
            textFadeIn.duration = 700
            textMoveDown.duration = 700

            textScaleX.interpolator = OvershootInterpolator(1.2f)
            textScaleY.interpolator = OvershootInterpolator(1.2f)
            textFadeIn.interpolator = AccelerateDecelerateInterpolator()
            textMoveDown.interpolator = AccelerateDecelerateInterpolator()

            val textAnimatorSet = AnimatorSet()
            textAnimatorSet.playTogether(textScaleX, textScaleY, textFadeIn, textMoveDown)
            textAnimatorSet.start()

            // Staggered tagline
            appNameTextView.alpha = 1f
            taglineTextView.alpha = 0f
            Handler(Looper.getMainLooper()).postDelayed({
                val taglineFade = ObjectAnimator.ofFloat(taglineTextView, "alpha", 0f, 1f)
                taglineFade.duration = 400
                taglineFade.interpolator = AccelerateDecelerateInterpolator()
                taglineFade.start()
            }, 300)

            Log.d("SplashDebug", "Stage 3 started: Particles from ($logoCenterX, $logoCenterY), text to $textEndY")

        }, 200)  // Delay setelah logo mulai naik
    }
}
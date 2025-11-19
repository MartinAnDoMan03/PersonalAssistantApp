package com.example.yourassistantyora

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.OvershootInterpolator
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import com.example.yourassistantyora.screen.GeometricShapesView
import com.example.yourassistantyora.screen.ParticleView
import com.example.yourassistantyora.screen.RippleWaveView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class SplashActivity : AppCompatActivity() {

    private lateinit var logoImageView: ImageView
    private lateinit var logoContainer: FrameLayout
    private lateinit var textContainer: LinearLayout
    private lateinit var appNameTextView: TextView
    private lateinit var taglineTextView: TextView
    private lateinit var rootLayout: ConstraintLayout

    private lateinit var particleView: ParticleView
    private lateinit var geometricShapesView: GeometricShapesView
    private lateinit var rippleWaveView: RippleWaveView

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

    //Data Fetching
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

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
        geometricShapesView = findViewById(R.id.geometricShapesView)
        rippleWaveView = findViewById(R.id.rippleWaveView)

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
        }, 200)  // Mulai lebih cepat

        // Stage 2: Logo scale lagi (800-1600ms)
        Handler(Looper.getMainLooper()).postDelayed({
            animateStage2()
        }, 900)  // Timing lebih smooth

        // Stage 3: Background gradient, logo naik, teks muncul dari logo (1600-2400ms)
        Handler(Looper.getMainLooper()).postDelayed({
            animateStage3()
        }, 1700)  // Timing lebih smooth

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
            Log.d("GlowDebug", "Stage1 Alpha: $currentAlpha")  // Debug log
        }
        // Reset alpha setelah animasi selesai (biar gak stuck)
        glowAnimator.addListener(object : android.animation.AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: android.animation.Animator) {
                shadowView1.alpha = 0.15f  // Kembali ke setup awal
                shadowView2.alpha = 0.2f
                shadowView3.alpha = 0.25f
            }
        })

        rotateAnimator.duration = 700
        scaleXAnimator.duration = 700
        scaleYAnimator.duration = 700

        // Gunakan interpolator yang lebih smooth
        val smoothInterpolator = android.view.animation.DecelerateInterpolator(1.5f)
        rotateAnimator.interpolator = smoothInterpolator
        scaleXAnimator.interpolator = smoothInterpolator
        scaleYAnimator.interpolator = smoothInterpolator

        val animatorSet = AnimatorSet()
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
            Log.d("GlowDebug", "Stage2 Alpha: $currentAlpha")
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
        animatorSet.playTogether(scaleXAnimator, scaleYAnimator, glowAnimator)
        animatorSet.start()
    }

    private fun animateStage3() {
        // Ganti ikon menjadi versi putih SEBELUM animasi dimulai
        logoImageView.setImageResource(R.drawable.ic_checkmark_putih)

        // Fade in geometric shapes & ripple waves
        geometricShapesView.alpha = 0f
        rippleWaveView.alpha = 0f

        val geometricFadeIn = ObjectAnimator.ofFloat(geometricShapesView, "alpha", 0f, 1f)
        geometricFadeIn.duration = 800
        geometricFadeIn.interpolator = AccelerateDecelerateInterpolator()

        val rippleFadeIn = ObjectAnimator.ofFloat(rippleWaveView, "alpha", 0f, 1f)
        rippleFadeIn.duration = 800
        rippleFadeIn.interpolator = AccelerateDecelerateInterpolator()

        val bgAnimSet = AnimatorSet()
        bgAnimSet.playTogether(geometricFadeIn, rippleFadeIn)
        bgAnimSet.start()

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

            // This handler will now wait for the final animation (700ms) to finish before checking the user and navigating.
            Handler(Looper.getMainLooper()).postDelayed({
                val currentUser = auth.currentUser
                if (currentUser != null) {
                    // Jika sudah login, ambil datanya dari Firestore
                    fetchUserAndNavigate()
                } else {
                    // Kalau belum login, arahkan ke login
                    val intent = Intent(this, MainActivity::class.java).apply {
                        putExtra("START_DESTINATION", "login")
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    }
                    startActivity(intent)
                    finish()
                }
            }, 1000)



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
    private fun fetchUserAndNavigate() {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            // This should rarely happen if currentUser is not null, but it's a safe fallback.
            navigateToHome("User")
            return
        }

        db.collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val username = document.getString("username") ?: "User"
                    navigateToHome(username)
                } else {
                    // Fallback if document doesn't exist for some reason
                    val fallbackName = auth.currentUser?.displayName ?: "New User"
                    navigateToHome(fallbackName)
                }
            }
            .addOnFailureListener {
                // Fallback on Firestore error
                navigateToHome("User")
            }
    }

    private fun navigateToHome(userName: String) {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("START_DESTINATION", "home")
            putExtra("USER_NAME", userName)
        }
        startActivity(intent)
        finish()
    }
}
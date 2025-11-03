package com.example.yourassistantyora

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.util.Log
import android.view.View
import kotlin.random.Random

class ParticleView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val particles = mutableListOf<Particle>()
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#5B6FD8")  // Ungu, ganti ke @color/particle_color kalau mau
    }
    private var animator: ValueAnimator? = null
    private var isAnimating = false
    private var startX: Float = 0f  // Posisi start particles (dari logo)
    private var startY: Float = 0f

    data class Particle(
        var x: Float,
        var y: Float,
        var vx: Float,
        var vy: Float,
        var size: Float,
        var alpha: Float = 1f
    )

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        Log.d("ParticleDebug", "View size changed: $w x $h")
    }

    fun startBurstFromLogo(logoCenterX: Float, logoCenterY: Float, duration: Long = 700L) {
        if (isAnimating) return

        startX = logoCenterX
        startY = logoCenterY
        Log.d("ParticleDebug", "Starting burst from logo pos: ($logoCenterX, $logoCenterY)")

        animator?.cancel()
        particles.clear()
        generateParticles(20)  // Naikkan ke 20 biar lebih keliatan

        isAnimating = true
        animator = ValueAnimator.ofFloat(0f, 1f).apply {
            this.duration = duration
            addUpdateListener { animation ->
                if (!isAnimating) return@addUpdateListener
                val progress = animation.animatedValue as Float
                updateParticles(progress)
                invalidate()
                Log.d("ParticleDebug", "Update progress: $progress, particles left: ${particles.size}")
            }
            addListener(object : android.animation.AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: android.animation.Animator) {
                    isAnimating = false
                    particles.clear()
                    invalidate()
                    Log.d("ParticleDebug", "Burst ended!")
                }
            })
            start()
        }
    }

    private fun generateParticles(count: Int) {
        repeat(count) {
            particles.add(
                Particle(
                    x = startX + randomFloat(-20f, 20f),  // Cluster lebih lebar di logo
                    y = startY + randomFloat(-20f, 20f),
                    vx = randomFloat(-100f, 100f),  // Spread horizontal lebih luas
                    vy = randomFloat(200f, 500f),  // Ke bawah lebih cepat & variatif
                    size = randomFloat(8f, 15f)  // Dots lebih gede
                )
            )
        }
        Log.d("ParticleDebug", "Generated $count particles – bigger & faster!")
    }

    private fun randomFloat(min: Float, max: Float): Float {
        return Random.nextFloat() * (max - min) + min
    }

    private fun updateParticles(progress: Float) {
        val time = progress * 4f  // Lebih dinamis
        val particlesToRemove = mutableListOf<Particle>()

        particles.forEach { particle ->
            particle.x += particle.vx * time * 0.015f
            particle.y += particle.vy * time * 0.015f
            particle.size *= (1f - progress * 0.4f)  // Shrink lebih lambat
            particle.alpha = 1f - (progress * 1.2f)  // Fade lebih cepat di akhir
            if (particle.alpha <= 0f) {
                particlesToRemove.add(particle)
            }
        }

        particles.removeAll(particlesToRemove)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (!isAnimating && particles.isEmpty()) return

        paint.alpha = (255 * 0.8f).toInt()
        particles.forEach { particle ->
            if (particle.alpha > 0f) {
                // Shadow ringan buat glow
                paint.setShadowLayer(4f, 0f, 0f, 0x20000000.toInt())
                paint.alpha = (255 * particle.alpha).toInt()
                canvas.drawCircle(particle.x, particle.y, particle.size, paint)
                paint.clearShadowLayer()
            }
        }
        Log.d("ParticleDebug", "Drew ${particles.count { it.alpha > 0f }} glowing particles")
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        animator?.cancel()
        isAnimating = false
    }
}
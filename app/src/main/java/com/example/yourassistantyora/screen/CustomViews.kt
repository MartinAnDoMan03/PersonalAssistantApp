package com.example.yourassistantyora.screen

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import kotlin.math.sin
import kotlin.random.Random

// ============================================================
// ParticleView - Efek particle burst dari logo
// ============================================================
class ParticleView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val particles = mutableListOf<Particle>()
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#5B6FD8")
    }
    private var animator: ValueAnimator? = null
    private var isAnimating = false
    private var startX: Float = 0f
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
        generateParticles(30)

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
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
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
                    x = startX + randomFloat(-30f, 30f),
                    y = startY + randomFloat(-30f, 30f),
                    vx = randomFloat(-120f, 120f),
                    vy = randomFloat(250f, 600f),
                    size = randomFloat(10f, 20f)
                )
            )
        }
        Log.d("ParticleDebug", "Generated $count particles – BIGGER & FASTER!")
    }

    private fun randomFloat(min: Float, max: Float): Float {
        return Random.nextFloat() * (max - min) + min
    }

    private fun updateParticles(progress: Float) {
        val time = progress * 4f
        val particlesToRemove = mutableListOf<Particle>()

        particles.forEach { particle ->
            particle.x += particle.vx * time * 0.015f
            particle.y += particle.vy * time * 0.015f
            particle.size *= (1f - progress * 0.4f)
            particle.alpha = 1f - (progress * 1.2f)
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
                paint.setShadowLayer(6f, 0f, 0f, 0x30000000.toInt())
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

// ============================================================
// GeometricShapesView - Floating geometric shapes
// ============================================================
class GeometricShapesView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = 0x26FFFFFF
    }

    private val shapes = mutableListOf<GeometricShape>()
    private var animator: ValueAnimator? = null

    init {
        shapes.add(GeometricShape(ShapeType.SQUARE, 220f, -0.08f, 0.08f, 45f, 6000L))
        shapes.add(GeometricShape(ShapeType.SQUARE, 160f, 1.05f, 0.88f, -30f, 8000L, true))
        shapes.add(GeometricShape(ShapeType.CIRCLE, 130f, 0.03f, 0.58f, 0f, 5000L))
        shapes.add(GeometricShape(ShapeType.CIRCLE, 100f, 0.88f, 0.12f, 0f, 7000L))
        shapes.add(GeometricShape(ShapeType.SQUARE, 110f, 0.12f, 0.88f, 15f, 6500L, true))
        shapes.add(GeometricShape(ShapeType.CIRCLE, 90f, 0.5f, 0.05f, 0f, 5500L))
        shapes.add(GeometricShape(ShapeType.SQUARE, 85f, 0.95f, 0.45f, 60f, 7500L, true))
    }

    // === FUNGSI HARUS DI ATAS onAttachedToWindow() ===
    private fun startShapesAnimation() {
        animator = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = Long.MAX_VALUE
            interpolator = AccelerateDecelerateInterpolator()
            repeatCount = ValueAnimator.INFINITE
            addUpdateListener { invalidate() }
            start()
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        startShapesAnimation()  // Sekarang TIDAK ERROR
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        animator?.cancel()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val currentTime = System.currentTimeMillis()
        shapes.forEach { shape ->
            drawShape(canvas, shape, currentTime)
        }
    }

    private fun drawShape(canvas: Canvas, shape: GeometricShape, currentTime: Long) {
        val w = width.toFloat()
        val h = height.toFloat()

        val progress = ((currentTime % shape.duration).toFloat() / shape.duration)
        val animProgress = if (shape.reverse) 1f - progress else progress
        val floatOffset = sin(animProgress * Math.PI.toFloat() * 2) * 50f
        val animatedRotation = shape.rotation + (animProgress * 180f)

        val x = w * shape.startX
        val y = h * shape.startY + floatOffset

        canvas.save()
        canvas.translate(x, y)
        canvas.rotate(animatedRotation)

        when (shape.type) {
            ShapeType.SQUARE -> {
                val halfSize = shape.size / 2f
                val rect = RectF(-halfSize, -halfSize, halfSize, halfSize)
                canvas.drawRoundRect(rect, 30f, 30f, paint)
            }
            ShapeType.CIRCLE -> {
                canvas.drawCircle(0f, 0f, shape.size / 2f, paint)
            }
        }
        canvas.restore()
    }

    private data class GeometricShape(
        val type: ShapeType,
        val size: Float,
        val startX: Float,
        val startY: Float,
        val rotation: Float,
        val duration: Long,
        val reverse: Boolean = false
    )

    private enum class ShapeType { SQUARE, CIRCLE }
}

// ============================================================
// RippleWaveView - Efek gelombang ripple
// ============================================================
class RippleWaveView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 3f
        color = 0x26FFFFFF
    }

    private val waves = mutableListOf<Wave>()
    private var animator: ValueAnimator? = null
    private var centerX = 0f
    private var centerY = 0f

    data class Wave(
        var radius: Float,
        var alpha: Float,
        val maxRadius: Float,
        val speed: Float
    )

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        centerX = w / 2f
        centerY = h / 2f
        initWaves()
    }

    private fun initWaves() {
        waves.clear()
        val maxRadius = (width.coerceAtLeast(height) * 0.9f)
        waves.add(Wave(0f, 1f, maxRadius, 1.2f))
        waves.add(Wave(maxRadius * 0.25f, 0.8f, maxRadius, 1.2f))
        waves.add(Wave(maxRadius * 0.5f, 0.6f, maxRadius, 1.2f))
        waves.add(Wave(maxRadius * 0.75f, 0.4f, maxRadius, 1.2f))
    }

    // === updateWaves() HARUS DI ATAS startAnimation() ===
    private fun updateWaves() {
        waves.forEach { wave ->
            wave.radius += wave.speed * 4f
            wave.alpha = 1f - (wave.radius / wave.maxRadius)
            if (wave.radius >= wave.maxRadius) {
                wave.radius = 0f
                wave.alpha = 1f
            }
        }
    }

    private fun startAnimation() {
        animator = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = 3000L
            repeatCount = ValueAnimator.INFINITE
            interpolator = AccelerateDecelerateInterpolator()
            addUpdateListener {
                updateWaves()  // Sekarang TIDAK ERROR
                invalidate()
            }
            start()
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        startAnimation()  // OK
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        animator?.cancel()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        waves.forEach { wave ->
            if (wave.alpha > 0) {
                paint.alpha = (wave.alpha * 38).toInt()
                canvas.drawCircle(centerX, centerY, wave.radius, paint)
            }
        }
    }
}
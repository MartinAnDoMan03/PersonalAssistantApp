package com.example.yourassistantyora

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
// ParticleView - Untuk efek particle burst dari logo
// ============================================================
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
        generateParticles(30)  // 20 → 30 (lebih banyak particles!)

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
                    x = startX + randomFloat(-30f, 30f),  // -20f → -30f (cluster lebih lebar)
                    y = startY + randomFloat(-30f, 30f),  // -20f → -30f
                    vx = randomFloat(-120f, 120f),  // -100f → -120f (spread lebih luas)
                    vy = randomFloat(250f, 600f),  // 200f-500f → 250f-600f (lebih cepat)
                    size = randomFloat(10f, 20f)  // 8f-15f → 10f-20f (lebih gede!)
                )
            )
        }
        Log.d("ParticleDebug", "Generated $count particles – BIGGER & FASTER!")
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
                // Shadow lebih tebal buat glow effect
                paint.setShadowLayer(6f, 0f, 0f, 0x30000000.toInt())  // 4f → 6f, 0x20 → 0x30
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
// GeometricShapesView - Untuk floating geometric shapes
// ============================================================
class GeometricShapesView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = 0x26FFFFFF // 0x1A → 0x26 (15% opacity, lebih visible!)
    }

    private val shapes = mutableListOf<GeometricShape>()
    private var animator: ValueAnimator? = null

    init {
        // Initialize shapes with different properties - UKURAN DIPERBESAR!
        shapes.add(GeometricShape(
            type = ShapeType.SQUARE,
            size = 220f,  // 150f → 220f
            startX = -0.08f,  // Keluar lebih jauh
            startY = 0.08f,
            rotation = 45f,
            duration = 6000L
        ))

        shapes.add(GeometricShape(
            type = ShapeType.SQUARE,
            size = 160f,  // 100f → 160f
            startX = 1.05f,  // Keluar lebih jauh
            startY = 0.88f,
            rotation = -30f,
            duration = 8000L,
            reverse = true
        ))

        shapes.add(GeometricShape(
            type = ShapeType.CIRCLE,
            size = 130f,  // 80f → 130f
            startX = 0.03f,
            startY = 0.58f,
            rotation = 0f,
            duration = 5000L
        ))

        // Tambahan shapes untuk lebih dynamic - DIPERBESAR!
        shapes.add(GeometricShape(
            type = ShapeType.CIRCLE,
            size = 100f,  // 60f → 100f
            startX = 0.88f,
            startY = 0.12f,
            rotation = 0f,
            duration = 7000L
        ))

        shapes.add(GeometricShape(
            type = ShapeType.SQUARE,
            size = 110f,  // 70f → 110f
            startX = 0.12f,
            startY = 0.88f,
            rotation = 15f,
            duration = 6500L,
            reverse = true
        ))

        // TAMBAHAN: Shape ekstra untuk lebih ramai
        shapes.add(GeometricShape(
            type = ShapeType.CIRCLE,
            size = 90f,
            startX = 0.5f,
            startY = 0.05f,
            rotation = 0f,
            duration = 5500L
        ))

        shapes.add(GeometricShape(
            type = ShapeType.SQUARE,
            size = 85f,
            startX = 0.95f,
            startY = 0.45f,
            rotation = 60f,
            duration = 7500L,
            reverse = true
        ))
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        startAnimations()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        animator?.cancel()
    }

    private fun startAnimations() {
        animator = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = Long.MAX_VALUE
            interpolator = AccelerateDecelerateInterpolator()
            repeatCount = ValueAnimator.INFINITE
            addUpdateListener {
                invalidate()
            }
            start()
        }
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

        // Calculate animated position
        val progress = ((currentTime % shape.duration).toFloat() / shape.duration)
        val animProgress = if (shape.reverse) 1f - progress else progress

        // Float animation (up and down) - DIPERBESAR!
        val floatOffset = sin(animProgress * Math.PI.toFloat() * 2) * 50f  // 30f → 50f

        // Rotation animation
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
                canvas.drawRoundRect(rect, 30f, 30f, paint)  // Corner radius 20f → 30f
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
        val startX: Float, // 0-1 range (percentage of width)
        val startY: Float, // 0-1 range (percentage of height)
        val rotation: Float,
        val duration: Long,
        val reverse: Boolean = false
    )

    private enum class ShapeType {
        SQUARE,
        CIRCLE
    }
}

// ============================================================
// RippleWaveView - Untuk efek gelombang ripple
// ============================================================
class RippleWaveView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 3f  // 2f → 3f (lebih tebal)
        color = 0x26FFFFFF // 0x1A → 0x26 (15% opacity, lebih visible)
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
        val maxRadius = (width.coerceAtLeast(height) * 0.9f)  // 0.7f → 0.9f (lebih besar)

        // 4 waves dengan timing berbeda - TAMBAH 1 WAVE
        waves.add(Wave(0f, 1f, maxRadius, 1.2f))
        waves.add(Wave(maxRadius * 0.25f, 0.8f, maxRadius, 1.2f))
        waves.add(Wave(maxRadius * 0.5f, 0.6f, maxRadius, 1.2f))
        waves.add(Wave(maxRadius * 0.75f, 0.4f, maxRadius, 1.2f))
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        startAnimation()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        animator?.cancel()
    }

    private fun startAnimation() {
        animator = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = 3000L
            repeatCount = ValueAnimator.INFINITE
            interpolator = AccelerateDecelerateInterpolator()
            addUpdateListener {
                updateWaves()
                invalidate()
            }
            start()
        }
    }

    private fun updateWaves() {
        waves.forEach { wave ->
            wave.radius += wave.speed * 4f  // 3f → 4f (lebih cepat)
            wave.alpha = 1f - (wave.radius / wave.maxRadius)

            if (wave.radius >= wave.maxRadius) {
                wave.radius = 0f
                wave.alpha = 1f
            }
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        waves.forEach { wave ->
            if (wave.alpha > 0) {
                paint.alpha = (wave.alpha * 38).toInt() // 26 → 38 (15% max opacity)
                canvas.drawCircle(centerX, centerY, wave.radius, paint)
            }
        }
    }
}
package com.yechy.example.animator
import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ValueAnimator
import android.content.res.Resources
import android.graphics.*
import android.graphics.drawable.Animatable
import android.graphics.drawable.Drawable
import android.util.TypedValue

/**
 *
 * Created by cloud on 2023/4/19.
 */
abstract class AnimIndicator() : Drawable(), Animatable {

    private val mUpdateListeners: HashMap<ValueAnimator, ValueAnimator.AnimatorUpdateListener> =
        HashMap()

    private var mAnimators : MutableList<ValueAnimator> = mutableListOf()
    private var alpha = 255
    private val ZERO_BOUNDS_RECT: Rect = Rect()
    private var drawBounds: Rect = ZERO_BOUNDS_RECT

    private var mHasAnimators = false

    private val mPaint: Paint = Paint()

    init {
        mPaint.color = Color.BLACK
        mPaint.style = Paint.Style.FILL
        mPaint.isAntiAlias = true
    }

    fun getColor(): Int {
        return mPaint.getColor()
    }

    fun setColor(color: Int) {
        mPaint.setColor(color)
    }

    override fun setAlpha(alpha: Int) {
        this.alpha = alpha
    }

    override fun getAlpha(): Int {
        return alpha
    }

    override fun getOpacity(): Int {
        return PixelFormat.OPAQUE
    }

    override fun setColorFilter(colorFilter: ColorFilter?) {}

    override fun draw(canvas: Canvas) {
        draw(canvas, mPaint)
    }

    abstract fun draw(canvas: Canvas, paint: Paint)

    abstract fun onCreateAnimators(): List<ValueAnimator>

    override fun start() {
        ensureAnimators()

        // If the animators has not ended, do nothing.
        if (isStarted()) {
            return
        }
        startAnimators()
        invalidateSelf()
    }

    private fun startAnimators() {
        if (mAnimators.size > 0) {
            val animatorSet = AnimatorSet()
            animatorSet.playTogether(mAnimators as Collection<Animator>?)
            for (i in 0 until mAnimators.size) {
                val animator: ValueAnimator = mAnimators[i]

                //when the animator restart , add the updateListener again because they
                // was removed by animator stop .
                val updateListener: ValueAnimator.AnimatorUpdateListener? = mUpdateListeners[animator]
                if (updateListener != null) {
                    animator.addUpdateListener(updateListener)
                }
            }
            animatorSet.start()
        }
    }

    private fun stopAnimators() {
        for (animator in mAnimators) {
            if (animator.isStarted) {
                animator.removeAllUpdateListeners()
                animator.end()
            }
        }
    }

    private fun ensureAnimators() {
        if (!mHasAnimators) {
            mAnimators.clear()
            mAnimators.addAll(onCreateAnimators())
            mHasAnimators = true
        }
    }

    override fun stop() {
        stopAnimators()
    }

    private fun isStarted(): Boolean {
        for (animator in mAnimators) {
            return animator.isStarted()
        }
        return false
    }

    override fun isRunning(): Boolean {
        for (animator in mAnimators) {
            return animator.isRunning()
        }
        return false
    }

    /**
     * Your should use this to add AnimatorUpdateListener when
     * create animator , otherwise , animator doesn't work when
     * the animation restart .
     * @param updateListener
     */
    fun addUpdateListener(
        animator: ValueAnimator,
        updateListener: ValueAnimator.AnimatorUpdateListener
    ) {
        mUpdateListeners[animator] = updateListener
    }

    override fun onBoundsChange(bounds: Rect) {
        super.onBoundsChange(bounds)
        setDrawBounds(bounds)
    }

    fun setDrawBounds(drawBounds: Rect) {
        setDrawBounds(drawBounds.left, drawBounds.top, drawBounds.right, drawBounds.bottom)
    }

    fun setDrawBounds(left: Int, top: Int, right: Int, bottom: Int) {
        drawBounds = Rect(left, top, right, bottom)
    }

    fun postInvalidate() {
        invalidateSelf()
    }

    fun getDrawBounds(): Rect {
        return drawBounds
    }

    fun getWidth(): Int {
        return drawBounds.width()
    }

    fun getHeight(): Int {
        return drawBounds.height()
    }

    fun centerX(): Int {
        return drawBounds.centerX()
    }

    fun centerY(): Int {
        return drawBounds.centerY()
    }

    fun exactCenterX(): Float {
        return drawBounds.exactCenterX()
    }

    fun exactCenterY(): Float {
        return drawBounds.exactCenterY()
    }

    val Float.dp
        get() = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            this,
            Resources.getSystem().displayMetrics
        )
}
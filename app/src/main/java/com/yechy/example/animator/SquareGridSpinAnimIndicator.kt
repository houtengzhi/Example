package com.yechy.example.animator
import android.animation.Keyframe
import android.animation.PropertyValuesHolder
import android.animation.ValueAnimator
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import kotlin.math.roundToInt

/**
 *
 * Created by cloud on 2023/4/19.
 */
class SquareGridSpinAnimIndicator: AnimIndicator() {

    companion object {
        private val INITIAL_ALPHA = (0.18 * 255).roundToInt()

        private const val DURATION = 1200L
    }

    var alphas = arrayOf(intArrayOf(INITIAL_ALPHA, INITIAL_ALPHA), intArrayOf(INITIAL_ALPHA, INITIAL_ALPHA))

    override fun draw(canvas: Canvas, paint: Paint) {
        val divider = 4f.dp
        val cornerRadius = 6f.dp
        val squareWidth = (getWidth() - divider) / 2
        for (row in 0..1) {
            for (colum in 0..1) {
                canvas.save()
                val translateX = colum * (squareWidth + divider)
                val translateY = row * (squareWidth + divider)
                canvas.translate(translateX, translateY)
                val rectf = RectF(0f, 0f, squareWidth, squareWidth)
                paint.color = Color.WHITE
                paint.alpha = alphas[row][colum]
                canvas.drawRoundRect(rectf, cornerRadius, cornerRadius, paint)
                canvas.restore()
            }
        }

    }

    override fun onCreateAnimators(): List<ValueAnimator> {
        val animators = mutableListOf<ValueAnimator>()
//        val fractions = floatArrayOf(0f, 0.39f, 0.4f, 1f)
//        val alphaArray = intArrayOf(INITIAL_ALPHA, INITIAL_ALPHA, (0.54 * 255).roundToInt(), INITIAL_ALPHA)

        val fractions = floatArrayOf(0f, 0.6f, 0.99f, 1f)
        val alphaArray = intArrayOf((0.54 * 255).roundToInt(), INITIAL_ALPHA, INITIAL_ALPHA, INITIAL_ALPHA)

        val keyframes = arrayOfNulls<Keyframe>(fractions.size)
        for (i in fractions.indices) {
            val keyframe = Keyframe.ofInt(fractions[i], alphaArray[i])
            keyframes[i] = keyframe
        }
        val propertyValuesHolder = PropertyValuesHolder.ofKeyframe("alpha", *keyframes)
        for (row in 0..1) {
            for (colum in 0..1) {
                val alphaAnimator = ValueAnimator.ofPropertyValuesHolder(propertyValuesHolder)
                alphaAnimator.duration = DURATION
                alphaAnimator.repeatCount = ValueAnimator.INFINITE
                alphaAnimator.interpolator = KeyFrameInterpolator.easeInOut(*fractions)
                alphaAnimator.addUpdateListener {
                    alphas[row][colum] = it.getAnimatedValue("alpha") as Int
                    postInvalidate()
                }
                animators.add(alphaAnimator)
            }
        }
        animators[0].startDelay = 0L
        animators[1].startDelay = DURATION / 4
        animators[2].startDelay = 3 * DURATION / 4
        animators[3].startDelay = 2 * DURATION / 4
        return animators
    }
}
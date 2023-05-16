package com.yechy.example.animator

import android.animation.TimeInterpolator
import android.view.animation.Interpolator

/**
 *
 * Created by cloud on 2023/4/21.
 */
class KeyFrameInterpolator(val interpolator: TimeInterpolator, vararg fractions: Float) : Interpolator {

    private var fractions: FloatArray

    companion object {

        fun easeInOut(vararg fractions: Float): KeyFrameInterpolator {
            val interpolator = KeyFrameInterpolator(Ease.inOut())
            interpolator.setFractions(*fractions)
            return interpolator
        }
    }

    init {
        this.fractions = fractions

    }

    fun setFractions(vararg fractions: Float) {
        this.fractions = fractions
    }

    override fun getInterpolation(input: Float): Float {
        if (fractions.size > 1) {
            for (i in 0 until fractions.size - 1) {
                val start = fractions[i]
                val end = fractions[i + 1]
                val duration = end - start
                if (input in start..end) {
                    val newInput = (input - start) / duration
                    return start + (interpolator.getInterpolation(newInput)
                            * duration)
                }
            }
        }
        return interpolator.getInterpolation(input)
    }
}
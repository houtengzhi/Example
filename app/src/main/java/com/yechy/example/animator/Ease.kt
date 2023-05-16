package com.yechy.example.animator

import android.view.animation.Interpolator
import androidx.core.view.animation.PathInterpolatorCompat

/**
 *
 * Created by cloud on 2023/4/21.
 */
class Ease {

    companion object {
        fun inOut(): Interpolator {
            return PathInterpolatorCompat.create(0.42f, 0f, 0.58f, 1f)
        }
    }
}
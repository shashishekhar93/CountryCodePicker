package com.smcoding.countrycodepicker.recyclerview_listeners.viewprovider

import android.animation.Animator
import android.animation.AnimatorInflater
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.view.View
import androidx.annotation.AnimatorRes
import com.smcoding.countrycodepicker.R
import androidx.core.view.isInvisible

/**
 * Manages the show/hide animations for scroller views (like the bubble).
 */
class VisibilityAnimationManager protected constructor(
    private val view: View,
    @AnimatorRes showAnimatorRes: Int,
    @AnimatorRes hideAnimatorRes: Int,
    private val pivotXRelative: Float,
    private val pivotYRelative: Float,
    hideDelay: Int
) {
    private val hideAnimator: AnimatorSet = AnimatorInflater.loadAnimator(view.context, hideAnimatorRes) as AnimatorSet
    private val showAnimator: AnimatorSet = AnimatorInflater.loadAnimator(view.context, showAnimatorRes) as AnimatorSet

    init {
        hideAnimator.apply {
            startDelay = hideDelay.toLong()
            setTarget(view)
            addListener(object : AnimatorListenerAdapter() {
                var wasCanceled = false
                override fun onAnimationEnd(animation: Animator) {
                    if (!wasCanceled) view.visibility = View.INVISIBLE
                    wasCanceled = false
                }
                override fun onAnimationCancel(animation: Animator) {
                    wasCanceled = true
                }
            })
        }
        showAnimator.setTarget(view)
        updatePivot()
    }

    /**
     * Cancels any pending hide animation and shows the view.
     */
    fun show() {
        hideAnimator.cancel()
        if (view.isInvisible) {
            view.visibility = View.VISIBLE
            updatePivot()
            showAnimator.start()
        }
    }

    /**
     * Starts the hide animation.
     */
    fun hide() {
        updatePivot()
        hideAnimator.start()
    }

    private fun updatePivot() {
        view.pivotX = pivotXRelative * view.measuredWidth
        view.pivotY = pivotYRelative * view.measuredHeight
    }

    /**
     * Builder for creating instances of [VisibilityAnimationManager].
     */
    class Builder(private val view: View) {
        private var showAnimatorResource: Int = R.animator.fastscroll__default_show
        private var hideAnimatorResource: Int = R.animator.fastscroll__default_hide
        private var hideDelay: Int = 1000
        private var pivotX: Float = 0.5f
        private var pivotY: Float = 0.5f

        fun withShowAnimator(@AnimatorRes resId: Int) = apply { showAnimatorResource = resId }
        fun withHideAnimator(@AnimatorRes resId: Int) = apply { hideAnimatorResource = resId }
        fun withHideDelay(delay: Int) = apply { hideDelay = delay }
        fun withPivotX(px: Float) = apply { pivotX = px }
        fun withPivotY(py: Float) = apply { pivotY = py }

        fun build() = VisibilityAnimationManager(view, showAnimatorResource, hideAnimatorResource, pivotX, pivotY, hideDelay)
    }
}

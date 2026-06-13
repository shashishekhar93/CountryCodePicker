package com.smcoding.countrycodepicker.recyclerview_listeners.viewprovider

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.smcoding.countrycodepicker.recyclerview_listeners.FastScroller

/**
 * Abstract class that provides the views (handle and bubble) and their behaviors for the [FastScroller].
 */
abstract class ScrollerViewProvider {
    private var scroller: FastScroller? = null
    
    // Lazy initialized behaviors
    private var mHandleBehavior: ViewBehavior? = null
    protected val handleBehavior: ViewBehavior?
        get() {
            if (mHandleBehavior == null) mHandleBehavior = provideHandleBehavior()
            return mHandleBehavior
        }

    private var mBubbleBehavior: ViewBehavior? = null
    protected val bubbleBehavior: ViewBehavior?
        get() {
            if (mBubbleBehavior == null) mBubbleBehavior = provideBubbleBehavior()
            return mBubbleBehavior
        }

    fun setFastScroller(scroller: FastScroller) {
        this.scroller = scroller
    }

    protected val context: Context?
        get() = scroller?.context

    protected fun getScroller(): FastScroller? = scroller

    /**
     * Provides the view to be used as a handle.
     * @param container The container [FastScroller] for the view to inflate properly.
     */
    abstract fun provideHandleView(container: ViewGroup?): View?

    /**
     * Provides the view to be used as a bubble.
     * @param container The container [FastScroller] for the view to inflate properly.
     */
    abstract fun provideBubbleView(container: ViewGroup?): View?

    /**
     * Returns the [TextView] inside the bubble view that shows the section title.
     */
    abstract fun provideBubbleTextView(): TextView?

    /**
     * Returns the position offset of the bubble relative to the handle.
     */
    abstract val bubbleOffset: Int

    protected abstract fun provideHandleBehavior(): ViewBehavior?

    protected abstract fun provideBubbleBehavior(): ViewBehavior?

    // Event callbacks to behaviors
    fun onHandleGrabbed() {
        handleBehavior?.onHandleGrabbed()
        bubbleBehavior?.onHandleGrabbed()
    }

    fun onHandleReleased() {
        handleBehavior?.onHandleReleased()
        bubbleBehavior?.onHandleReleased()
    }

    fun onScrollStarted() {
        handleBehavior?.onScrollStarted()
        bubbleBehavior?.onScrollStarted()
    }

    fun onScrollFinished() {
        handleBehavior?.onScrollFinished()
        bubbleBehavior?.onScrollFinished()
    }
}

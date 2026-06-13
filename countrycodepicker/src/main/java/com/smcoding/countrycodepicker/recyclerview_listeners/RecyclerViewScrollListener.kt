package com.smcoding.countrycodepicker.recyclerview_listeners

import androidx.recyclerview.widget.RecyclerView

/**
 * Listener that updates the [FastScroller] position as the [RecyclerView] is scrolled.
 */
class RecyclerViewScrollListener(private val scroller: FastScroller) : RecyclerView.OnScrollListener() {

    private val listeners = mutableListOf<ScrollerListener>()
    private var oldScrollState = RecyclerView.SCROLL_STATE_IDLE

    fun addScrollerListener(listener: ScrollerListener) {
        listeners.add(listener)
    }

    override fun onScrollStateChanged(recyclerView: RecyclerView, newScrollState: Int) {
        super.onScrollStateChanged(recyclerView, newScrollState)
        
        // Check for start/finish of scroll event
        if (newScrollState == RecyclerView.SCROLL_STATE_IDLE && oldScrollState != RecyclerView.SCROLL_STATE_IDLE) {
            scroller.viewProvider?.onScrollFinished()
        } else if (newScrollState != RecyclerView.SCROLL_STATE_IDLE && oldScrollState == RecyclerView.SCROLL_STATE_IDLE) {
            scroller.viewProvider?.onScrollStarted()
        }
        oldScrollState = newScrollState
    }

    override fun onScrolled(rv: RecyclerView, dx: Int, dy: Int) {
        if (scroller.shouldUpdateHandlePosition()) {
            updateHandlePosition(rv)
        }
    }

    /**
     * Calculates the relative position of the scroll and updates the handle.
     */
    fun updateHandlePosition(rv: RecyclerView) {
        val relativePos = if (scroller.isVertical) {
            val denom = rv.computeVerticalScrollRange() - rv.computeVerticalScrollExtent()
            if (denom > 0) rv.computeVerticalScrollOffset() / denom.toFloat() else 0f
        } else {
            val denom = rv.computeHorizontalScrollRange() - rv.computeHorizontalScrollExtent()
            if (denom > 0) rv.computeHorizontalScrollOffset() / denom.toFloat() else 0f
        }
        
        scroller.setScrollerPosition(relativePos)
        notifyListeners(relativePos)
    }

    private fun notifyListeners(relativePos: Float) {
        listeners.forEach { it.onScroll(relativePos) }
    }

    interface ScrollerListener {
        fun onScroll(relativePos: Float)
    }
}

package com.smcoding.countrycodepicker.recyclerviewfastscroll

import androidx.recyclerview.widget.RecyclerView
class RecyclerViewScrollListener(private val scroller: FastScroller) : RecyclerView.OnScrollListener() {

    private val listeners = mutableListOf<ScrollerListener>()
    private var oldScrollState = RecyclerView.SCROLL_STATE_IDLE

    fun addScrollerListener(listener: ScrollerListener) {
        listeners.add(listener)
    }

    override fun onScrollStateChanged(recyclerView: RecyclerView, newScrollState: Int) {
        super.onScrollStateChanged(recyclerView, newScrollState)
        if ((newScrollState == RecyclerView.SCROLL_STATE_IDLE) && (oldScrollState != RecyclerView.SCROLL_STATE_IDLE)) {
            scroller.viewProvider?.onScrollFinished()
        } else if ((newScrollState != RecyclerView.SCROLL_STATE_IDLE) && (oldScrollState == RecyclerView.SCROLL_STATE_IDLE)) {
            scroller.viewProvider?.onScrollStarted()
        }
        oldScrollState = newScrollState
    }

    override fun onScrolled(rv: RecyclerView, dx: Int, dy: Int) {
        if (scroller.shouldUpdateHandlePosition()) {
            updateHandlePosition(rv)
        }
    }

    fun updateHandlePosition(rv: RecyclerView) {
        val relativePos = if (scroller.isVertical) {
            val offset = rv.computeVerticalScrollOffset()
            val extent = rv.computeVerticalScrollExtent()
            val range = rv.computeVerticalScrollRange()
            val denom = range - extent
            if (denom > 0) offset / denom.toFloat() else 0f
        } else {
            val offset = rv.computeHorizontalScrollOffset()
            val extent = rv.computeHorizontalScrollExtent()
            val range = rv.computeHorizontalScrollRange()
            val denom = range - extent
            if (denom > 0) offset / denom.toFloat() else 0f
        }
        scroller.setScrollerPosition(relativePos)
        notifyListeners(relativePos)
    }

    private fun notifyListeners(relativePos: Float) {
        for (listener in listeners) listener.onScroll(relativePos)
    }

    interface ScrollerListener {
        fun onScroll(relativePos: Float)
    }
}

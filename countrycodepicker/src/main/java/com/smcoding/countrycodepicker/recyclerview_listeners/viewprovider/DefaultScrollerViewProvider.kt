package com.smcoding.countrycodepicker.recyclerview_listeners.viewprovider

import android.graphics.drawable.InsetDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.smcoding.countrycodepicker.R
import com.smcoding.countrycodepicker.recyclerview_listeners.Utils

/**
 * Default implementation of [ScrollerViewProvider].
 * Provides a simple handle and a speech bubble as the section indicator.
 */
class DefaultScrollerViewProvider : ScrollerViewProvider() {
    private var bubble: View? = null
    private var handle: View? = null

    override fun provideHandleView(container: ViewGroup?): View? {
        val scroller = getScroller() ?: return null
        val context = scroller.context
        
        val handleView = View(context)
        handle = handleView

        // Adjust insets based on orientation
        val inset = context.resources.getDimensionPixelSize(R.dimen.fastscroll__handle_inset)
        val vInset = if (scroller.isVertical) 0 else inset
        val hInset = if (scroller.isVertical) inset else 0
        
        val drawable = ContextCompat.getDrawable(context, R.drawable.fastscroll__default_handle)
        val handleBg = InsetDrawable(drawable, hInset, vInset, hInset, vInset)
        Utils.setBackground(handleView, handleBg)

        // Set dimensions
        val handleWidth = context.resources.getDimensionPixelSize(if (scroller.isVertical) R.dimen.fastscroll__handle_clickable_width else R.dimen.fastscroll__handle_height)
        val handleHeight = context.resources.getDimensionPixelSize(if (scroller.isVertical) R.dimen.fastscroll__handle_height else R.dimen.fastscroll__handle_clickable_width)
        handleView.layoutParams = ViewGroup.LayoutParams(handleWidth, handleHeight)

        return handleView
    }

    override fun provideBubbleView(container: ViewGroup?): View? {
        val context = context ?: return null
        bubble = LayoutInflater.from(context).inflate(R.layout.custom_scrollbar, container, false)
        return bubble
    }

    override fun provideBubbleTextView(): TextView? = bubble as? TextView

    override val bubbleOffset: Int
        get() {
            val scroller = getScroller() ?: return 0
            val h = handle ?: return 0
            val b = bubble ?: return 0
            return if (scroller.isVertical) ((h.height / 2f) - b.height).toInt() 
                   else ((h.width / 2f) - b.width).toInt()
        }

    override fun provideHandleBehavior(): ViewBehavior? = null

    override fun provideBubbleBehavior(): ViewBehavior? {
        return bubble?.let {
            DefaultBubbleBehavior(VisibilityAnimationManager.Builder(it).withPivotX(1f).withPivotY(1f).build())
        }
    }
}

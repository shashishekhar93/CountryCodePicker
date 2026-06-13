package com.smcoding.countrycodepicker.recyclerviewfastscroll.viewprovider

import android.graphics.drawable.InsetDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.smcoding.countrycodepicker.R
import com.smcoding.countrycodepicker.recyclerviewfastscroll.Utils

class DefaultScrollerViewProvider : ScrollerViewProvider() {
    private var bubble: View? = null
    private var handle: View? = null

    override fun provideHandleView(container: ViewGroup?): View? {
        val scroller = getScroller() ?: return null
        val context = scroller.context
        handle = View(context)

        val verticalInset = if (scroller.isVertical) 0 else context.resources
            .getDimensionPixelSize(R.dimen.fastscroll__handle_inset)
        val horizontalInset = if (!scroller.isVertical) 0 else context.resources
            .getDimensionPixelSize(R.dimen.fastscroll__handle_inset)
        
        val drawable = ContextCompat.getDrawable(context, R.drawable.fastscroll__default_handle)
        val handleBg = InsetDrawable(drawable, horizontalInset, verticalInset, horizontalInset, verticalInset)
        Utils.setBackground(handle, handleBg)

        val handleWidth = context.resources
            .getDimensionPixelSize(if (scroller.isVertical) R.dimen.fastscroll__handle_clickable_width else R.dimen.fastscroll__handle_height)
        val handleHeight = context.resources
            .getDimensionPixelSize(if (scroller.isVertical) R.dimen.fastscroll__handle_height else R.dimen.fastscroll__handle_clickable_width)
        val params = ViewGroup.LayoutParams(handleWidth, handleHeight)
        handle?.layoutParams = params

        return handle
    }

    override fun provideBubbleView(container: ViewGroup?): View? {
        val context = getScroller()?.context ?: return null
        bubble = LayoutInflater.from(context)
            .inflate(R.layout.fastscroll__default_bubble, container, false)
        return bubble
    }

    override fun provideBubbleTextView(): TextView? {
        return bubble as? TextView
    }

    override val bubbleOffset: Int
        get() {
            val scroller = getScroller() ?: return 0
            val h = handle ?: return 0
            val b = bubble ?: return 0
            return if (scroller.isVertical) ((h.height.toFloat() / 2f) - b.height).toInt() 
                   else ((h.width.toFloat() / 2f) - b.width).toInt()
        }

    override fun provideHandleBehavior(): ViewBehavior? {
        return null
    }

    override fun provideBubbleBehavior(): ViewBehavior? {
        val b = bubble ?: return null
        return DefaultBubbleBehavior(
            VisibilityAnimationManager.Builder(b).withPivotX(1f).withPivotY(1f).build()
        )
    }
}

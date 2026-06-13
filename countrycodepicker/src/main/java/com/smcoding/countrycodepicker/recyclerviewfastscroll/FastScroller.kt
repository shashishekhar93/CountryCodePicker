package com.smcoding.countrycodepicker.recyclerviewfastscroll

import android.content.Context
import android.content.res.TypedArray
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.widget.TextViewCompat
import androidx.recyclerview.widget.RecyclerView
import com.smcoding.countrycodepicker.R
import com.smcoding.countrycodepicker.recyclerviewfastscroll.viewprovider.DefaultScrollerViewProvider
import com.smcoding.countrycodepicker.recyclerviewfastscroll.viewprovider.ScrollerViewProvider

class FastScroller @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : LinearLayout(context, attrs, defStyle) {
    
    private val scrollListener = RecyclerViewScrollListener(this)
    private var recyclerView: RecyclerView? = null

    private var bubble: View? = null
    private var handle: View? = null
    private var bubbleTextView: TextView? = null

    private var bubbleOffset = 0
    private var handleColor = 0
    private var bubbleColor = 0
    private var bubbleTextAppearance = 0
    private var scrollerOrientation = 0

    private var maxVisibility: Int
    private var manuallyChangingPosition = false

    var viewProvider: ScrollerViewProvider? = null
        private set
    private var titleProvider: SectionTitleProvider? = null

    init {
        clipChildren = false
        val style: TypedArray = context.obtainStyledAttributes(
            attrs,
            R.styleable.fastscroll__fastScroller,
            R.attr.fastscroll__style,
            0
        )
        try {
            bubbleColor = style.getColor(
                R.styleable.fastscroll__fastScroller_fastscroll__bubbleColor,
                STYLE_NONE
            )
            handleColor = style.getColor(
                R.styleable.fastscroll__fastScroller_fastscroll__handleColor,
                STYLE_NONE
            )
            bubbleTextAppearance = style.getResourceId(
                R.styleable.fastscroll__fastScroller_fastscroll__bubbleTextAppearance,
                STYLE_NONE
            )
        } finally {
            style.recycle()
        }
        maxVisibility = visibility
        setViewProvider(DefaultScrollerViewProvider())
    }

    /**
     * Attach the [FastScroller] to [RecyclerView].
     */
    fun setRecyclerView(recyclerView: RecyclerView) {
        this.recyclerView = recyclerView
        if (recyclerView.adapter is SectionTitleProvider) {
            titleProvider = recyclerView.adapter as SectionTitleProvider
        }
        recyclerView.addOnScrollListener(scrollListener)
        invalidateVisibility()
        recyclerView.setOnHierarchyChangeListener(object : OnHierarchyChangeListener {
            override fun onChildViewAdded(parent: View?, child: View?) {
                invalidateVisibility()
            }

            override fun onChildViewRemoved(parent: View?, child: View?) {
                invalidateVisibility()
            }
        })
    }

    override fun setOrientation(orientation: Int) {
        scrollerOrientation = orientation
        super.setOrientation(if (orientation == HORIZONTAL) VERTICAL else HORIZONTAL)
    }

    fun setBubbleColor(color: Int) {
        bubbleColor = color
        invalidate()
    }

    fun setHandleColor(color: Int) {
        handleColor = color
        invalidate()
    }

    fun setBubbleTextAppearance(textAppearanceResourceId: Int) {
        bubbleTextAppearance = textAppearanceResourceId
        invalidate()
    }

    fun addScrollerListener(listener: RecyclerViewScrollListener.ScrollerListener) {
        scrollListener.addScrollerListener(listener)
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        super.onLayout(changed, l, t, r, b)

        initHandleMovement()
        bubbleOffset = viewProvider?.bubbleOffset ?: 0

        applyStyling()

        if (!isInEditMode) {
            recyclerView?.let { scrollListener.updateHandlePosition(it) }
        }
    }

    private fun applyStyling() {
        bubbleTextView?.let { if (bubbleColor != STYLE_NONE) setBackgroundTint(it, bubbleColor) }
        handle?.let { if (handleColor != STYLE_NONE) setBackgroundTint(it, handleColor) }
        bubbleTextView?.let {
            if (bubbleTextAppearance != STYLE_NONE) {
                TextViewCompat.setTextAppearance(it, bubbleTextAppearance)
            }
        }
    }

    private fun setBackgroundTint(view: View, color: Int) {
        val background = view.background ?: return
        val wrapped = DrawableCompat.wrap(background)
        DrawableCompat.setTint(wrapped.mutate(), color)
        Utils.setBackground(view, wrapped)
    }

    private fun initHandleMovement() {
        handle?.setOnTouchListener { _, event ->
            parent.requestDisallowInterceptTouchEvent(true)
            when (event.action) {
                MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> {
                    if (titleProvider != null && event.action == MotionEvent.ACTION_DOWN) {
                        viewProvider?.onHandleGrabbed()
                    }
                    manuallyChangingPosition = true
                    val relativePos = getRelativeTouchPosition(event)
                    setScrollerPosition(relativePos)
                    setRecyclerViewPosition(relativePos)
                    true
                }
                MotionEvent.ACTION_UP -> {
                    manuallyChangingPosition = false
                    if (titleProvider != null) viewProvider?.onHandleReleased()
                    true
                }
                else -> false
            }
        }
    }

    private fun getRelativeTouchPosition(event: MotionEvent): Float {
        val h = handle ?: return 0f
        return if (isVertical) {
            val yInParent = event.rawY - Utils.getViewRawY(h)
            yInParent / (height - h.height)
        } else {
            val xInParent = event.rawX - Utils.getViewRawX(h)
            xInParent / (width - h.width)
        }
    }

    override fun setVisibility(visibility: Int) {
        maxVisibility = visibility
        invalidateVisibility()
    }

    private fun invalidateVisibility() {
        val adapter = recyclerView?.adapter
        if (adapter == null || adapter.itemCount == 0 || recyclerView?.getChildAt(0) == null ||
            isRecyclerViewNotScrollable() || maxVisibility != View.VISIBLE
        ) {
            super.setVisibility(View.INVISIBLE)
        } else {
            super.setVisibility(View.VISIBLE)
        }
    }

    private fun isRecyclerViewNotScrollable(): Boolean {
        val rv = recyclerView ?: return true
        val adapter = rv.adapter ?: return true
        val firstChild = rv.getChildAt(0) ?: return true
        return if (isVertical) {
            firstChild.height * adapter.itemCount <= rv.height
        } else {
            firstChild.width * adapter.itemCount <= rv.width
        }
    }

    private fun setRecyclerViewPosition(relativePos: Float) {
        val rv = recyclerView ?: return
        val adapter = rv.adapter ?: return
        val itemCount = adapter.itemCount
        val targetPos = Utils.getValueInRange(
            0f,
            (itemCount - 1).toFloat(),
            (relativePos * itemCount).toInt().toFloat()
        ).toInt()
        rv.scrollToPosition(targetPos)
        titleProvider?.let {
            bubbleTextView?.text = it.getSectionTitle(targetPos)
        }
    }

    fun setScrollerPosition(relativePos: Float) {
        val b = bubble ?: return
        val h = handle ?: return
        if (isVertical) {
            b.y = Utils.getValueInRange(0f, (height - b.height).toFloat(), relativePos * (height - h.height) + bubbleOffset)
            h.y = Utils.getValueInRange(0f, (height - h.height).toFloat(), relativePos * (height - h.height))
        } else {
            b.x = Utils.getValueInRange(0f, (width - b.width).toFloat(), relativePos * (width - h.width) + bubbleOffset)
            h.x = Utils.getValueInRange(0f, (width - h.width).toFloat(), relativePos * (width - h.width))
        }
    }

    val isVertical: Boolean
        get() = scrollerOrientation == VERTICAL

    fun shouldUpdateHandlePosition(): Boolean {
        val rv = recyclerView
        return handle != null && !manuallyChangingPosition && rv != null && rv.childCount > 0
    }

    fun setViewProvider(viewProvider: ScrollerViewProvider) {
        removeAllViews()
        this.viewProvider = viewProvider
        viewProvider.setFastScroller(this)
        bubble = viewProvider.provideBubbleView(this)
        handle = viewProvider.provideHandleView(this)
        bubbleTextView = viewProvider.provideBubbleTextView()
        bubble?.let { addView(it) }
        handle?.let { addView(it) }
    }

    companion object {
        private const val STYLE_NONE = -1
    }
}

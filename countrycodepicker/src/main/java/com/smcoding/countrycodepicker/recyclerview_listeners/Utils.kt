package com.smcoding.countrycodepicker.recyclerview_listeners

import android.graphics.drawable.Drawable
import android.view.View

/**
 * Utility class for common view operations.
 */
object Utils {
    /**
     * Gets the raw Y position of a view in the window.
     */
    fun getViewRawY(view: View): Float {
        val location = IntArray(2)
        location[1] = view.y.toInt()
        (view.parent as? View)?.getLocationInWindow(location)
        return location[1].toFloat()
    }

    /**
     * Gets the raw X position of a view in the window.
     */
    fun getViewRawX(view: View): Float {
        val location = IntArray(2)
        location[0] = view.x.toInt()
        (view.parent as? View)?.getLocationInWindow(location)
        return location[0].toFloat()
    }

    /**
     * Clamps a value within a given range.
     */
    fun getValueInRange(min: Float, max: Float, value: Float): Float {
        return value.coerceIn(min, max)
    }

    /**
     * Sets the background of a view.
     */
    fun setBackground(view: View, drawable: Drawable?) {
        view.background = drawable
    }
}

package com.smcoding.countrycodepicker.recyclerview_listeners.viewprovider

/**
 * Interface that defines how a view (handle or bubble) should react to various scroller events.
 */
interface ViewBehavior {
    /** Called when the user touches and starts dragging the scroller handle. */
    fun onHandleGrabbed()
    /** Called when the user releases the scroller handle. */
    fun onHandleReleased()
    /** Called when the RecyclerView starts scrolling (programmatically or via touch). */
    fun onScrollStarted()
    /** Called when the RecyclerView stops scrolling. */
    fun onScrollFinished()
}

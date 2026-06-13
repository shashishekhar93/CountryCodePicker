package com.smcoding.countrycodepicker.recyclerview_listeners.viewprovider

/**
 * Default behavior for the bubble view: show when handle is grabbed, hide when released.
 */
class DefaultBubbleBehavior(private val animationManager: VisibilityAnimationManager) : ViewBehavior {
    override fun onHandleGrabbed() = animationManager.show()
    override fun onHandleReleased() = animationManager.hide()
    override fun onScrollStarted() {}
    override fun onScrollFinished() {}
}

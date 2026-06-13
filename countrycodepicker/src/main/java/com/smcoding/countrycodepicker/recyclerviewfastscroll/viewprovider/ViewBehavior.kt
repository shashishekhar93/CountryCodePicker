package com.smcoding.countrycodepicker.recyclerviewfastscroll.viewprovider

interface ViewBehavior {
    fun onHandleGrabbed()
    fun onHandleReleased()
    fun onScrollStarted()
    fun onScrollFinished()
}

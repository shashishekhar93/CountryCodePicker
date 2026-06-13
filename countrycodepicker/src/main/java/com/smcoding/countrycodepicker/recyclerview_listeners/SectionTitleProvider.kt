package com.smcoding.countrycodepicker.recyclerview_listeners

/**
 * Interface that provides a title for a specific position in the adapter.
 * Used by the [FastScroller] to display a bubble with the current section.
 */
interface SectionTitleProvider {
    /**
     * Gets the title of the section corresponding to the given position.
     */
    fun getSectionTitle(position: Int): String?
}

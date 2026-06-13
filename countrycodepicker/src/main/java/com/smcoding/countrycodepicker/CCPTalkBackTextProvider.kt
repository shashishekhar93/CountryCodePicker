package com.smcoding.countrycodepicker

/**
 * Interface to provide custom talkback text for accessibility.
 */
interface CCPTalkBackTextProvider {
    fun getTalkBackTextForCountry(country: CCPCountry?): String?
}

internal class InternalTalkBackTextProvider : CCPTalkBackTextProvider {
    /**
     * Default implementation of talkback text.
     * Example: "India phone code is +91"
     */
    override fun getTalkBackTextForCountry(country: CCPCountry?): String? {
        return country?.let { "${it.name} phone code is +${it.phoneCode}" }
    }
}

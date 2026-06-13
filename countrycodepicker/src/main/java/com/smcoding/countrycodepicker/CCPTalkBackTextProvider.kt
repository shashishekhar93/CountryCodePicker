package com.smcoding.countrycodepicker

interface CCPTalkBackTextProvider {
    fun getTalkBackTextForCountry(country: CCPCountry?): String?
}

internal class InternalTalkBackTextProvider : CCPTalkBackTextProvider {
    override fun getTalkBackTextForCountry(country: CCPCountry?): String? {
        if (country == null) {
            return null
        } else {
            return country.name + " phone code is +" + country.phoneCode
        }
    }
}



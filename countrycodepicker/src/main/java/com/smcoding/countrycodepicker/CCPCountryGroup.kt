package com.smcoding.countrycodepicker

import android.content.Context
import android.util.SparseArray
import com.smcoding.countrycodepicker.CCPCountry.Companion.getCountryForNameCodeFromLibraryMasterList

/**
 * Represents a group of countries that share the same phone code (e.g., +1 for North America).
 * It uses area codes to differentiate between countries within the group.
 */
class CCPCountryGroup private constructor(
    var defaultNameCode: String?,
    var areaCodeLength: Int,
    private val nameCodeToAreaCodesMap: Map<String?, String?>
) {
    /**
     * Identifies the specific country within the group based on the area code.
     */
    fun getCountryForAreaCode(
        context: Context,
        language: CountryCodePicker.Language,
        areaCode: String
    ): CCPCountry? {
        var nameCode = defaultNameCode
        for ((key, value) in nameCodeToAreaCodesMap) {
            if (value?.contains(areaCode) == true) {
                nameCode = key
                break
            }
        }
        return getCountryForNameCodeFromLibraryMasterList(context, language, nameCode)
    }

    companion object {
        private var countryGroups: SparseArray<CCPCountryGroup?>? = null

        /**
         * Initializes all known country groups.
         */
        private fun initializeGroups() {
            countryGroups = SparseArray<CCPCountryGroup?>().apply {
                put(358, createGroup358())
                put(44, createGroup44())
                put(1, createGroup1())
                put(7, createGroup7())
            }
        }

        private fun createGroup358() = CCPCountryGroup(
            "fi", 2, mapOf("ax" to "18") // Finland, Åland Islands
        )

        private fun createGroup44() = CCPCountryGroup(
            "gb", 4, mapOf(
                "gg" to "1481", // Guernsey
                "im" to "1624", // Isle of Man
                "je" to "1534"  // Jersey
            )
        )

        private fun createGroup1() = CCPCountryGroup(
            "us", 3, mapOf(
                "ag" to "268", "ai" to "264", "as" to "684", "bb" to "246",
                "bm" to "441", "bs" to "242", "dm" to "767", "do" to "809/829/849",
                "gd" to "473", "gu" to "671", "jm" to "876", "kn" to "869",
                "ky" to "345", "lc" to "758", "mp" to "670", "ms" to "664",
                "pr" to "787", "sx" to "721", "tc" to "649", "tt" to "868",
                "vc" to "784", "vg" to "284", "vi" to "340",
                "ca" to "204/226/236/249/250/289/306/343/365/403/416/418/431/437/438/450/506/514/519/579/581/587/600/601/604/613/639/647/705/709/769/778/780/782/807/819/825/867/873/902/905/"
            )
        )

        private fun createGroup7() = CCPCountryGroup(
            "ru", 1, mapOf("kz" to "6/7") // Russia, Kazakhstan
        )

        /**
         * Gets the country group associated with a given phone code.
         */
        @JvmStatic
        fun getCountryGroupForPhoneCode(countryCode: Int): CCPCountryGroup? {
            if (countryGroups == null) initializeGroups()
            return countryGroups?.get(countryCode)
        }
    }
}

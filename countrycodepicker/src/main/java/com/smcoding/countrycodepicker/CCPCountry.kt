package com.smcoding.countrycodepicker

import android.content.Context
import android.util.Log
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.IOException
import java.text.Collator
import java.util.Locale

/**
 * Data class representing a country with its name, phone code, and flag.
 * Implements [Comparable] to allow sorting by country name.
 */
class CCPCountry : Comparable<CCPCountry?> {
    var nameCode: String? = null
        set(value) { field = value?.uppercase() }

    var phoneCode: String? = null
    var name: String? = null
    var englishName: String? = null
    var flagResID: Int = DEFAULT_FLAG_RES

    constructor()

    constructor(nameCode: String, phoneCode: String?, name: String?, flagResID: Int) {
        this.nameCode = nameCode.uppercase()
        this.phoneCode = phoneCode
        this.name = name
        this.flagResID = flagResID
    }

    /**
     * Gets the resource ID for the country flag.
     * If not explicitly set, it attempts to find it based on the name code.
     */
    val flagID: Int
        get() {
            if (flagResID == DEFAULT_FLAG_RES) {
                flagResID = getFlagMasterResID(this)
            }
            return flagResID
        }

    /**
     * Logs country details for debugging.
     */
    fun log() {
        Log.d(TAG, "Country->$nameCode:$phoneCode:$name")
    }

    fun logString(): String = "${nameCode?.uppercase()} +$phoneCode ($name)"

    /**
     * Checks if this country matches the given search query.
     * Searches in name, name code, phone code, and english name.
     */
    fun isEligibleForQuery(query: String): Boolean {
        val lowerQuery = query.lowercase(Locale.getDefault())
        return containsQueryWord(this.name, lowerQuery) ||
                containsQueryWord(nameCode, lowerQuery) ||
                containsQueryWord(this.phoneCode, lowerQuery) ||
                containsQueryWord(this.englishName, lowerQuery)
    }

    private fun containsQueryWord(
        fieldValue: String?,
        query: String
    ): Boolean {
        return fieldValue?.lowercase()?.contains(query) ?: false
    }

    override fun compareTo(other: CCPCountry?): Int {
        return Collator.getInstance().compare(this.name, other?.name)
    }

    companion object {
        const val DEFAULT_FLAG_RES: Int = -99
        private const val TAG: String = "CCPCountry"
        
        var loadedLibraryMasterListLanguage: CountryCodePicker.Language? = null
        var dialogTitle: String? = null
        var searchHintMessage: String? = null
        var noResultFoundAckMessage: String? = null
        var loadedLibraryMaterList: MutableList<CCPCountry>? = null

        /**
         * Parses the XML file corresponding to the selected language to load country data.
         */
        fun loadDataFromXML(context: Context, language: CountryCodePicker.Language) {
            val countries = mutableListOf<CCPCountry>()
            var tempDialogTitle = ""
            var tempSearchHint = ""
            var tempNoResultAck = ""
            
            try {
                val xmlFactoryObject = XmlPullParserFactory.newInstance()
                val xmlPullParser = xmlFactoryObject.newPullParser()
                val resourceId = getRawResourceForLanguage(language)
                
                if (resourceId == 0) throw IOException("Resource not found")
                
                val ins = context.resources.openRawResource(resourceId)
                xmlPullParser.setInput(ins, "UTF-8")
                
                var event = xmlPullParser.eventType
                while (event != XmlPullParser.END_DOCUMENT) {
                    val tagName = xmlPullParser.name
                    when (event) {
                        XmlPullParser.END_TAG -> when (tagName) {
                            "country" -> countries.add(CCPCountry().apply {
                                nameCode = xmlPullParser.getAttributeValue(null, "name_code")
                                phoneCode = xmlPullParser.getAttributeValue(null, "phone_code")
                                englishName = xmlPullParser.getAttributeValue(null, "english_name")
                                name = xmlPullParser.getAttributeValue(null, "name")
                            })
                            "ccp_dialog_title" -> tempDialogTitle = xmlPullParser.getAttributeValue(null, "translation")
                            "ccp_dialog_search_hint_message" -> tempSearchHint = xmlPullParser.getAttributeValue(null, "translation")
                            "ccp_dialog_no_result_ack_message" -> tempNoResultAck = xmlPullParser.getAttributeValue(null, "translation")
                        }
                    }
                    event = xmlPullParser.next()
                }
                loadedLibraryMasterListLanguage = language
            } catch (e: Exception) {
                Log.e(TAG, "Error loading data from XML", e)
            }

            // Fallback to English if loading fails
            if (countries.isEmpty()) {
                loadedLibraryMasterListLanguage = CountryCodePicker.Language.ENGLISH
                loadedLibraryMaterList = libraryMasterCountriesEnglish
            } else {
                loadedLibraryMaterList = countries
            }

            dialogTitle = tempDialogTitle.ifEmpty { "Select a country" }
            searchHintMessage = tempSearchHint.ifEmpty { "Search..." }
            noResultFoundAckMessage = tempNoResultAck.ifEmpty { "Results not found" }

            loadedLibraryMaterList?.sort()
        }

        private fun getRawResourceForLanguage(language: CountryCodePicker.Language): Int {
            return when (language) {
                CountryCodePicker.Language.AFRIKAANS -> R.raw.ccp_afrikaans
                CountryCodePicker.Language.ARABIC -> R.raw.ccp_arabic
                CountryCodePicker.Language.BASQUE -> R.raw.ccp_basque
                CountryCodePicker.Language.BELARUSIAN -> R.raw.ccp_belarusian
                CountryCodePicker.Language.BENGALI -> R.raw.ccp_bengali
                CountryCodePicker.Language.CHINESE_SIMPLIFIED -> R.raw.ccp_chinese_simplified
                CountryCodePicker.Language.CHINESE_TRADITIONAL -> R.raw.ccp_chinese_traditional
                CountryCodePicker.Language.CZECH -> R.raw.ccp_czech
                CountryCodePicker.Language.DANISH -> R.raw.ccp_danish
                CountryCodePicker.Language.DUTCH -> R.raw.ccp_dutch
                CountryCodePicker.Language.ENGLISH -> R.raw.ccp_english
                CountryCodePicker.Language.FARSI -> R.raw.ccp_farsi
                CountryCodePicker.Language.FRENCH -> R.raw.ccp_french
                CountryCodePicker.Language.GERMAN -> R.raw.ccp_german
                CountryCodePicker.Language.GREEK -> R.raw.ccp_greek
                CountryCodePicker.Language.GUJARATI -> R.raw.ccp_gujarati
                CountryCodePicker.Language.HAUSA -> R.raw.ccp_hausa
                CountryCodePicker.Language.HEBREW -> R.raw.ccp_hebrew
                CountryCodePicker.Language.HINDI -> R.raw.ccp_hindi
                CountryCodePicker.Language.HUNGARIAN -> R.raw.ccp_hungarian
                CountryCodePicker.Language.INDONESIA -> R.raw.ccp_indonesia
                CountryCodePicker.Language.ITALIAN -> R.raw.ccp_italian
                CountryCodePicker.Language.JAPANESE -> R.raw.ccp_japanese
                CountryCodePicker.Language.KAZAKH -> R.raw.ccp_kazakh
                CountryCodePicker.Language.KOREAN -> R.raw.ccp_korean
                CountryCodePicker.Language.LITHUANIAN -> R.raw.ccp_lithuanian
                CountryCodePicker.Language.MARATHI -> R.raw.ccp_marathi
                CountryCodePicker.Language.POLISH -> R.raw.ccp_polish
                CountryCodePicker.Language.PORTUGUESE -> R.raw.ccp_portuguese
                CountryCodePicker.Language.PUNJABI -> R.raw.ccp_punjabi
                CountryCodePicker.Language.RUSSIAN -> R.raw.ccp_russian
                CountryCodePicker.Language.SERBIAN -> R.raw.ccp_serbian
                CountryCodePicker.Language.SLOVAK -> R.raw.ccp_slovak
                CountryCodePicker.Language.SLOVENIAN -> R.raw.ccp_slovenian
                CountryCodePicker.Language.SPANISH -> R.raw.ccp_spanish
                CountryCodePicker.Language.SWEDISH -> R.raw.ccp_swedish
                CountryCodePicker.Language.TAGALOG -> R.raw.ccp_tagalog
                CountryCodePicker.Language.TAMIL -> R.raw.ccp_tamil
                CountryCodePicker.Language.THAI -> R.raw.ccp_thai
                CountryCodePicker.Language.TURKISH -> R.raw.ccp_turkish
                CountryCodePicker.Language.UKRAINIAN -> R.raw.ccp_ukrainian
                CountryCodePicker.Language.URDU -> R.raw.ccp_urdu
                CountryCodePicker.Language.UZBEK -> R.raw.ccp_uzbek
                CountryCodePicker.Language.VIETNAMESE -> R.raw.ccp_vietnamese
            }
        }

        @JvmStatic
        fun getDialogTitle(context: Context, language: CountryCodePicker.Language): String? {
            if (loadedLibraryMasterListLanguage != language || dialogTitle.isNullOrEmpty()) {
                loadDataFromXML(context, language)
            }
            return dialogTitle
        }

        @JvmStatic
        fun getSearchHintMessage(context: Context, language: CountryCodePicker.Language): String? {
            if (loadedLibraryMasterListLanguage != language || searchHintMessage.isNullOrEmpty()) {
                loadDataFromXML(context, language)
            }
            return searchHintMessage
        }

        @JvmStatic
        fun getNoResultFoundAckMessage(context: Context, language: CountryCodePicker.Language): String? {
            if (loadedLibraryMasterListLanguage != language || noResultFoundAckMessage.isNullOrEmpty()) {
                loadDataFromXML(context, language)
            }
            return noResultFoundAckMessage
        }

        /**
         * Finds a country by its phone code.
         */
        fun getCountryForCode(
            context: Context,
            language: CountryCodePicker.Language,
            preferredCountries: MutableList<CCPCountry>?,
            code: String?
        ): CCPCountry? {
            preferredCountries?.find { it.phoneCode == code }?.let { return it }
            return getLibraryMasterCountryList(context, language).find { it.phoneCode == code }
        }

        @JvmStatic
        fun getCountryForCodeFromEnglishList(code: String?): CCPCountry? {
            return libraryMasterCountriesEnglish.find { it.phoneCode == code }
        }

        @JvmStatic
        fun getCustomMasterCountryList(
            context: Context,
            codePicker: CountryCodePicker
        ): MutableList<CCPCountry>? {
            codePicker.refreshCustomMasterList()
            return if (!codePicker.customMasterCountriesList.isNullOrEmpty()) {
                codePicker.customMasterCountriesList
            } else {
                codePicker.languageToApply?.let { getLibraryMasterCountryList(context, it) }
            }
        }

        fun getCountryForNameCodeFromCustomMasterList(
            context: Context,
            customMasterCountriesList: MutableList<CCPCountry>?,
            language: CountryCodePicker.Language,
            nameCode: String?
        ): CCPCountry? {
            if (customMasterCountriesList.isNullOrEmpty()) {
                return getCountryForNameCodeFromLibraryMasterList(context, language, nameCode)
            }
            return customMasterCountriesList.find { it.nameCode.equals(nameCode, true) }
        }

        @JvmStatic
        fun getCountryForNameCodeFromLibraryMasterList(
            context: Context,
            language: CountryCodePicker.Language,
            nameCode: String?
        ): CCPCountry? {
            return getLibraryMasterCountryList(context, language).find { it.nameCode.equals(nameCode, true) }
        }

        @JvmStatic
        fun getCountryForNameCodeFromEnglishList(nameCode: String?): CCPCountry? {
            return libraryMasterCountriesEnglish.find { it.nameCode.equals(nameCode, true) }
        }

        fun getCountryForCode(
            context: Context,
            language: CountryCodePicker.Language,
            preferredCountries: MutableList<CCPCountry>?,
            code: Int
        ): CCPCountry? = getCountryForCode(context, language, preferredCountries, code.toString())

        /**
         * Attempts to detect country from a full phone number.
         */
        fun getCountryForNumber(
            context: Context,
            language: CountryCodePicker.Language,
            preferredCountries: MutableList<CCPCountry>?,
            fullNumber: String?
        ): CCPCountry? {
            if (fullNumber.isNullOrBlank()) return null
            
            val trimmedNumber = fullNumber.trim()
            val startIdx = if (trimmedNumber.startsWith("+")) 1 else 0
            
            for (i in startIdx + 1..trimmedNumber.length.coerceAtMost(startIdx + 4)) {
                val code = trimmedNumber.substring(startIdx, i)
                val countryGroup = try {
                    CCPCountryGroup.getCountryGroupForPhoneCode(code.toInt())
                } catch (e: Exception) {
                    null
                }

                if (countryGroup != null) {
                    val areaCodeStartsAt = i
                    if (trimmedNumber.length >= areaCodeStartsAt + countryGroup.areaCodeLength) {
                        val areaCode = trimmedNumber.substring(areaCodeStartsAt, areaCodeStartsAt + countryGroup.areaCodeLength)
                        return countryGroup.getCountryForAreaCode(context, language, areaCode)
                    }
                    return getCountryForNameCodeFromLibraryMasterList(context, language, countryGroup.defaultNameCode)
                }

                getCountryForCode(context, language, preferredCountries, code)?.let { return it }
            }
            return null
        }

        fun getCountryForNumber(
            context: Context,
            language: CountryCodePicker.Language,
            fullNumber: String?
        ): CCPCountry? = getCountryForNumber(context, language, null, fullNumber)

        /**
         * Returns the flag resource ID for the given country.
         */
        fun getFlagMasterResID(country: CCPCountry): Int {
            val code = country.nameCode?.lowercase() ?: ""
            return flagResMap[code] ?: R.drawable.flag_transparent
        }

        /**
         * Returns the flag emoji for the given country.
         */
        @JvmStatic
        fun getFlagEmoji(country: CCPCountry): String {
            val code = country.nameCode?.lowercase() ?: ""
            return emojiMap[code] ?: " "
        }

        @JvmStatic
        fun getLibraryMasterCountryList(
            context: Context,
            language: CountryCodePicker.Language
        ): MutableList<CCPCountry> {
            if (loadedLibraryMasterListLanguage != language || loadedLibraryMaterList.isNullOrEmpty()) {
                loadDataFromXML(context, language)
            }
            return loadedLibraryMaterList!!
        }

        // Region: Large Data Maps
        
        private val flagResMap = mapOf(
            "ad" to R.drawable.flag_andorra, "ae" to R.drawable.flag_uae, "af" to R.drawable.flag_afghanistan,
            "ag" to R.drawable.flag_antigua_and_barbuda, "ai" to R.drawable.flag_anguilla, "al" to R.drawable.flag_albania,
            "am" to R.drawable.flag_armenia, "ao" to R.drawable.flag_angola, "aq" to R.drawable.flag_antarctica,
            "ar" to R.drawable.flag_argentina, "as" to R.drawable.flag_american_samoa, "at" to R.drawable.flag_austria,
            "au" to R.drawable.flag_australia, "aw" to R.drawable.flag_aruba, "ax" to R.drawable.flag_aland,
            "az" to R.drawable.flag_azerbaijan, "ba" to R.drawable.flag_bosnia, "bb" to R.drawable.flag_barbados,
            "bd" to R.drawable.flag_bangladesh, "be" to R.drawable.flag_belgium, "bf" to R.drawable.flag_burkina_faso,
            "bg" to R.drawable.flag_bulgaria, "bh" to R.drawable.flag_bahrain, "bi" to R.drawable.flag_burundi,
            "bj" to R.drawable.flag_benin, "bl" to R.drawable.flag_saint_barthelemy, "bm" to R.drawable.flag_bermuda,
            "bn" to R.drawable.flag_brunei, "bo" to R.drawable.flag_bolivia, "br" to R.drawable.flag_brazil,
            "bs" to R.drawable.flag_bahamas, "bt" to R.drawable.flag_bhutan, "bw" to R.drawable.flag_botswana,
            "by" to R.drawable.flag_belarus, "bz" to R.drawable.flag_belize, "ca" to R.drawable.flag_canada,
            "cc" to R.drawable.flag_cocos, "cd" to R.drawable.flag_democratic_republic_of_the_congo,
            "cf" to R.drawable.flag_central_african_republic, "cg" to R.drawable.flag_republic_of_the_congo,
            "ch" to R.drawable.flag_switzerland, "ci" to R.drawable.flag_cote_divoire, "ck" to R.drawable.flag_cook_islands,
            "cl" to R.drawable.flag_chile, "cm" to R.drawable.flag_cameroon, "cn" to R.drawable.flag_china,
            "co" to R.drawable.flag_colombia, "cr" to R.drawable.flag_costa_rica, "cu" to R.drawable.flag_cuba,
            "cv" to R.drawable.flag_cape_verde, "cw" to R.drawable.flag_curacao, "cx" to R.drawable.flag_christmas_island,
            "cy" to R.drawable.flag_cyprus, "cz" to R.drawable.flag_czech_republic, "de" to R.drawable.flag_germany,
            "dj" to R.drawable.flag_djibouti, "dk" to R.drawable.flag_denmark, "dm" to R.drawable.flag_dominica,
            "do" to R.drawable.flag_dominican_republic, "dz" to R.drawable.flag_algeria, "ec" to R.drawable.flag_ecuador,
            "ee" to R.drawable.flag_estonia, "eg" to R.drawable.flag_egypt, "er" to R.drawable.flag_eritrea,
            "es" to R.drawable.flag_spain, "et" to R.drawable.flag_ethiopia, "fi" to R.drawable.flag_finland,
            "fj" to R.drawable.flag_fiji, "fk" to R.drawable.flag_falkland_islands, "fm" to R.drawable.flag_micronesia,
            "fo" to R.drawable.flag_faroe_islands, "fr" to R.drawable.flag_france, "ga" to R.drawable.flag_gabon,
            "gb" to R.drawable.flag_united_kingdom, "gd" to R.drawable.flag_grenada, "ge" to R.drawable.flag_georgia,
            "gf" to R.drawable.flag_guyane, "gg" to R.drawable.flag_guernsey, "gh" to R.drawable.flag_ghana,
            "gi" to R.drawable.flag_gibraltar, "gl" to R.drawable.flag_greenland, "gm" to R.drawable.flag_gambia,
            "gn" to R.drawable.flag_guinea, "gp" to R.drawable.flag_guadeloupe, "gq" to R.drawable.flag_equatorial_guinea,
            "gr" to R.drawable.flag_greece, "gt" to R.drawable.flag_guatemala, "gu" to R.drawable.flag_guam,
            "gw" to R.drawable.flag_guinea_bissau, "gy" to R.drawable.flag_guyana, "hk" to R.drawable.flag_hong_kong,
            "hn" to R.drawable.flag_honduras, "hr" to R.drawable.flag_croatia, "ht" to R.drawable.flag_haiti,
            "hu" to R.drawable.flag_hungary, "id" to R.drawable.flag_indonesia, "ie" to R.drawable.flag_ireland,
            "il" to R.drawable.flag_israel, "im" to R.drawable.flag_isleof_man, "is" to R.drawable.flag_iceland,
            "in" to R.drawable.flag_india, "io" to R.drawable.flag_british_indian_ocean_territory, "iq" to R.drawable.flag_iraq_new,
            "ir" to R.drawable.flag_iran, "it" to R.drawable.flag_italy, "je" to R.drawable.flag_jersey,
            "jm" to R.drawable.flag_jamaica, "jo" to R.drawable.flag_jordan, "jp" to R.drawable.flag_japan,
            "ke" to R.drawable.flag_kenya, "kg" to R.drawable.flag_kyrgyzstan, "kh" to R.drawable.flag_cambodia,
            "ki" to R.drawable.flag_kiribati, "km" to R.drawable.flag_comoros, "kn" to R.drawable.flag_saint_kitts_and_nevis,
            "kp" to R.drawable.flag_north_korea, "kr" to R.drawable.flag_south_korea, "kw" to R.drawable.flag_kuwait,
            "ky" to R.drawable.flag_cayman_islands, "kz" to R.drawable.flag_kazakhstan, "la" to R.drawable.flag_laos,
            "lb" to R.drawable.flag_lebanon, "lc" to R.drawable.flag_saint_lucia, "li" to R.drawable.flag_liechtenstein,
            "lk" to R.drawable.flag_sri_lanka, "lr" to R.drawable.flag_liberia, "ls" to R.drawable.flag_lesotho,
            "lt" to R.drawable.flag_lithuania, "lu" to R.drawable.flag_luxembourg, "lv" to R.drawable.flag_latvia,
            "ly" to R.drawable.flag_libya, "ma" to R.drawable.flag_morocco, "mc" to R.drawable.flag_monaco,
            "md" to R.drawable.flag_moldova, "me" to R.drawable.flag_of_montenegro, "mf" to R.drawable.flag_saint_martin,
            "mg" to R.drawable.flag_madagascar, "mh" to R.drawable.flag_marshall_islands, "mk" to R.drawable.flag_macedonia,
            "ml" to R.drawable.flag_mali, "mm" to R.drawable.flag_myanmar, "mn" to R.drawable.flag_mongolia,
            "mo" to R.drawable.flag_macao, "mp" to R.drawable.flag_northern_mariana_islands, "mq" to R.drawable.flag_martinique,
            "mr" to R.drawable.flag_mauritania, "ms" to R.drawable.flag_montserrat, "mt" to R.drawable.flag_malta,
            "mu" to R.drawable.flag_mauritius, "mv" to R.drawable.flag_maldives, "mw" to R.drawable.flag_malawi,
            "mx" to R.drawable.flag_mexico, "my" to R.drawable.flag_malaysia, "mz" to R.drawable.flag_mozambique,
            "na" to R.drawable.flag_namibia, "nc" to R.drawable.flag_new_caledonia, "ne" to R.drawable.flag_niger,
            "nf" to R.drawable.flag_norfolk_island, "ng" to R.drawable.flag_nigeria, "ni" to R.drawable.flag_nicaragua,
            "nl" to R.drawable.flag_netherlands, "no" to R.drawable.flag_norway, "np" to R.drawable.flag_nepal,
            "nr" to R.drawable.flag_nauru, "nu" to R.drawable.flag_niue, "nz" to R.drawable.flag_new_zealand,
            "om" to R.drawable.flag_oman, "pa" to R.drawable.flag_panama, "pe" to R.drawable.flag_peru,
            "pf" to R.drawable.flag_french_polynesia, "pg" to R.drawable.flag_papua_new_guinea, "ph" to R.drawable.flag_philippines,
            "pk" to R.drawable.flag_pakistan, "pl" to R.drawable.flag_poland, "pm" to R.drawable.flag_saint_pierre,
            "pn" to R.drawable.flag_pitcairn_islands, "pr" to R.drawable.flag_puerto_rico, "ps" to R.drawable.flag_palestine,
            "pt" to R.drawable.flag_portugal, "pw" to R.drawable.flag_palau, "py" to R.drawable.flag_paraguay,
            "qa" to R.drawable.flag_qatar, "re" to R.drawable.flag_martinique, "ro" to R.drawable.flag_romania,
            "rs" to R.drawable.flag_serbia, "ru" to R.drawable.flag_russian_federation, "rw" to R.drawable.flag_rwanda,
            "sa" to R.drawable.flag_saudi_arabia, "sb" to R.drawable.flag_soloman_islands, "sc" to R.drawable.flag_seychelles,
            "sd" to R.drawable.flag_sudan, "se" to R.drawable.flag_sweden, "sg" to R.drawable.flag_singapore,
            "sh" to R.drawable.flag_saint_helena, "si" to R.drawable.flag_slovenia, "sk" to R.drawable.flag_slovakia,
            "sl" to R.drawable.flag_sierra_leone, "sm" to R.drawable.flag_san_marino, "sn" to R.drawable.flag_senegal,
            "so" to R.drawable.flag_somalia, "sr" to R.drawable.flag_suriname, "ss" to R.drawable.flag_south_sudan,
            "st" to R.drawable.flag_sao_tome_and_principe, "sv" to R.drawable.flag_el_salvador, "sx" to R.drawable.flag_sint_maarten,
            "sy" to R.drawable.flag_syria, "sz" to R.drawable.flag_swaziland, "tc" to R.drawable.flag_turks_and_caicos_islands,
            "td" to R.drawable.flag_chad, "tg" to R.drawable.flag_togo, "th" to R.drawable.flag_thailand,
            "tj" to R.drawable.flag_tajikistan, "tk" to R.drawable.flag_tokelau, "tl" to R.drawable.flag_timor_leste,
            "tm" to R.drawable.flag_turkmenistan, "tn" to R.drawable.flag_tunisia, "to" to R.drawable.flag_tonga,
            "tr" to R.drawable.flag_turkey, "tt" to R.drawable.flag_trinidad_and_tobago, "tv" to R.drawable.flag_tuvalu,
            "tw" to R.drawable.flag_taiwan, "tz" to R.drawable.flag_tanzania, "ua" to R.drawable.flag_ukraine,
            "ug" to R.drawable.flag_uganda, "us" to R.drawable.flag_united_states_of_america, "uy" to R.drawable.flag_uruguay,
            "uz" to R.drawable.flag_uzbekistan, "va" to R.drawable.flag_vatican_city, "vc" to R.drawable.flag_saint_vicent_and_the_grenadines,
            "ve" to R.drawable.flag_venezuela, "vg" to R.drawable.flag_british_virgin_islands, "vi" to R.drawable.flag_us_virgin_islands,
            "vn" to R.drawable.flag_vietnam, "vu" to R.drawable.flag_vanuatu, "wf" to R.drawable.flag_wallis_and_futuna,
            "ws" to R.drawable.flag_samoa, "xk" to R.drawable.flag_kosovo, "ye" to R.drawable.flag_yemen,
            "yt" to R.drawable.flag_martinique, "za" to R.drawable.flag_south_africa, "zm" to R.drawable.flag_zambia,
            "zw" to R.drawable.flag_zimbabwe
        )

        private val emojiMap = mapOf(
            "ad" to "🇦🇩", "ae" to "🇦🇪", "af" to "🇦🇫", "ag" to "🇦🇬", "ai" to "🇦🇮", "al" to "🇦🇱",
            "am" to "🇦🇲", "ao" to "🇦🇴", "aq" to "🇦🇶", "ar" to "🇦🇷", "as" to "🇦🇸", "at" to "🇦🇹",
            "au" to "🇦🇺", "aw" to "🇦🇼", "ax" to "🇦🇽", "az" to "🇦🇿", "ba" to "🇧🇦", "bb" to "🇧🇧",
            "bd" to "🇧🇩", "be" to "🇧🇪", "bf" to "🇧🇫", "bg" to "🇧🇬", "bh" to "🇧🇭", "bi" to "🇧🇮",
            "bj" to "🇧🇯", "bl" to "🇧🇱", "bm" to "🇧🇲", "bn" to "🇧🇳", "bo" to "🇧🇴", "bq" to "🇧🇶",
            "br" to "🇧🇷", "bs" to "🇧🇸", "bt" to "🇧🇹", "bv" to "🇧🇻", "bw" to "🇧🇼", "by" to "🇧🇾",
            "bz" to "🇧🇿", "ca" to "🇨🇦", "cc" to "🇨🇨", "cd" to "🇨🇩", "cf" to "🇨🇫", "cg" to "🇨🇬",
            "ch" to "🇨🇭", "ci" to "🇨🇮", "ck" to "🇨🇰", "cl" to "🇨🇱", "cm" to "🇨🇲", "cn" to "🇨🇳",
            "co" to "🇨🇴", "cr" to "🇨🇷", "cu" to "🇨🇺", "cv" to "🇨🇻", "cw" to "🇨🇼", "cx" to "🇨🇽",
            "cy" to "🇨🇾", "cz" to "🇨🇿", "de" to "🇩🇪", "dj" to "🇩🇯", "dk" to "🇩🇰", "dm" to "🇩🇲",
            "do" to "🇩🇴", "dz" to "🇩🇿", "ec" to "🇪🇨", "ee" to "🇪🇪", "eg" to "🇪🇬", "eh" to "🇪🇭",
            "er" to "🇪🇷", "es" to "🇪🇸", "et" to "🇪🇹", "fi" to "🇫🇮", "fj" to "🇫🇯", "fk" to "🇫🇰",
            "fm" to "🇫🇲", "fo" to "🇫🇴", "fr" to "🇫🇷", "ga" to "🇬🇦", "gb" to "🇬🇧", "gd" to "🇬🇩",
            "ge" to "🇬🇪", "gf" to "🇬🇫", "gg" to "🇬🇬", "gh" to "🇬🇭", "gi" to "🇬🇮", "gl" to "🇬🇱",
            "gm" to "🇬🇲", "gn" to "🇬🇳", "gp" to "🇬🇵", "gq" to "🇬🇶", "gr" to "🇬🇷", "gs" to "🇬🇸",
            "gt" to "🇬🇹", "gu" to "🇬🇺", "gw" to "🇬🇼", "gy" to "🇬🇾", "hk" to "🇭🇰", "hm" to "🇭🇲",
            "hn" to "🇭🇳", "hr" to "🇭🇷", "ht" to "🇭🇹", "hu" to "🇭🇺", "id" to "🇮🇩", "ie" to "🇮🇪",
            "il" to "🇮🇱", "im" to "🇮🇲", "in" to "🇮🇳", "io" to "🇮🇴", "iq" to "🇮🇶", "ir" to "🇮🇷",
            "is" to "🇮🇸", "it" to "🇮🇹", "je" to "🇯🇪", "jm" to "🇯🇲", "jo" to "🇯🇴", "jp" to "🇯🇵",
            "ke" to "🇰🇪", "kg" to "🇰🇬", "kh" to "🇰🇭", "ki" to "🇰🇮", "km" to "🇰🇲", "kn" to "🇰🇳",
            "kp" to "🇰🇵", "kr" to "🇰🇷", "kw" to "🇰🇼", "ky" to "🇰🇾", "kz" to "🇰🇿", "la" to "🇱🇦",
            "lb" to "🇱🇧", "lc" to "🇱🇨", "li" to "🇱🇮", "lk" to "🇱🇰", "lr" to "🇱🇷", "ls" to "🇱🇸",
            "lt" to "🇱🇹", "lu" to "🇱🇺", "lv" to "🇱🇻", "ly" to "🇱🇾", "ma" to "🇲🇦", "mc" to "🇲🇨",
            "md" to "🇲🇩", "me" to "🇲🇪", "mf" to "🇲🇫", "mg" to "🇲🇬", "mh" to "🇲🇭", "mk" to "🇲🇰",
            "ml" to "🇲🇱", "mm" to "🇲🇲", "mn" to "🇲🇳", "mo" to "🇲🇴", "mp" to "🇲🇵", "mq" to "🇲🇶",
            "mr" to "🇲🇷", "ms" to "🇲🇸", "mt" to "🇲🇹", "mu" to "🇲🇺", "mv" to "🇲🇻", "mw" to "🇲🇼",
            "mx" to "🇲🇽", "my" to "🇲🇾", "mz" to "🇲🇿", "na" to "🇳🇦", "nc" to "🇳🇨", "ne" to "🇳🇪",
            "nf" to "🇳🇫", "ng" to "🇳🇬", "ni" to "🇳🇮", "nl" to "🇳🇱", "no" to "🇳🇴", "np" to "🇳🇵",
            "nr" to "🇳🇷", "nu" to "🇳🇺", "nz" to "🇳🇿", "om" to "🇴🇲", "pa" to "🇵🇦", "pe" to "🇵🇪",
            "pf" to "🇵🇫", "pg" to "🇵🇬", "ph" to "🇵🇭", "pk" to "🇵🇰", "pl" to "🇵🇱", "pm" to "🇵🇲",
            "pn" to "🇵🇳", "pr" to "🇵🇷", "ps" to "🇵🇸", "pt" to "🇵🇹", "pw" to "🇵🇼", "py" to "🇵🇾",
            "qa" to "🇶🇦", "re" to "🇷🇪", "ro" to "🇷🇴", "rs" to "🇷🇸", "ru" to "🇷🇺", "rw" to "🇷🇼",
            "sa" to "🇸🇦", "sb" to "🇸🇧", "sc" to "🇸🇨", "sd" to "🇸🇩", "se" to "🇸🇪", "sg" to "🇸🇬",
            "sh" to "🇸🇭", "si" to "🇸🇮", "sj" to "🇸🇯", "sk" to "🇸🇰", "sl" to "🇸🇱", "sm" to "🇸🇲",
            "sn" to "🇸🇳", "so" to "🇸🇴", "sr" to "🇸🇷", "ss" to "🇸🇸", "st" to "🇸🇹", "sv" to "🇸🇻",
            "sx" to "🇸🇽", "sy" to "🇸🇾", "sz" to "🇸🇿", "tc" to "🇹🇨", "td" to "🇹🇩", "tf" to "🇹🇫",
            "tg" to "🇹🇬", "th" to "🇹🇭", "tj" to "🇹🇯", "tk" to "🇹🇰", "tl" to "🇹🇱", "tm" to "🇹🇲",
            "tn" to "🇹🇳", "to" to "🇹🇴", "tr" to "🇹🇷", "tt" to "🇹🇹", "tv" to "🇹🇻", "tw" to "🇹🇼",
            "tz" to "🇹🇿", "ua" to "🇺🇦", "ug" to "🇺🇬", "um" to "🇺🇲", "us" to "🇺🇸", "uy" to "🇺🇾",
            "uz" to "🇺🇿", "va" to "🇻🇦", "vc" to "🇻🇨", "ve" to "🇻🇪", "vg" to "🇻🇬", "vi" to "🇻🇮",
            "vn" to "🇻🇳", "vu" to "🇻🇺", "wf" to "🇼🇫", "ws" to "🇼🇸", "xk" to "🇽🇰", "ye" to "🇾🇪",
            "yt" to "🇾🇹", "za" to "🇿🇦", "zm" to "🇿🇲", "zw" to "🇿🇼"
        )

        val libraryMasterCountriesEnglish: MutableList<CCPCountry>
            get() = mutableListOf(
                CCPCountry("ad", "376", "Andorra", DEFAULT_FLAG_RES),
                CCPCountry("ae", "971", "United Arab Emirates (UAE)", DEFAULT_FLAG_RES),
                CCPCountry("af", "93", "Afghanistan", DEFAULT_FLAG_RES),
                CCPCountry("ag", "1", "Antigua and Barbuda", DEFAULT_FLAG_RES),
                CCPCountry("ai", "1", "Anguilla", DEFAULT_FLAG_RES),
                CCPCountry("al", "355", "Albania", DEFAULT_FLAG_RES),
                CCPCountry("am", "374", "Armenia", DEFAULT_FLAG_RES),
                CCPCountry("ao", "244", "Angola", DEFAULT_FLAG_RES),
                CCPCountry("aq", "672", "Antarctica", DEFAULT_FLAG_RES),
                CCPCountry("ar", "54", "Argentina", DEFAULT_FLAG_RES),
                CCPCountry("as", "1", "American Samoa", DEFAULT_FLAG_RES),
                CCPCountry("at", "43", "Austria", DEFAULT_FLAG_RES),
                CCPCountry("au", "61", "Australia", DEFAULT_FLAG_RES),
                CCPCountry("aw", "297", "Aruba", DEFAULT_FLAG_RES),
                CCPCountry("ax", "358", "Åland Islands", DEFAULT_FLAG_RES),
                CCPCountry("az", "994", "Azerbaijan", DEFAULT_FLAG_RES),
                CCPCountry("ba", "387", "Bosnia And Herzegovina", DEFAULT_FLAG_RES),
                CCPCountry("bb", "1", "Barbados", DEFAULT_FLAG_RES),
                CCPCountry("bd", "880", "Bangladesh", DEFAULT_FLAG_RES),
                CCPCountry("be", "32", "Belgium", DEFAULT_FLAG_RES),
                CCPCountry("bf", "226", "Burkina Faso", DEFAULT_FLAG_RES),
                CCPCountry("bg", "359", "Bulgaria", DEFAULT_FLAG_RES),
                CCPCountry("bh", "973", "Bahrain", DEFAULT_FLAG_RES),
                CCPCountry("bi", "257", "Burundi", DEFAULT_FLAG_RES),
                CCPCountry("bj", "229", "Benin", DEFAULT_FLAG_RES),
                CCPCountry("bl", "590", "Saint Barthélemy", DEFAULT_FLAG_RES),
                CCPCountry("bm", "1", "Bermuda", DEFAULT_FLAG_RES),
                CCPCountry("bn", "673", "Brunei Darussalam", DEFAULT_FLAG_RES),
                CCPCountry("bo", "591", "Bolivia, Plurinational State Of", DEFAULT_FLAG_RES),
                CCPCountry("br", "55", "Brazil", DEFAULT_FLAG_RES),
                CCPCountry("bs", "1", "Bahamas", DEFAULT_FLAG_RES),
                CCPCountry("bt", "975", "Bhutan", DEFAULT_FLAG_RES),
                CCPCountry("bw", "267", "Botswana", DEFAULT_FLAG_RES),
                CCPCountry("by", "375", "Belarus", DEFAULT_FLAG_RES),
                CCPCountry("bz", "501", "Belize", DEFAULT_FLAG_RES),
                CCPCountry("ca", "1", "Canada", DEFAULT_FLAG_RES),
                CCPCountry("cc", "61", "Cocos (keeling) Islands", DEFAULT_FLAG_RES),
                CCPCountry("cd", "243", "Congo, The Democratic Republic Of The", DEFAULT_FLAG_RES),
                CCPCountry("cf", "236", "Central African Republic", DEFAULT_FLAG_RES),
                CCPCountry("cg", "242", "Congo", DEFAULT_FLAG_RES),
                CCPCountry("ch", "41", "Switzerland", DEFAULT_FLAG_RES),
                CCPCountry("ci", "225", "Côte D'ivoire", DEFAULT_FLAG_RES),
                CCPCountry("ck", "682", "Cook Islands", DEFAULT_FLAG_RES),
                CCPCountry("cl", "56", "Chile", DEFAULT_FLAG_RES),
                CCPCountry("cm", "237", "Cameroon", DEFAULT_FLAG_RES),
                CCPCountry("cn", "86", "China", DEFAULT_FLAG_RES),
                CCPCountry("co", "57", "Colombia", DEFAULT_FLAG_RES),
                CCPCountry("cr", "506", "Costa Rica", DEFAULT_FLAG_RES),
                CCPCountry("cu", "53", "Cuba", DEFAULT_FLAG_RES),
                CCPCountry("cv", "238", "Cape Verde", DEFAULT_FLAG_RES),
                CCPCountry("cw", "599", "Curaçao", DEFAULT_FLAG_RES),
                CCPCountry("cx", "61", "Christmas Island", DEFAULT_FLAG_RES),
                CCPCountry("cy", "357", "Cyprus", DEFAULT_FLAG_RES),
                CCPCountry("cz", "420", "Czech Republic", DEFAULT_FLAG_RES),
                CCPCountry("de", "49", "Germany", DEFAULT_FLAG_RES),
                CCPCountry("dj", "253", "Djibouti", DEFAULT_FLAG_RES),
                CCPCountry("dk", "45", "Denmark", DEFAULT_FLAG_RES),
                CCPCountry("dm", "1", "Dominica", DEFAULT_FLAG_RES),
                CCPCountry("do", "1", "Dominican Republic", DEFAULT_FLAG_RES),
                CCPCountry("dz", "213", "Algeria", DEFAULT_FLAG_RES),
                CCPCountry("ec", "593", "Ecuador", DEFAULT_FLAG_RES),
                CCPCountry("ee", "372", "Estonia", DEFAULT_FLAG_RES),
                CCPCountry("eg", "20", "Egypt", DEFAULT_FLAG_RES),
                CCPCountry("er", "291", "Eritrea", DEFAULT_FLAG_RES),
                CCPCountry("es", "34", "Spain", DEFAULT_FLAG_RES),
                CCPCountry("et", "251", "Ethiopia", DEFAULT_FLAG_RES),
                CCPCountry("fi", "358", "Finland", DEFAULT_FLAG_RES),
                CCPCountry("fj", "679", "Fiji", DEFAULT_FLAG_RES),
                CCPCountry("fk", "500", "Falkland Islands (malvinas)", DEFAULT_FLAG_RES),
                CCPCountry("fm", "691", "Micronesia, Federated States Of", DEFAULT_FLAG_RES),
                CCPCountry("fo", "298", "Faroe Islands", DEFAULT_FLAG_RES),
                CCPCountry("fr", "33", "France", DEFAULT_FLAG_RES),
                CCPCountry("ga", "241", "Gabon", DEFAULT_FLAG_RES),
                CCPCountry("gb", "44", "United Kingdom", DEFAULT_FLAG_RES),
                CCPCountry("gd", "1", "Grenada", DEFAULT_FLAG_RES),
                CCPCountry("ge", "995", "Georgia", DEFAULT_FLAG_RES),
                CCPCountry("gf", "594", "French Guyana", DEFAULT_FLAG_RES),
                CCPCountry("gg", "44", "Guernsey", DEFAULT_FLAG_RES),
                CCPCountry("gh", "233", "Ghana", DEFAULT_FLAG_RES),
                CCPCountry("gi", "350", "Gibraltar", DEFAULT_FLAG_RES),
                CCPCountry("gl", "299", "Greenland", DEFAULT_FLAG_RES),
                CCPCountry("gm", "220", "Gambia", DEFAULT_FLAG_RES),
                CCPCountry("gn", "224", "Guinea", DEFAULT_FLAG_RES),
                CCPCountry("gp", "450", "Guadeloupe", DEFAULT_FLAG_RES),
                CCPCountry("gq", "240", "Equatorial Guinea", DEFAULT_FLAG_RES),
                CCPCountry("gr", "30", "Greece", DEFAULT_FLAG_RES),
                CCPCountry("gt", "502", "Guatemala", DEFAULT_FLAG_RES),
                CCPCountry("gu", "1", "Guam", DEFAULT_FLAG_RES),
                CCPCountry("gw", "245", "Guinea-bissau", DEFAULT_FLAG_RES),
                CCPCountry("gy", "592", "Guyana", DEFAULT_FLAG_RES),
                CCPCountry("hk", "852", "Hong Kong", DEFAULT_FLAG_RES),
                CCPCountry("hn", "504", "Honduras", DEFAULT_FLAG_RES),
                CCPCountry("hr", "385", "Croatia", DEFAULT_FLAG_RES),
                CCPCountry("ht", "509", "Haiti", DEFAULT_FLAG_RES),
                CCPCountry("hu", "36", "Hungary", DEFAULT_FLAG_RES),
                CCPCountry("id", "62", "Indonesia", DEFAULT_FLAG_RES),
                CCPCountry("ie", "353", "Ireland", DEFAULT_FLAG_RES),
                CCPCountry("il", "972", "Israel", DEFAULT_FLAG_RES),
                CCPCountry("im", "44", "Isle Of Man", DEFAULT_FLAG_RES),
                CCPCountry("is", "354", "Iceland", DEFAULT_FLAG_RES),
                CCPCountry("in", "91", "India", DEFAULT_FLAG_RES),
                CCPCountry("io", "246", "British Indian Ocean Territory", DEFAULT_FLAG_RES),
                CCPCountry("iq", "964", "Iraq", DEFAULT_FLAG_RES),
                CCPCountry("ir", "98", "Iran, Islamic Republic Of", DEFAULT_FLAG_RES),
                CCPCountry("it", "39", "Italy", DEFAULT_FLAG_RES),
                CCPCountry("je", "44", "Jersey", DEFAULT_FLAG_RES),
                CCPCountry("jm", "1", "Jamaica", DEFAULT_FLAG_RES),
                CCPCountry("jo", "962", "Jordan", DEFAULT_FLAG_RES),
                CCPCountry("jp", "81", "Japan", DEFAULT_FLAG_RES),
                CCPCountry("ke", "254", "Kenya", DEFAULT_FLAG_RES),
                CCPCountry("kg", "996", "Kyrgyzstan", DEFAULT_FLAG_RES),
                CCPCountry("kh", "855", "Cambodia", DEFAULT_FLAG_RES),
                CCPCountry("ki", "686", "Kiribati", DEFAULT_FLAG_RES),
                CCPCountry("km", "269", "Comoros", DEFAULT_FLAG_RES),
                CCPCountry("kn", "1", "Saint Kitts and Nevis", DEFAULT_FLAG_RES),
                CCPCountry("kp", "850", "North Korea", DEFAULT_FLAG_RES),
                CCPCountry("kr", "82", "South Korea", DEFAULT_FLAG_RES),
                CCPCountry("kw", "965", "Kuwait", DEFAULT_FLAG_RES),
                CCPCountry("ky", "1", "Cayman Islands", DEFAULT_FLAG_RES),
                CCPCountry("kz", "7", "Kazakhstan", DEFAULT_FLAG_RES),
                CCPCountry("la", "856", "Lao People's Democratic Republic", DEFAULT_FLAG_RES),
                CCPCountry("lb", "961", "Lebanon", DEFAULT_FLAG_RES),
                CCPCountry("lc", "1", "Saint Lucia", DEFAULT_FLAG_RES),
                CCPCountry("li", "423", "Liechtenstein", DEFAULT_FLAG_RES),
                CCPCountry("lk", "94", "Sri Lanka", DEFAULT_FLAG_RES),
                CCPCountry("lr", "231", "Liberia", DEFAULT_FLAG_RES),
                CCPCountry("ls", "266", "Lesotho", DEFAULT_FLAG_RES),
                CCPCountry("lt", "370", "Lithuania", DEFAULT_FLAG_RES),
                CCPCountry("lu", "352", "Luxembourg", DEFAULT_FLAG_RES),
                CCPCountry("lv", "371", "Latvia", DEFAULT_FLAG_RES),
                CCPCountry("ly", "218", "Libya", DEFAULT_FLAG_RES),
                CCPCountry("ma", "212", "Morocco", DEFAULT_FLAG_RES),
                CCPCountry("mc", "377", "Monaco", DEFAULT_FLAG_RES),
                CCPCountry("md", "373", "Moldova, Republic Of", DEFAULT_FLAG_RES),
                CCPCountry("me", "382", "Montenegro", DEFAULT_FLAG_RES),
                CCPCountry("mf", "590", "Saint Martin", DEFAULT_FLAG_RES),
                CCPCountry("mg", "261", "Madagascar", DEFAULT_FLAG_RES),
                CCPCountry("mh", "692", "Marshall Islands", DEFAULT_FLAG_RES),
                CCPCountry("mk", "389", "Macedonia (FYROM)", DEFAULT_FLAG_RES),
                CCPCountry("ml", "223", "Mali", DEFAULT_FLAG_RES),
                CCPCountry("mm", "95", "Myanmar", DEFAULT_FLAG_RES),
                CCPCountry("mn", "976", "Mongolia", DEFAULT_FLAG_RES),
                CCPCountry("mo", "853", "Macau", DEFAULT_FLAG_RES),
                CCPCountry("mp", "1", "Northern Mariana Islands", DEFAULT_FLAG_RES),
                CCPCountry("mq", "596", "Martinique", DEFAULT_FLAG_RES),
                CCPCountry("mr", "222", "Mauritania", DEFAULT_FLAG_RES),
                CCPCountry("ms", "1", "Montserrat", DEFAULT_FLAG_RES),
                CCPCountry("mt", "356", "Malta", DEFAULT_FLAG_RES),
                CCPCountry("mu", "230", "Mauritius", DEFAULT_FLAG_RES),
                CCPCountry("mv", "960", "Maldives", DEFAULT_FLAG_RES),
                CCPCountry("mw", "265", "Malawi", DEFAULT_FLAG_RES),
                CCPCountry("mx", "52", "Mexico", DEFAULT_FLAG_RES),
                CCPCountry("my", "60", "Malaysia", DEFAULT_FLAG_RES),
                CCPCountry("mz", "258", "Mozambique", DEFAULT_FLAG_RES),
                CCPCountry("na", "264", "Namibia", DEFAULT_FLAG_RES),
                CCPCountry("nc", "687", "New Caledonia", DEFAULT_FLAG_RES),
                CCPCountry("ne", "227", "Niger", DEFAULT_FLAG_RES),
                CCPCountry("nf", "672", "Norfolk Islands", DEFAULT_FLAG_RES),
                CCPCountry("ng", "234", "Nigeria", DEFAULT_FLAG_RES),
                CCPCountry("ni", "505", "Nicaragua", DEFAULT_FLAG_RES),
                CCPCountry("nl", "31", "Netherlands", DEFAULT_FLAG_RES),
                CCPCountry("no", "47", "Norway", DEFAULT_FLAG_RES),
                CCPCountry("np", "977", "Nepal", DEFAULT_FLAG_RES),
                CCPCountry("nr", "674", "Nauru", DEFAULT_FLAG_RES),
                CCPCountry("nu", "683", "Niue", DEFAULT_FLAG_RES),
                CCPCountry("nz", "64", "New Zealand", DEFAULT_FLAG_RES),
                CCPCountry("om", "968", "Oman", DEFAULT_FLAG_RES),
                CCPCountry("pa", "507", "Panama", DEFAULT_FLAG_RES),
                CCPCountry("pe", "51", "Peru", DEFAULT_FLAG_RES),
                CCPCountry("pf", "689", "French Polynesia", DEFAULT_FLAG_RES),
                CCPCountry("pg", "675", "Papua New Guinea", DEFAULT_FLAG_RES),
                CCPCountry("ph", "63", "Philippines", DEFAULT_FLAG_RES),
                CCPCountry("pk", "92", "Pakistan", DEFAULT_FLAG_RES),
                CCPCountry("pl", "48", "Poland", DEFAULT_FLAG_RES),
                CCPCountry("pm", "508", "Saint Pierre And Miquelon", DEFAULT_FLAG_RES),
                CCPCountry("pn", "870", "Pitcairn Islands", DEFAULT_FLAG_RES),
                CCPCountry("pr", "1", "Puerto Rico", DEFAULT_FLAG_RES),
                CCPCountry("ps", "970", "Palestine", DEFAULT_FLAG_RES),
                CCPCountry("pt", "351", "Portugal", DEFAULT_FLAG_RES),
                CCPCountry("pw", "680", "Palau", DEFAULT_FLAG_RES),
                CCPCountry("py", "595", "Paraguay", DEFAULT_FLAG_RES),
                CCPCountry("qa", "974", "Qatar", DEFAULT_FLAG_RES),
                CCPCountry("re", "262", "Réunion", DEFAULT_FLAG_RES),
                CCPCountry("ro", "40", "Romania", DEFAULT_FLAG_RES),
                CCPCountry("rs", "381", "Serbia", DEFAULT_FLAG_RES),
                CCPCountry("ru", "7", "Russian Federation", DEFAULT_FLAG_RES),
                CCPCountry("rw", "250", "Rwanda", DEFAULT_FLAG_RES),
                CCPCountry("sa", "966", "Saudi Arabia", DEFAULT_FLAG_RES),
                CCPCountry("sb", "677", "Solomon Islands", DEFAULT_FLAG_RES),
                CCPCountry("sc", "248", "Seychelles", DEFAULT_FLAG_RES),
                CCPCountry("sd", "249", "Sudan", DEFAULT_FLAG_RES),
                CCPCountry("se", "46", "Sweden", DEFAULT_FLAG_RES),
                CCPCountry("sg", "65", "Singapore", DEFAULT_FLAG_RES),
                CCPCountry("sh", "290", "Saint Helena, Ascension And Tristan Da Cunha", DEFAULT_FLAG_RES),
                CCPCountry("si", "386", "Slovenia", DEFAULT_FLAG_RES),
                CCPCountry("sk", "421", "Slovakia", DEFAULT_FLAG_RES),
                CCPCountry("sl", "232", "Sierra Leone", DEFAULT_FLAG_RES),
                CCPCountry("sm", "378", "San Marino", DEFAULT_FLAG_RES),
                CCPCountry("sn", "221", "Senegal", DEFAULT_FLAG_RES),
                CCPCountry("so", "252", "Somalia", DEFAULT_FLAG_RES),
                CCPCountry("sr", "597", "Suriname", DEFAULT_FLAG_RES),
                CCPCountry("ss", "211", "South Sudan", DEFAULT_FLAG_RES),
                CCPCountry("st", "239", "Sao Tome And Principe", DEFAULT_FLAG_RES),
                CCPCountry("sv", "503", "El Salvador", DEFAULT_FLAG_RES),
                CCPCountry("sx", "1", "Sint Maarten", DEFAULT_FLAG_RES),
                CCPCountry("sy", "963", "Syrian Arab Republic", DEFAULT_FLAG_RES),
                CCPCountry("sz", "268", "Swaziland", DEFAULT_FLAG_RES),
                CCPCountry("tc", "1", "Turks and Caicos Islands", DEFAULT_FLAG_RES),
                CCPCountry("td", "235", "Chad", DEFAULT_FLAG_RES),
                CCPCountry("tg", "228", "Togo", DEFAULT_FLAG_RES),
                CCPCountry("th", "66", "Thailand", DEFAULT_FLAG_RES),
                CCPCountry("tj", "992", "Tajikistan", DEFAULT_FLAG_RES),
                CCPCountry("tk", "690", "Tokelau", DEFAULT_FLAG_RES),
                CCPCountry("tl", "670", "Timor-leste", DEFAULT_FLAG_RES),
                CCPCountry("tm", "993", "Turkmenistan", DEFAULT_FLAG_RES),
                CCPCountry("tn", "216", "Tunisia", DEFAULT_FLAG_RES),
                CCPCountry("to", "676", "Tonga", DEFAULT_FLAG_RES),
                CCPCountry("tr", "90", "Turkey", DEFAULT_FLAG_RES),
                CCPCountry("tt", "1", "Trinidad \u0026amp; Tobago", DEFAULT_FLAG_RES),
                CCPCountry("tv", "688", "Tuvalu", DEFAULT_FLAG_RES),
                CCPCountry("tw", "886", "Taiwan", DEFAULT_FLAG_RES),
                CCPCountry("tz", "255", "Tanzania, United Republic Of", DEFAULT_FLAG_RES),
                CCPCountry("ua", "380", "Ukraine", DEFAULT_FLAG_RES),
                CCPCountry("ug", "256", "Uganda", DEFAULT_FLAG_RES),
                CCPCountry("us", "1", "United States", DEFAULT_FLAG_RES),
                CCPCountry("uy", "598", "Uruguay", DEFAULT_FLAG_RES),
                CCPCountry("uz", "998", "Uzbekistan", DEFAULT_FLAG_RES),
                CCPCountry("va", "379", "Holy See (vatican City State)", DEFAULT_FLAG_RES),
                CCPCountry("vc", "1", "Saint Vincent \u0026amp; The Grenadines", DEFAULT_FLAG_RES),
                CCPCountry("ve", "58", "Venezuela, Bolivarian Republic Of", DEFAULT_FLAG_RES),
                CCPCountry("vg", "1", "British Virgin Islands", DEFAULT_FLAG_RES),
                CCPCountry("vi", "1", "US Virgin Islands", DEFAULT_FLAG_RES),
                CCPCountry("vn", "84", "Vietnam", DEFAULT_FLAG_RES),
                CCPCountry("vu", "678", "Vanuatu", DEFAULT_FLAG_RES),
                CCPCountry("wf", "681", "Wallis And Futuna", DEFAULT_FLAG_RES),
                CCPCountry("ws", "685", "Samoa", DEFAULT_FLAG_RES),
                CCPCountry("xk", "383", "Kosovo", DEFAULT_FLAG_RES),
                CCPCountry("ye", "967", "Yemen", DEFAULT_FLAG_RES),
                CCPCountry("yt", "262", "Mayotte", DEFAULT_FLAG_RES),
                CCPCountry("za", "27", "South Africa", DEFAULT_FLAG_RES),
                CCPCountry("zm", "260", "Zambia", DEFAULT_FLAG_RES),
                CCPCountry("zw", "263", "Zimbabwe", DEFAULT_FLAG_RES)
            )
    }
}

package com.smcoding.countrycodepicker

import android.content.Context
import android.util.Log
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException
import org.xmlpull.v1.XmlPullParserFactory
import java.io.IOException
import java.text.Collator
import java.util.Collections
import java.util.Locale

class CCPCountry : Comparable<CCPCountry?> {
    @JvmField
    var nameCode: String? = null

    @JvmField
    var phoneCode: String? = null

    @JvmField
    var name: String? = null

    @JvmField
    var englishName: String? = null

    @JvmField
    var flagResID: Int = DEFAULT_FLAG_RES

    constructor()

    constructor(nameCode: String, phoneCode: String?, name: String?, flagResID: Int) {
        this.nameCode = nameCode.uppercase()
        this.phoneCode = phoneCode
        this.name = name
        this.flagResID = flagResID
    }

    val flagID: Int
        get() {
            if (flagResID == -99) {
                flagResID = getFlagMasterResID(this)
            }
            return flagResID
        }

    fun getNameCode(): String {
        return nameCode!!
    }

    fun setNameCode(nameCode: String) {
        this.nameCode = nameCode
    }

    fun log() {
        try {
            Log.d(TAG, "Country->$nameCode:$phoneCode:$name")
        } catch (ex: NullPointerException) {
            Log.d(TAG, "Null")
        }
    }

    fun logString(): String {
        return nameCode!!.uppercase() + " +" + phoneCode + "(" + name + ")"
    }

    /**
     * If country have query word in name or name code or phone code, this will return true.
     * 
     * @param query
     * @return
     */
    fun isEligibleForQuery(query: String): Boolean {
        var query = query
        query = query.lowercase(Locale.getDefault())
        return containsQueryWord("Name", this.name, query) ||
                containsQueryWord("NameCode", getNameCode(), query) ||
                containsQueryWord("PhoneCode", this.phoneCode, query) ||
                containsQueryWord("EnglishName", this.englishName, query)
    }

    private fun containsQueryWord(
        fieldName: String?,
        fieldValue: String?,
        query: String?
    ): Boolean {
        try {
            if (fieldValue == null || query == null) {
                return false
            } else {
                return fieldValue.lowercase().contains(query)
            }
        } catch (e: Exception) {
            Log.w(
                "CCPCountry", fieldName + ":" + fieldValue +
                        " failed to execute toLowerCase(Locale.ROOT).contains(query) " +
                        "for query:" + query
            )
            return false
        }
    }

    override fun compareTo(other: CCPCountry?): Int {
        return Collator.getInstance().compare(this.name, other?.name)
    }

    companion object {
        var DEFAULT_FLAG_RES: Int = -99
        var TAG: String = "Class Country"
        var loadedLibraryMasterListLanguage: CountryCodePicker.Language? = null
        var dialogTitle: String? = null
        var searchHintMessage: String? = null
        var noResultFoundAckMessage: String? = null
        var loadedLibraryMaterList: MutableList<CCPCountry>? = null

        //countries with +1
        private const val ANTIGUA_AND_BARBUDA_AREA_CODES = "268"
        private const val ANGUILLA_AREA_CODES = "264"
        private const val BARBADOS_AREA_CODES = "246"
        private const val BERMUDA_AREA_CODES = "441"
        private const val BAHAMAS_AREA_CODES = "242"
        private const val CANADA_AREA_CODES =
            "204/226/236/249/250/289/306/343/365/403/416/418/431/437/438/450/506/514/519/579/581/587/600/604/613/639/647/705/709/769/778/780/782/807/819/825/867/873/902/905/"
        private const val DOMINICA_AREA_CODES = "767"
        private const val DOMINICAN_REPUBLIC_AREA_CODES = "809/829/849"
        private const val GRENADA_AREA_CODES = "473"
        private const val JAMAICA_AREA_CODES = "876"
        private const val SAINT_KITTS_AND_NEVIS_AREA_CODES = "869"
        private const val CAYMAN_ISLANDS_AREA_CODES = "345"
        private const val SAINT_LUCIA_AREA_CODES = "758"
        private const val MONTSERRAT_AREA_CODES = "664"
        private const val PUERTO_RICO_AREA_CODES = "787"
        private const val SINT_MAARTEN_AREA_CODES = "721"
        private const val TURKS_AND_CAICOS_ISLANDS_AREA_CODES = "649"
        private const val TRINIDAD_AND_TOBAGO_AREA_CODES = "868"
        private const val SAINT_VINCENT_AND_THE_GRENADINES_AREA_CODES = "784"
        private const val BRITISH_VIRGIN_ISLANDS_AREA_CODES = "284"
        private const val US_VIRGIN_ISLANDS_AREA_CODES = "340"

        //countries with +44
        private const val ISLE_OF_MAN = "1624"

        /**
         * This function parses the raw/countries.xml file, and get list of all the countries.
         * 
         * @param context: required to access application resources (where country.xml is).
         * @return List of all the countries available in xml file.
         */
        fun loadDataFromXML(context: Context, language: CountryCodePicker.Language) {
            var countries: MutableList<CCPCountry> = ArrayList<CCPCountry>()
            var tempDialogTitle = ""
            var tempSearchHint = ""
            var tempNoResultAck = ""
            try {
                val xmlFactoryObject = XmlPullParserFactory.newInstance()
                val xmlPullParser = xmlFactoryObject.newPullParser()
                val ins = context.getResources().openRawResource(
                    context.getResources()
                        .getIdentifier(
                            "ccp_" + language.toString().lowercase(),
                            "raw", context.getPackageName()
                        )
                )
                xmlPullParser.setInput(ins, "UTF-8")
                var event = xmlPullParser.getEventType()
                while (event != XmlPullParser.END_DOCUMENT) {
                    val name = xmlPullParser.getName()
                    when (event) {
                        XmlPullParser.START_TAG -> {}
                        XmlPullParser.END_TAG -> if (name == "country") {
                            val ccpCountry = CCPCountry()
                            ccpCountry.setNameCode(
                                xmlPullParser.getAttributeValue(
                                    null,
                                    "name_code"
                                ).uppercase()
                            )
                            ccpCountry.phoneCode =
                                xmlPullParser.getAttributeValue(null, "phone_code")
                            ccpCountry.englishName =
                                xmlPullParser.getAttributeValue(null, "english_name")
                            ccpCountry.name = xmlPullParser.getAttributeValue(null, "name")
                            countries.add(ccpCountry)
                        } else if (name == "ccp_dialog_title") {
                            tempDialogTitle = xmlPullParser.getAttributeValue(null, "translation")
                        } else if (name == "ccp_dialog_search_hint_message") {
                            tempSearchHint = xmlPullParser.getAttributeValue(null, "translation")
                        } else if (name == "ccp_dialog_no_result_ack_message") {
                            tempNoResultAck = xmlPullParser.getAttributeValue(null, "translation")
                        }
                    }
                    event = xmlPullParser.next()
                }
                loadedLibraryMasterListLanguage = language
            } catch (e: XmlPullParserException) {
                e.printStackTrace()
            } catch (e: IOException) {
                e.printStackTrace()
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
            }

            //if anything went wrong, countries will be loaded for english language
            if (countries.size == 0) {
                loadedLibraryMasterListLanguage = CountryCodePicker.Language.ENGLISH
                countries = libraryMasterCountriesEnglish
            }

            dialogTitle = if (tempDialogTitle.length > 0) tempDialogTitle else "Select a country"
            searchHintMessage = if (tempSearchHint.length > 0) tempSearchHint else "Search..."
            noResultFoundAckMessage =
                if (tempNoResultAck.length > 0) tempNoResultAck else "Results not found"
            loadedLibraryMaterList = countries

            // sort list
            Collections.sort<CCPCountry?>(loadedLibraryMaterList)
        }

        @JvmStatic
        fun getDialogTitle(context: Context, language: CountryCodePicker.Language): String? {
            if (loadedLibraryMasterListLanguage == null || loadedLibraryMasterListLanguage != language || dialogTitle == null || dialogTitle!!.length == 0) {
                loadDataFromXML(context, language)
            }
            return dialogTitle
        }

        @JvmStatic
        fun getSearchHintMessage(context: Context, language: CountryCodePicker.Language): String? {
            if (loadedLibraryMasterListLanguage == null || loadedLibraryMasterListLanguage != language || searchHintMessage == null || searchHintMessage!!.length == 0) {
                loadDataFromXML(context, language)
            }
            return searchHintMessage
        }

        @JvmStatic
        fun getNoResultFoundAckMessage(
            context: Context,
            language: CountryCodePicker.Language
        ): String? {
            if (loadedLibraryMasterListLanguage == null || loadedLibraryMasterListLanguage != language || noResultFoundAckMessage == null || noResultFoundAckMessage!!.length == 0) {
                loadDataFromXML(context, language)
            }
            return noResultFoundAckMessage
        }

        fun setDialogTitle(dialogTitle: String?) {
            Companion.dialogTitle = dialogTitle
        }

        fun setSearchHintMessage(searchHintMessage: String?) {
            Companion.searchHintMessage = searchHintMessage
        }

        fun setNoResultFoundAckMessage(noResultFoundAckMessage: String?) {
            Companion.noResultFoundAckMessage = noResultFoundAckMessage
        }

        /**
         * Search a country which matches @param code.
         * 
         * @param context
         * @param preferredCountries is list of preference countries.
         * @param code               phone code. i.e "91" or "1"
         * @return Country that has phone code as @param code.
         * or returns null if no country matches given code.
         * if same code (e.g. +1) available for more than one country ( US, canada) , this function will return preferred country.
         */
        fun getCountryForCode(
            context: Context,
            language: CountryCodePicker.Language,
            preferredCountries: MutableList<CCPCountry>?,
            code: String?
        ): CCPCountry? {
            /**
             * check in preferred countries
             */

            if (preferredCountries != null && !preferredCountries.isEmpty()) {
                for (CCPCountry in preferredCountries) {
                    if (CCPCountry.phoneCode == code) {
                        return CCPCountry
                    }
                }
            }

            for (CCPCountry in getLibraryMasterCountryList(context, language)) {
                if (CCPCountry.phoneCode == code) {
                    return CCPCountry
                }
            }
            return null
        }

        /**
         * @param code phone code. i.e "91" or "1"
         * @return Country that has phone code as @param code.
         * or returns null if no country matches given code.
         * if same code (e.g. +1) available for more than one country ( US, canada) , this function will return preferred country.
         * @avoid Search a country which matches @param code. This method is just to support correct preview
         */
        @JvmStatic
        fun getCountryForCodeFromEnglishList(code: String?): CCPCountry? {
            val countries: MutableList<CCPCountry>?
            countries = libraryMasterCountriesEnglish

            for (ccpCountry in countries) {
                if (ccpCountry.phoneCode == code) {
                    return ccpCountry
                }
            }
            return null
        }

        @JvmStatic
        fun getCustomMasterCountryList(
            context: Context,
            codePicker: CountryCodePicker
        ): MutableList<CCPCountry>? {
            codePicker.refreshCustomMasterList()
            return if (codePicker.customMasterCountriesList != null && (codePicker.customMasterCountriesList as Collection<Any?>).isNotEmpty()) {
                codePicker.customMasterCountriesList
            } else {
                codePicker.getLanguageToApply()?.let { getLibraryMasterCountryList(context, it) }
            }
        }

        /**
         * Search a country which matches @param nameCode.
         *
         * @param context
         * @param customMasterCountriesList
         * @param nameCode                  country name code. i.e US or us or Au. See countries.xml for all code names.
         * @return Country that has country name code as @param code.
         */
        fun getCountryForNameCodeFromCustomMasterList(
            context: Context,
            customMasterCountriesList: MutableList<CCPCountry>?,
            language: CountryCodePicker.Language,
            nameCode: String?
        ): CCPCountry? {
            if (customMasterCountriesList == null || customMasterCountriesList.size == 0) {
                return getCountryForNameCodeFromLibraryMasterList(context, language, nameCode)
            } else {
                for (ccpCountry in customMasterCountriesList) {
                    if (ccpCountry.getNameCode().equals(nameCode, ignoreCase = true)) {
                        return ccpCountry
                    }
                }
            }
            return null
        }

        /**
         * Search a country which matches @param nameCode.
         * 
         * @param context
         * @param nameCode country name code. i.e US or us or Au. See countries.xml for all code names.
         * @return Country that has country name code as @param code.
         * or returns null if no country matches given code.
         */
        @JvmStatic
        fun getCountryForNameCodeFromLibraryMasterList(
            context: Context,
            language: CountryCodePicker.Language,
            nameCode: String?
        ): CCPCountry? {
            val countries: MutableList<CCPCountry>
            countries = getLibraryMasterCountryList(context, language)
            for (ccpCountry in countries) {
                if (ccpCountry.getNameCode().equals(nameCode, ignoreCase = true)) {
                    return ccpCountry
                }
            }
            return null
        }

        /**
         * Search a country which matches @param nameCode.
         * This searches through local english name list. This should be used only for the preview purpose.
         * 
         * @param nameCode country name code. i.e US or us or Au. See countries.xml for all code names.
         * @return Country that has country name code as @param code.
         * or returns null if no country matches given code.
         */
        @JvmStatic
        fun getCountryForNameCodeFromEnglishList(nameCode: String?): CCPCountry? {
            val countries: MutableList<CCPCountry>?
            countries = libraryMasterCountriesEnglish
            for (CCPCountry in countries) {
                if (CCPCountry.getNameCode().equals(nameCode, ignoreCase = true)) {
                    return CCPCountry
                }
            }
            return null
        }

        /**
         * Search a country which matches @param code.
         * 
         * @param context
         * @param preferredCountries list of country with priority,
         * @param code               phone code. i.e 91 or 1
         * @return Country that has phone code as @param code.
         * or returns null if no country matches given code.
         */
        fun getCountryForCode(
            context: Context,
            language: CountryCodePicker.Language,
            preferredCountries: MutableList<CCPCountry>?,
            code: Int
        ): CCPCountry? {
            return getCountryForCode(context, language, preferredCountries, code.toString() + "")
        }

        /**
         * Finds country code by matching substring from left to right from full number.
         * For example. if full number is +819017901357
         * function will ignore "+" and try to find match for first character "8"
         * if any country found for code "8", will return that country. If not, then it will
         * try to find country for "81". and so on till first 3 characters ( maximum number of characters in country code is 3).
         * 
         * @param context
         * @param preferredCountries countries of preference
         * @param fullNumber         full number ( "+" (optional)+ country code + carrier number) i.e. +819017901357 / 819017901357 / 918866667722
         * @return Country JP +81(Japan) for +819017901357 or 819017901357
         * Country IN +91(India) for  918866667722
         * null for 2956635321 ( as neither of "2", "29" and "295" matches any country code)
         */
        fun getCountryForNumber(
            context: Context,
            language: CountryCodePicker.Language,
            preferredCountries: MutableList<CCPCountry>?,
            fullNumber: String?
        ): CCPCountry? {
            var fullNumber = fullNumber
            val firstDigit: Int
            if (fullNumber == null) {
                return null
            } else {
                fullNumber = fullNumber.trim { it <= ' ' }
            }

            if (fullNumber.length != 0) {
                if (fullNumber.get(0) == '+') {
                    firstDigit = 1
                } else {
                    firstDigit = 0
                }
                var ccpCountry: CCPCountry? = null
                for (i in firstDigit..fullNumber.length) {
                    val code = fullNumber.substring(firstDigit, i)
                    var countryGroup: CCPCountryGroup? = null
                    try {
                        countryGroup = CCPCountryGroup.getCountryGroupForPhoneCode(code.toInt())
                    } catch (ignored: Exception) {
                    }
                    if (countryGroup != null) {
                        val areaCodeStartsAt = firstDigit + code.length
                        //when phone number covers area code too.
                        if (fullNumber.length >= areaCodeStartsAt + countryGroup.areaCodeLength) {
                            val areaCode = fullNumber.substring(
                                areaCodeStartsAt,
                                areaCodeStartsAt + countryGroup.areaCodeLength
                            )
                            return countryGroup.getCountryForAreaCode(context, language, areaCode)
                        } else {
                            return getCountryForNameCodeFromLibraryMasterList(
                                context,
                                language,
                                countryGroup.defaultNameCode
                            )
                        }
                    } else {
                        ccpCountry = getCountryForCode(context, language, preferredCountries, code)
                        if (ccpCountry != null) {
                            return ccpCountry
                        }
                    }
                }
            }
            //it reaches here means, phone number has some problem.
            return null
        }

        /**
         * Finds country code by matching substring from left to right from full number.
         * For example. if full number is +819017901357
         * function will ignore "+" and try to find match for first character "8"
         * if any country found for code "8", will return that country. If not, then it will
         * try to find country for "81". and so on till first 3 characters ( maximum number of characters in country code is 3).
         * 
         * @param context
         * @param fullNumber full number ( "+" (optional)+ country code + carrier number) i.e. +819017901357 / 819017901357 / 918866667722
         * @return Country JP +81(Japan) for +819017901357 or 819017901357
         * Country IN +91(India) for  918866667722
         * null for 2956635321 ( as neither of "2", "29" and "295" matches any country code)
         */
        fun getCountryForNumber(
            context: Context,
            language: CountryCodePicker.Language,
            fullNumber: String?
        ): CCPCountry? {
            return getCountryForNumber(context, language, null, fullNumber)
        }

        /**
         * Returns image res based on country name code
         * 
         * @param CCPCountry
         * @return
         */
        fun getFlagMasterResID(CCPCountry: CCPCountry): Int {
            when (CCPCountry.getNameCode().lowercase(Locale.getDefault())) {
                "ad" -> return R.drawable.flag_andorra
                "ae" -> return R.drawable.flag_uae
                "af" -> return R.drawable.flag_afghanistan
                "ag" -> return R.drawable.flag_antigua_and_barbuda
                "ai" -> return R.drawable.flag_anguilla
                "al" -> return R.drawable.flag_albania
                "am" -> return R.drawable.flag_armenia
                "ao" -> return R.drawable.flag_angola
                "aq" -> return R.drawable.flag_antarctica
                "ar" -> return R.drawable.flag_argentina
                "as" -> return R.drawable.flag_american_samoa
                "at" -> return R.drawable.flag_austria
                "au" -> return R.drawable.flag_australia
                "aw" -> return R.drawable.flag_aruba
                "ax" -> return R.drawable.flag_aland
                "az" -> return R.drawable.flag_azerbaijan
                "ba" -> return R.drawable.flag_bosnia
                "bb" -> return R.drawable.flag_barbados
                "bd" -> return R.drawable.flag_bangladesh
                "be" -> return R.drawable.flag_belgium
                "bf" -> return R.drawable.flag_burkina_faso
                "bg" -> return R.drawable.flag_bulgaria
                "bh" -> return R.drawable.flag_bahrain
                "bi" -> return R.drawable.flag_burundi
                "bj" -> return R.drawable.flag_benin
                "bl" -> return R.drawable.flag_saint_barthelemy // custom
                "bm" -> return R.drawable.flag_bermuda
                "bn" -> return R.drawable.flag_brunei
                "bo" -> return R.drawable.flag_bolivia
                "br" -> return R.drawable.flag_brazil
                "bs" -> return R.drawable.flag_bahamas
                "bt" -> return R.drawable.flag_bhutan
                "bw" -> return R.drawable.flag_botswana
                "by" -> return R.drawable.flag_belarus
                "bz" -> return R.drawable.flag_belize
                "ca" -> return R.drawable.flag_canada
                "cc" -> return R.drawable.flag_cocos // custom
                "cd" -> return R.drawable.flag_democratic_republic_of_the_congo
                "cf" -> return R.drawable.flag_central_african_republic
                "cg" -> return R.drawable.flag_republic_of_the_congo
                "ch" -> return R.drawable.flag_switzerland
                "ci" -> return R.drawable.flag_cote_divoire
                "ck" -> return R.drawable.flag_cook_islands
                "cl" -> return R.drawable.flag_chile
                "cm" -> return R.drawable.flag_cameroon
                "cn" -> return R.drawable.flag_china
                "co" -> return R.drawable.flag_colombia
                "cr" -> return R.drawable.flag_costa_rica
                "cu" -> return R.drawable.flag_cuba
                "cv" -> return R.drawable.flag_cape_verde
                "cw" -> return R.drawable.flag_curacao
                "cx" -> return R.drawable.flag_christmas_island
                "cy" -> return R.drawable.flag_cyprus
                "cz" -> return R.drawable.flag_czech_republic
                "de" -> return R.drawable.flag_germany
                "dj" -> return R.drawable.flag_djibouti
                "dk" -> return R.drawable.flag_denmark
                "dm" -> return R.drawable.flag_dominica
                "do" -> return R.drawable.flag_dominican_republic
                "dz" -> return R.drawable.flag_algeria
                "ec" -> return R.drawable.flag_ecuador
                "ee" -> return R.drawable.flag_estonia
                "eg" -> return R.drawable.flag_egypt
                "er" -> return R.drawable.flag_eritrea
                "es" -> return R.drawable.flag_spain
                "et" -> return R.drawable.flag_ethiopia
                "fi" -> return R.drawable.flag_finland
                "fj" -> return R.drawable.flag_fiji
                "fk" -> return R.drawable.flag_falkland_islands
                "fm" -> return R.drawable.flag_micronesia
                "fo" -> return R.drawable.flag_faroe_islands
                "fr" -> return R.drawable.flag_france
                "ga" -> return R.drawable.flag_gabon
                "gb" -> return R.drawable.flag_united_kingdom
                "gd" -> return R.drawable.flag_grenada
                "ge" -> return R.drawable.flag_georgia
                "gf" -> return R.drawable.flag_guyane
                "gg" -> return R.drawable.flag_guernsey
                "gh" -> return R.drawable.flag_ghana
                "gi" -> return R.drawable.flag_gibraltar
                "gl" -> return R.drawable.flag_greenland
                "gm" -> return R.drawable.flag_gambia
                "gn" -> return R.drawable.flag_guinea
                "gp" -> return R.drawable.flag_guadeloupe
                "gq" -> return R.drawable.flag_equatorial_guinea
                "gr" -> return R.drawable.flag_greece
                "gt" -> return R.drawable.flag_guatemala
                "gu" -> return R.drawable.flag_guam
                "gw" -> return R.drawable.flag_guinea_bissau
                "gy" -> return R.drawable.flag_guyana
                "hk" -> return R.drawable.flag_hong_kong
                "hn" -> return R.drawable.flag_honduras
                "hr" -> return R.drawable.flag_croatia
                "ht" -> return R.drawable.flag_haiti
                "hu" -> return R.drawable.flag_hungary
                "id" -> return R.drawable.flag_indonesia
                "ie" -> return R.drawable.flag_ireland
                "il" -> return R.drawable.flag_israel
                "im" -> return R.drawable.flag_isleof_man // custom
                "is" -> return R.drawable.flag_iceland
                "in" -> return R.drawable.flag_india
                "io" -> return R.drawable.flag_british_indian_ocean_territory
                "iq" -> return R.drawable.flag_iraq_new
                "ir" -> return R.drawable.flag_iran
                "it" -> return R.drawable.flag_italy
                "je" -> return R.drawable.flag_jersey
                "jm" -> return R.drawable.flag_jamaica
                "jo" -> return R.drawable.flag_jordan
                "jp" -> return R.drawable.flag_japan
                "ke" -> return R.drawable.flag_kenya
                "kg" -> return R.drawable.flag_kyrgyzstan
                "kh" -> return R.drawable.flag_cambodia
                "ki" -> return R.drawable.flag_kiribati
                "km" -> return R.drawable.flag_comoros
                "kn" -> return R.drawable.flag_saint_kitts_and_nevis
                "kp" -> return R.drawable.flag_north_korea
                "kr" -> return R.drawable.flag_south_korea
                "kw" -> return R.drawable.flag_kuwait
                "ky" -> return R.drawable.flag_cayman_islands
                "kz" -> return R.drawable.flag_kazakhstan
                "la" -> return R.drawable.flag_laos
                "lb" -> return R.drawable.flag_lebanon
                "lc" -> return R.drawable.flag_saint_lucia
                "li" -> return R.drawable.flag_liechtenstein
                "lk" -> return R.drawable.flag_sri_lanka
                "lr" -> return R.drawable.flag_liberia
                "ls" -> return R.drawable.flag_lesotho
                "lt" -> return R.drawable.flag_lithuania
                "lu" -> return R.drawable.flag_luxembourg
                "lv" -> return R.drawable.flag_latvia
                "ly" -> return R.drawable.flag_libya
                "ma" -> return R.drawable.flag_morocco
                "mc" -> return R.drawable.flag_monaco
                "md" -> return R.drawable.flag_moldova
                "me" -> return R.drawable.flag_of_montenegro // custom
                "mf" -> return R.drawable.flag_saint_martin
                "mg" -> return R.drawable.flag_madagascar
                "mh" -> return R.drawable.flag_marshall_islands
                "mk" -> return R.drawable.flag_macedonia
                "ml" -> return R.drawable.flag_mali
                "mm" -> return R.drawable.flag_myanmar
                "mn" -> return R.drawable.flag_mongolia
                "mo" -> return R.drawable.flag_macao
                "mp" -> return R.drawable.flag_northern_mariana_islands
                "mq" -> return R.drawable.flag_martinique
                "mr" -> return R.drawable.flag_mauritania
                "ms" -> return R.drawable.flag_montserrat
                "mt" -> return R.drawable.flag_malta
                "mu" -> return R.drawable.flag_mauritius
                "mv" -> return R.drawable.flag_maldives
                "mw" -> return R.drawable.flag_malawi
                "mx" -> return R.drawable.flag_mexico
                "my" -> return R.drawable.flag_malaysia
                "mz" -> return R.drawable.flag_mozambique
                "na" -> return R.drawable.flag_namibia
                "nc" -> return R.drawable.flag_new_caledonia // custom
                "ne" -> return R.drawable.flag_niger
                "nf" -> return R.drawable.flag_norfolk_island
                "ng" -> return R.drawable.flag_nigeria
                "ni" -> return R.drawable.flag_nicaragua
                "nl" -> return R.drawable.flag_netherlands
                "no" -> return R.drawable.flag_norway
                "np" -> return R.drawable.flag_nepal
                "nr" -> return R.drawable.flag_nauru
                "nu" -> return R.drawable.flag_niue
                "nz" -> return R.drawable.flag_new_zealand
                "om" -> return R.drawable.flag_oman
                "pa" -> return R.drawable.flag_panama
                "pe" -> return R.drawable.flag_peru
                "pf" -> return R.drawable.flag_french_polynesia
                "pg" -> return R.drawable.flag_papua_new_guinea
                "ph" -> return R.drawable.flag_philippines
                "pk" -> return R.drawable.flag_pakistan
                "pl" -> return R.drawable.flag_poland
                "pm" -> return R.drawable.flag_saint_pierre
                "pn" -> return R.drawable.flag_pitcairn_islands
                "pr" -> return R.drawable.flag_puerto_rico
                "ps" -> return R.drawable.flag_palestine
                "pt" -> return R.drawable.flag_portugal
                "pw" -> return R.drawable.flag_palau
                "py" -> return R.drawable.flag_paraguay
                "qa" -> return R.drawable.flag_qatar
                "re" -> return R.drawable.flag_martinique // no exact flag found
                "ro" -> return R.drawable.flag_romania
                "rs" -> return R.drawable.flag_serbia // custom
                "ru" -> return R.drawable.flag_russian_federation
                "rw" -> return R.drawable.flag_rwanda
                "sa" -> return R.drawable.flag_saudi_arabia
                "sb" -> return R.drawable.flag_soloman_islands
                "sc" -> return R.drawable.flag_seychelles
                "sd" -> return R.drawable.flag_sudan
                "se" -> return R.drawable.flag_sweden
                "sg" -> return R.drawable.flag_singapore
                "sh" -> return R.drawable.flag_saint_helena // custom
                "si" -> return R.drawable.flag_slovenia
                "sk" -> return R.drawable.flag_slovakia
                "sl" -> return R.drawable.flag_sierra_leone
                "sm" -> return R.drawable.flag_san_marino
                "sn" -> return R.drawable.flag_senegal
                "so" -> return R.drawable.flag_somalia
                "sr" -> return R.drawable.flag_suriname
                "ss" -> return R.drawable.flag_south_sudan
                "st" -> return R.drawable.flag_sao_tome_and_principe
                "sv" -> return R.drawable.flag_el_salvador
                "sx" -> return R.drawable.flag_sint_maarten
                "sy" -> return R.drawable.flag_syria
                "sz" -> return R.drawable.flag_swaziland
                "tc" -> return R.drawable.flag_turks_and_caicos_islands
                "td" -> return R.drawable.flag_chad
                "tg" -> return R.drawable.flag_togo
                "th" -> return R.drawable.flag_thailand
                "tj" -> return R.drawable.flag_tajikistan
                "tk" -> return R.drawable.flag_tokelau // custom
                "tl" -> return R.drawable.flag_timor_leste
                "tm" -> return R.drawable.flag_turkmenistan
                "tn" -> return R.drawable.flag_tunisia
                "to" -> return R.drawable.flag_tonga
                "tr" -> return R.drawable.flag_turkey
                "tt" -> return R.drawable.flag_trinidad_and_tobago
                "tv" -> return R.drawable.flag_tuvalu
                "tw" -> return R.drawable.flag_taiwan
                "tz" -> return R.drawable.flag_tanzania
                "ua" -> return R.drawable.flag_ukraine
                "ug" -> return R.drawable.flag_uganda
                "us" -> return R.drawable.flag_united_states_of_america
                "uy" -> return R.drawable.flag_uruguay
                "uz" -> return R.drawable.flag_uzbekistan
                "va" -> return R.drawable.flag_vatican_city
                "vc" -> return R.drawable.flag_saint_vicent_and_the_grenadines
                "ve" -> return R.drawable.flag_venezuela
                "vg" -> return R.drawable.flag_british_virgin_islands
                "vi" -> return R.drawable.flag_us_virgin_islands
                "vn" -> return R.drawable.flag_vietnam
                "vu" -> return R.drawable.flag_vanuatu
                "wf" -> return R.drawable.flag_wallis_and_futuna
                "ws" -> return R.drawable.flag_samoa
                "xk" -> return R.drawable.flag_kosovo
                "ye" -> return R.drawable.flag_yemen
                "yt" -> return R.drawable.flag_martinique // no exact flag found
                "za" -> return R.drawable.flag_south_africa
                "zm" -> return R.drawable.flag_zambia
                "zw" -> return R.drawable.flag_zimbabwe
                else -> return R.drawable.flag_transparent
            }
        }


        /**
         * Returns image res based on country name code
         * 
         * @param CCPCountry
         * @return
         */
        @JvmStatic
        fun getFlagEmoji(CCPCountry: CCPCountry): String {
            when (CCPCountry.getNameCode().lowercase(Locale.getDefault())) {
                "ad" -> return "🇦🇩"
                "ae" -> return "🇦🇪"
                "af" -> return "🇦🇫"
                "ag" -> return "🇦🇬"
                "ai" -> return "🇦🇮"
                "al" -> return "🇦🇱"
                "am" -> return "🇦🇲"
                "ao" -> return "🇦🇴"
                "aq" -> return "🇦🇶"
                "ar" -> return "🇦🇷"
                "as" -> return "🇦🇸"
                "at" -> return "🇦🇹"
                "au" -> return "🇦🇺"
                "aw" -> return "🇦🇼"
                "ax" -> return "🇦🇽"
                "az" -> return "🇦🇿"
                "ba" -> return "🇧🇦"
                "bb" -> return "🇧🇧"
                "bd" -> return "🇧🇩"
                "be" -> return "🇧🇪"
                "bf" -> return "🇧🇫"
                "bg" -> return "🇧🇬"
                "bh" -> return "🇧🇭"
                "bi" -> return "🇧🇮"
                "bj" -> return "🇧🇯"
                "bl" -> return "🇧🇱"
                "bm" -> return "🇧🇲"
                "bn" -> return "🇧🇳"
                "bo" -> return "🇧🇴"
                "bq" -> return "🇧🇶"
                "br" -> return "🇧🇷"
                "bs" -> return "🇧🇸"
                "bt" -> return "🇧🇹"
                "bv" -> return "🇧🇻"
                "bw" -> return "🇧🇼"
                "by" -> return "🇧🇾"
                "bz" -> return "🇧🇿"
                "ca" -> return "🇨🇦"
                "cc" -> return "🇨🇨"
                "cd" -> return "🇨🇩"
                "cf" -> return "🇨🇫"
                "cg" -> return "🇨🇬"
                "ch" -> return "🇨🇭"
                "ci" -> return "🇨🇮"
                "ck" -> return "🇨🇰"
                "cl" -> return "🇨🇱"
                "cm" -> return "🇨🇲"
                "cn" -> return "🇨🇳"
                "co" -> return "🇨🇴"
                "cr" -> return "🇨🇷"
                "cu" -> return "🇨🇺"
                "cv" -> return "🇨🇻"
                "cw" -> return "🇨🇼"
                "cx" -> return "🇨🇽"
                "cy" -> return "🇨🇾"
                "cz" -> return "🇨🇿"
                "de" -> return "🇩🇪"
                "dj" -> return "🇩🇯"
                "dk" -> return "🇩🇰"
                "dm" -> return "🇩🇲"
                "do" -> return "🇩🇴"
                "dz" -> return "🇩🇿"
                "ec" -> return "🇪🇨"
                "ee" -> return "🇪🇪"
                "eg" -> return "🇪🇬"
                "eh" -> return "🇪🇭"
                "er" -> return "🇪🇷"
                "es" -> return "🇪🇸"
                "et" -> return "🇪🇹"
                "fi" -> return "🇫🇮"
                "fj" -> return "🇫🇯"
                "fk" -> return "🇫🇰"
                "fm" -> return "🇫🇲"
                "fo" -> return "🇫🇴"
                "fr" -> return "🇫🇷"
                "ga" -> return "🇬🇦"
                "gb" -> return "🇬🇧"
                "gd" -> return "🇬🇩"
                "ge" -> return "🇬🇪"
                "gf" -> return "🇬🇫"
                "gg" -> return "🇬🇬"
                "gh" -> return "🇬🇭"
                "gi" -> return "🇬🇮"
                "gl" -> return "🇬🇱"
                "gm" -> return "🇬🇲"
                "gn" -> return "🇬🇳"
                "gp" -> return "🇬🇵"
                "gq" -> return "🇬🇶"
                "gr" -> return "🇬🇷"
                "gs" -> return "🇬🇸"
                "gt" -> return "🇬🇹"
                "gu" -> return "🇬🇺"
                "gw" -> return "🇬🇼"
                "gy" -> return "🇬🇾"
                "hk" -> return "🇭🇰"
                "hm" -> return "🇭🇲"
                "hn" -> return "🇭🇳"
                "hr" -> return "🇭🇷"
                "ht" -> return "🇭🇹"
                "hu" -> return "🇭🇺"
                "id" -> return "🇮🇩"
                "ie" -> return "🇮🇪"
                "il" -> return "🇮🇱"
                "im" -> return "🇮🇲"
                "in" -> return "🇮🇳"
                "io" -> return "🇮🇴"
                "iq" -> return "🇮🇶"
                "ir" -> return "🇮🇷"
                "is" -> return "🇮🇸"
                "it" -> return "🇮🇹"
                "je" -> return "🇯🇪"
                "jm" -> return "🇯🇲"
                "jo" -> return "🇯🇴"
                "jp" -> return "🇯🇵"
                "ke" -> return "🇰🇪"
                "kg" -> return "🇰🇬"
                "kh" -> return "🇰🇭"
                "ki" -> return "🇰🇮"
                "km" -> return "🇰🇲"
                "kn" -> return "🇰🇳"
                "kp" -> return "🇰🇵"
                "kr" -> return "🇰🇷"
                "kw" -> return "🇰🇼"
                "ky" -> return "🇰🇾"
                "kz" -> return "🇰🇿"
                "la" -> return "🇱🇦"
                "lb" -> return "🇱🇧"
                "lc" -> return "🇱🇨"
                "li" -> return "🇱🇮"
                "lk" -> return "🇱🇰"
                "lr" -> return "🇱🇷"
                "ls" -> return "🇱🇸"
                "lt" -> return "🇱🇹"
                "lu" -> return "🇱🇺"
                "lv" -> return "🇱🇻"
                "ly" -> return "🇱🇾"
                "ma" -> return "🇲🇦"
                "mc" -> return "🇲🇨"
                "md" -> return "🇲🇩"
                "me" -> return "🇲🇪"
                "mf" -> return "🇲🇫"
                "mg" -> return "🇲🇬"
                "mh" -> return "🇲🇭"
                "mk" -> return "🇲🇰"
                "ml" -> return "🇲🇱"
                "mm" -> return "🇲🇲"
                "mn" -> return "🇲🇳"
                "mo" -> return "🇲🇴"
                "mp" -> return "🇲🇵"
                "mq" -> return "🇲🇶"
                "mr" -> return "🇲🇷"
                "ms" -> return "🇲🇸"
                "mt" -> return "🇲🇹"
                "mu" -> return "🇲🇺"
                "mv" -> return "🇲🇻"
                "mw" -> return "🇲🇼"
                "mx" -> return "🇲🇽"
                "my" -> return "🇲🇾"
                "mz" -> return "🇲🇿"
                "na" -> return "🇳🇦"
                "nc" -> return "🇳🇨"
                "ne" -> return "🇳🇪"
                "nf" -> return "🇳🇫"
                "ng" -> return "🇳🇬"
                "ni" -> return "🇳🇮"
                "nl" -> return "🇳🇱"
                "no" -> return "🇳🇴"
                "np" -> return "🇳🇵"
                "nr" -> return "🇳🇷"
                "nu" -> return "🇳🇺"
                "nz" -> return "🇳🇿"
                "om" -> return "🇴🇲"
                "pa" -> return "🇵🇦"
                "pe" -> return "🇵🇪"
                "pf" -> return "🇵🇫"
                "pg" -> return "🇵🇬"
                "ph" -> return "🇵🇭"
                "pk" -> return "🇵🇰"
                "pl" -> return "🇵🇱"
                "pm" -> return "🇵🇲"
                "pn" -> return "🇵🇳"
                "pr" -> return "🇵🇷"
                "ps" -> return "🇵🇸"
                "pt" -> return "🇵🇹"
                "pw" -> return "🇵🇼"
                "py" -> return "🇵🇾"
                "qa" -> return "🇶🇦"
                "re" -> return "🇷🇪"
                "ro" -> return "🇷🇴"
                "rs" -> return "🇷🇸"
                "ru" -> return "🇷🇺"
                "rw" -> return "🇷🇼"
                "sa" -> return "🇸🇦"
                "sb" -> return "🇸🇧"
                "sc" -> return "🇸🇨"
                "sd" -> return "🇸🇩"
                "se" -> return "🇸🇪"
                "sg" -> return "🇸🇬"
                "sh" -> return "🇸🇭"
                "si" -> return "🇸🇮"
                "sj" -> return "🇸🇯"
                "sk" -> return "🇸🇰"
                "sl" -> return "🇸🇱"
                "sm" -> return "🇸🇲"
                "sn" -> return "🇸🇳"
                "so" -> return "🇸🇴"
                "sr" -> return "🇸🇷"
                "ss" -> return "🇸🇸"
                "st" -> return "🇸🇹"
                "sv" -> return "🇸🇻"
                "sx" -> return "🇸🇽"
                "sy" -> return "🇸🇾"
                "sz" -> return "🇸🇿"
                "tc" -> return "🇹🇨"
                "td" -> return "🇹🇩"
                "tf" -> return "🇹🇫"
                "tg" -> return "🇹🇬"
                "th" -> return "🇹🇭"
                "tj" -> return "🇹🇯"
                "tk" -> return "🇹🇰"
                "tl" -> return "🇹🇱"
                "tm" -> return "🇹🇲"
                "tn" -> return "🇹🇳"
                "to" -> return "🇹🇴"
                "tr" -> return "🇹🇷"
                "tt" -> return "🇹🇹"
                "tv" -> return "🇹🇻"
                "tw" -> return "🇹🇼"
                "tz" -> return "🇹🇿"
                "ua" -> return "🇺🇦"
                "ug" -> return "🇺🇬"
                "um" -> return "🇺🇲"
                "us" -> return "🇺🇸"
                "uy" -> return "🇺🇾"
                "uz" -> return "🇺🇿"
                "va" -> return "🇻🇦"
                "vc" -> return "🇻🇨"
                "ve" -> return "🇻🇪"
                "vg" -> return "🇻🇬"
                "vi" -> return "🇻🇮"
                "vn" -> return "🇻🇳"
                "vu" -> return "🇻🇺"
                "wf" -> return "🇼🇫"
                "ws" -> return "🇼🇸"
                "xk" -> return "🇽🇰"
                "ye" -> return "🇾🇪"
                "yt" -> return "🇾🇹"
                "za" -> return "🇿🇦"
                "zm" -> return "🇿🇲"
                "zw" -> return "🇿🇼"
                else -> return " "
            }
        }

        /**
         * This will return all the countries. No preference is manages.
         * Anytime new country need to be added, add it
         * 
         * @return
         */
        @JvmStatic
        fun getLibraryMasterCountryList(
            context: Context,
            language: CountryCodePicker.Language
        ): MutableList<CCPCountry> {
            if (loadedLibraryMasterListLanguage == null || language != loadedLibraryMasterListLanguage || loadedLibraryMaterList == null || loadedLibraryMaterList!!.size == 0) { //when it is required to load country in country list
                loadDataFromXML(context, language)
            }
            return loadedLibraryMaterList!!
        }

        val libraryMasterCountriesEnglish: MutableList<CCPCountry>
            get() {
                val countries: MutableList<CCPCountry> =
                    ArrayList<CCPCountry>()
                countries.add(
                    CCPCountry(
                        "ad",
                        "376",
                        "Andorra",
                        DEFAULT_FLAG_RES
                    )
                )
                countries.add(
                    CCPCountry(
                        "ae",
                        "971",
                        "United Arab Emirates (UAE)",
                        DEFAULT_FLAG_RES
                    )
                )
                countries.add(
                    CCPCountry(
                        "af",
                        "93",
                        "Afghanistan",
                        DEFAULT_FLAG_RES
                    )
                )
                countries.add(
                    CCPCountry(
                        "ag",
                        "1",
                        "Antigua and Barbuda",
                        DEFAULT_FLAG_RES
                    )
                )
                countries.add(
                    CCPCountry(
                        "ai",
                        "1",
                        "Anguilla",
                        DEFAULT_FLAG_RES
                    )
                )
                countries.add(
                    CCPCountry(
                        "al",
                        "355",
                        "Albania",
                        DEFAULT_FLAG_RES
                    )
                )
                countries.add(
                    CCPCountry(
                        "am",
                        "374",
                        "Armenia",
                        DEFAULT_FLAG_RES
                    )
                )
                countries.add(
                    CCPCountry(
                        "ao",
                        "244",
                        "Angola",
                        DEFAULT_FLAG_RES
                    )
                )
                countries.add(
                    CCPCountry(
                        "aq",
                        "672",
                        "Antarctica",
                        DEFAULT_FLAG_RES
                    )
                )
                countries.add(
                    CCPCountry(
                        "ar",
                        "54",
                        "Argentina",
                        DEFAULT_FLAG_RES
                    )
                )
                countries.add(
                    CCPCountry(
                        "as",
                        "1",
                        "American Samoa",
                        DEFAULT_FLAG_RES
                    )
                )
                countries.add(
                    CCPCountry(
                        "at",
                        "43",
                        "Austria",
                        DEFAULT_FLAG_RES
                    )
                )
                countries.add(
                    CCPCountry(
                        "au",
                        "61",
                        "Australia",
                        DEFAULT_FLAG_RES
                    )
                )
                countries.add(
                    CCPCountry(
                        "aw",
                        "297",
                        "Aruba",
                        DEFAULT_FLAG_RES
                    )
                )
                countries.add(
                    CCPCountry(
                        "ax",
                        "358",
                        "Åland Islands",
                        DEFAULT_FLAG_RES
                    )
                )
                countries.add(
                    CCPCountry(
                        "az",
                        "994",
                        "Azerbaijan",
                        DEFAULT_FLAG_RES
                    )
                )
                countries.add(
                    CCPCountry(
                        "ba",
                        "387",
                        "Bosnia And Herzegovina",
                        DEFAULT_FLAG_RES
                    )
                )
                countries.add(
                    CCPCountry(
                        "bb",
                        "1",
                        "Barbados",
                        DEFAULT_FLAG_RES
                    )
                )
                countries.add(
                    CCPCountry(
                        "bd",
                        "880",
                        "Bangladesh",
                        DEFAULT_FLAG_RES
                    )
                )
                countries.add(
                    CCPCountry(
                        "be",
                        "32",
                        "Belgium",
                        DEFAULT_FLAG_RES
                    )
                )
                countries.add(
                    CCPCountry(
                        "bf",
                        "226",
                        "Burkina Faso",
                        DEFAULT_FLAG_RES
                    )
                )
                countries.add(
                    CCPCountry(
                        "bg",
                        "359",
                        "Bulgaria",
                        DEFAULT_FLAG_RES
                    )
                )
                countries.add(
                    CCPCountry(
                        "bh",
                        "973",
                        "Bahrain",
                        DEFAULT_FLAG_RES
                    )
                )
                countries.add(
                    CCPCountry(
                        "bi",
                        "257",
                        "Burundi",
                        DEFAULT_FLAG_RES
                    )
                )
                countries.add(
                    CCPCountry(
                        "bj",
                        "229",
                        "Benin",
                        DEFAULT_FLAG_RES
                    )
                )
                countries.add(
                    CCPCountry(
                        "bl",
                        "590",
                        "Saint Barthélemy",
                        DEFAULT_FLAG_RES
                    )
                )
                countries.add(
                    CCPCountry(
                        "bm",
                        "1",
                        "Bermuda",
                        DEFAULT_FLAG_RES
                    )
                )
                countries.add(
                    CCPCountry(
                        "bn",
                        "673",
                        "Brunei Darussalam",
                        DEFAULT_FLAG_RES
                    )
                )
                countries.add(
                    CCPCountry(
                        "bo",
                        "591",
                        "Bolivia, Plurinational State Of",
                        DEFAULT_FLAG_RES
                    )
                )
                countries.add(
                    CCPCountry(
                        "br",
                        "55",
                        "Brazil",
                        DEFAULT_FLAG_RES
                    )
                )
                countries.add(
                    CCPCountry(
                        "bs",
                        "1",
                        "Bahamas",
                        DEFAULT_FLAG_RES
                    )
                )
                countries.add(
                    CCPCountry(
                        "bt",
                        "975",
                        "Bhutan",
                        DEFAULT_FLAG_RES
                    )
                )
                countries.add(
                    CCPCountry(
                        "bw",
                        "267",
                        "Botswana",
                        DEFAULT_FLAG_RES
                    )
                )
                countries.add(
                    CCPCountry(
                        "by",
                        "375",
                        "Belarus",
                        DEFAULT_FLAG_RES
                    )
                )
                countries.add(
                    CCPCountry(
                        "bz",
                        "501",
                        "Belize",
                        DEFAULT_FLAG_RES
                    )
                )
                countries.add(
                    CCPCountry(
                        "ca",
                        "1",
                        "Canada",
                        DEFAULT_FLAG_RES
                    )
                )
                countries.add(
                    CCPCountry(
                        "cc",
                        "61",
                        "Cocos (keeling) Islands",
                        DEFAULT_FLAG_RES
                    )
                )
                countries.add(
                    CCPCountry(
                        "cd",
                        "243",
                        "Congo, The Democratic Republic Of The",
                        DEFAULT_FLAG_RES
                    )
                )
                countries.add(
                    CCPCountry(
                        "cf",
                        "236",
                        "Central African Republic",
                        DEFAULT_FLAG_RES
                    )
                )
                countries.add(
                    CCPCountry(
                        "cg",
                        "242",
                        "Congo",
                        DEFAULT_FLAG_RES
                    )
                )
                countries.add(
                    CCPCountry(
                        "ch",
                        "41",
                        "Switzerland",
                        DEFAULT_FLAG_RES
                    )
                )
                countries.add(
                    CCPCountry(
                        "ci",
                        "225",
                        "Côte D'ivoire",
                        DEFAULT_FLAG_RES
                    )
                )
                countries.add(
                    CCPCountry(
                        "ck",
                        "682",
                        "Cook Islands",
                        DEFAULT_FLAG_RES
                    )
                )
                countries.add(
                    CCPCountry(
                        "cl",
                        "56",
                        "Chile",
                        DEFAULT_FLAG_RES
                    )
                )
                countries.add(
                    CCPCountry(
                        "cm",
                        "237",
                        "Cameroon",
                        DEFAULT_FLAG_RES
                    )
                )
                countries.add(
                    CCPCountry(
                        "cn",
                        "86",
                        "China",
                        DEFAULT_FLAG_RES
                    )
                )
                countries.add(
                    CCPCountry(
                        "co",
                        "57",
                        "Colombia",
                        DEFAULT_FLAG_RES
                    )
                )
                countries.add(
                    CCPCountry(
                        "cr",
                        "506",
                        "Costa Rica",
                        DEFAULT_FLAG_RES
                    )
                )
                countries.add(CCPCountry("cu", "53", "Cuba", DEFAULT_FLAG_RES))
                countries.add(
                    CCPCountry(
                        "cv",
                        "238",
                        "Cape Verde",
                        DEFAULT_FLAG_RES
                    )
                )
                countries.add(
                    CCPCountry(
                        "cw",
                        "599",
                        "Curaçao",
                        DEFAULT_FLAG_RES
                    )
                )
                countries.add(
                    CCPCountry(
                        "cx",
                        "61",
                        "Christmas Island",
                        DEFAULT_FLAG_RES
                    )
                )
                countries.add(
                    CCPCountry(
                        "cy",
                        "357",
                        "Cyprus",
                        DEFAULT_FLAG_RES
                    )
                )
                countries.add(
                    CCPCountry(
                        "cz",
                        "420",
                        "Czech Republic",
                        DEFAULT_FLAG_RES
                    )
                )
                countries.add(
                    CCPCountry(
                        "de",
                        "49",
                        "Germany",
                        DEFAULT_FLAG_RES
                    )
                )
                countries.add(
                    CCPCountry(
                        "dj",
                        "253",
                        "Djibouti",
                        DEFAULT_FLAG_RES
                    )
                )
                countries.add(
                    CCPCountry(
                        "dk",
                        "45",
                        "Denmark",
                        DEFAULT_FLAG_RES
                    )
                )
                countries.add(
                    CCPCountry(
                        "dm",
                        "1",
                        "Dominica",
                        DEFAULT_FLAG_RES
                    )
                )
                countries.add(
                    CCPCountry(
                        "do",
                        "1",
                        "Dominican Republic",
                        DEFAULT_FLAG_RES
                    )
                )
                countries.add(
                    CCPCountry(
                        "dz",
                        "213",
                        "Algeria",
                        DEFAULT_FLAG_RES
                    )
                )
                countries.add(
                    CCPCountry(
                        "ec",
                        "593",
                        "Ecuador",
                        DEFAULT_FLAG_RES
                    )
                )
                countries.add(
                    CCPCountry(
                        "ee",
                        "372",
                        "Estonia",
                        DEFAULT_FLAG_RES
                    )
                )
                countries.add(
                    CCPCountry(
                        "eg",
                        "20",
                        "Egypt",
                        DEFAULT_FLAG_RES
                    )
                )
                countries.add(
                    CCPCountry(
                        "er",
                        "291",
                        "Eritrea",
                        DEFAULT_FLAG_RES
                    )
                )
                countries.add(
                    CCPCountry(
                        "es",
                        "34",
                        "Spain",
                        DEFAULT_FLAG_RES
                    )
                )
                countries.add(
                    CCPCountry(
                        "et",
                        "251",
                        "Ethiopia",
                        DEFAULT_FLAG_RES
                    )
                )
                countries.add(
                    CCPCountry(
                        "fi",
                        "358",
                        "Finland",
                        DEFAULT_FLAG_RES
                    )
                )
                countries.add(
                    CCPCountry(
                        "fj",
                        "679",
                        "Fiji",
                        DEFAULT_FLAG_RES
                    )
                )
                countries.add(
                    CCPCountry(
                        "fk",
                        "500",
                        "Falkland Islands (malvinas)",
                        DEFAULT_FLAG_RES
                    )
                )
                countries.add(
                    CCPCountry(
                        "fm",
                        "691",
                        "Micronesia, Federated States Of",
                        DEFAULT_FLAG_RES
                    )
                )
                countries.add(
                    CCPCountry(
                        "fo",
                        "298",
                        "Faroe Islands",
                        DEFAULT_FLAG_RES
                    )
                )
                countries.add(
                    CCPCountry(
                        "fr",
                        "33",
                        "France",
                        DEFAULT_FLAG_RES
                    )
                )
                countries.add(
                    CCPCountry(
                        "ga",
                        "241",
                        "Gabon",
                        DEFAULT_FLAG_RES
                    )
                )
                countries.add(
                    CCPCountry(
                        "gb",
                        "44",
                        "United Kingdom",
                        DEFAULT_FLAG_RES
                    )
                )
                countries.add(
                    CCPCountry(
                        "gd",
                        "1",
                        "Grenada",
                        DEFAULT_FLAG_RES
                    )
                )
                countries.add(
                    CCPCountry(
                        "ge",
                        "995",
                        "Georgia",
                        DEFAULT_FLAG_RES
                    )
                )
                countries.add(
                    CCPCountry(
                        "gf",
                        "594",
                        "French Guyana",
                        DEFAULT_FLAG_RES
                    )
                )
                countries.add(
                    CCPCountry(
                        "gh",
                        "233",
                        "Ghana",
                        DEFAULT_FLAG_RES
                    )
                )
                countries.add(
                    CCPCountry(
                        "gi",
                        "350",
                        "Gibraltar",
                        DEFAULT_FLAG_RES
                    )
                )
                countries.add(
                    CCPCountry(
                        "gl",
                        "299",
                        "Greenland",
                        DEFAULT_FLAG_RES
                    )
                )
                countries.add(
                    CCPCountry(
                        "gm",
                        "220",
                        "Gambia",
                        DEFAULT_FLAG_RES
                    )
                )
                countries.add(
                    CCPCountry(
                        "gn",
                        "224",
                        "Guinea",
                        DEFAULT_FLAG_RES
                    )
                )
                countries.add(
                    CCPCountry(
                        "gp",
                        "450",
                        "Guadeloupe",
                        DEFAULT_FLAG_RES
                    )
                )
                countries.add(
                    CCPCountry(
                        "gq",
                        "240",
                        "Equatorial Guinea",
                        DEFAULT_FLAG_RES
                    )
                )
                countries.add(
                    CCPCountry(
                        "gr",
                        "30",
                        "Greece",
                        DEFAULT_FLAG_RES
                    )
                )
                countries.add(
                    CCPCountry(
                        "gt",
                        "502",
                        "Guatemala",
                        DEFAULT_FLAG_RES
                    )
                )
                countries.add(CCPCountry("gu", "1", "Guam", DEFAULT_FLAG_RES))
                countries.add(
                    CCPCountry(
                        "gw",
                        "245",
                        "Guinea-bissau",
                        DEFAULT_FLAG_RES
                    )
                )
                countries.add(
                    CCPCountry(
                        "gy",
                        "592",
                        "Guyana",
                        DEFAULT_FLAG_RES
                    )
                )
                countries.add(
                    CCPCountry(
                        "hk",
                        "852",
                        "Hong Kong",
                        DEFAULT_FLAG_RES
                    )
                )
                countries.add(
                    CCPCountry(
                        "hn",
                        "504",
                        "Honduras",
                        DEFAULT_FLAG_RES
                    )
                )
                countries.add(
                    CCPCountry(
                        "hr",
                        "385",
                        "Croatia",
                        DEFAULT_FLAG_RES
                    )
                )
                countries.add(
                    CCPCountry(
                        "ht",
                        "509",
                        "Haiti",
                        DEFAULT_FLAG_RES
                    )
                )
                countries.add(
                    CCPCountry(
                        "hu",
                        "36",
                        "Hungary",
                        DEFAULT_FLAG_RES
                    )
                )
                countries.add(
                    CCPCountry(
                        "id",
                        "62",
                        "Indonesia",
                        DEFAULT_FLAG_RES
                    )
                )
                countries.add(
                    CCPCountry(
                        "ie",
                        "353",
                        "Ireland",
                        DEFAULT_FLAG_RES
                    )
                )
                countries.add(
                    CCPCountry(
                        "il",
                        "972",
                        "Israel",
                        DEFAULT_FLAG_RES
                    )
                )
                countries.add(
                    CCPCountry(
                        "im",
                        "44",
                        "Isle Of Man",
                        DEFAULT_FLAG_RES
                    )
                )
                countries.add(
                    CCPCountry(
                        "is",
                        "354",
                        "Iceland",
                        DEFAULT_FLAG_RES
                    )
                )
                countries.add(
                    CCPCountry(
                        "in",
                        "91",
                        "India",
                        DEFAULT_FLAG_RES
                    )
                )
                countries.add(
                    CCPCountry(
                        "io",
                        "246",
                        "British Indian Ocean Territory",
                        DEFAULT_FLAG_RES
                    )
                )
                countries.add(
                    CCPCountry(
                        "iq",
                        "964",
                        "Iraq",
                        DEFAULT_FLAG_RES
                    )
                )
                countries.add(
                    CCPCountry(
                        "ir",
                        "98",
                        "Iran, Islamic Republic Of",
                        DEFAULT_FLAG_RES
                    )
                )
                countries.add(
                    CCPCountry(
                        "it",
                        "39",
                        "Italy",
                        DEFAULT_FLAG_RES
                    )
                )
                countries.add(
                    CCPCountry(
                        "je",
                        "44",
                        "Jersey ",
                        DEFAULT_FLAG_RES
                    )
                )
                countries.add(
                    CCPCountry(
                        "jm",
                        "1",
                        "Jamaica",
                        DEFAULT_FLAG_RES
                    )
                )
                countries.add(
                    CCPCountry(
                        "jo",
                        "962",
                        "Jordan",
                        DEFAULT_FLAG_RES
                    )
                )
                countries.add(
                    CCPCountry(
                        "jp",
                        "81",
                        "Japan",
                        DEFAULT_FLAG_RES
                    )
                )
                countries.add(
                    CCPCountry(
                        "ke",
                        "254",
                        "Kenya",
                        DEFAULT_FLAG_RES
                    )
                )
                countries.add(
                    CCPCountry(
                        "kg",
                        "996",
                        "Kyrgyzstan",
                        DEFAULT_FLAG_RES
                    )
                )
                countries.add(
                    CCPCountry(
                        "kh",
                        "855",
                        "Cambodia",
                        DEFAULT_FLAG_RES
                    )
                )
                countries.add(
                    CCPCountry(
                        "ki",
                        "686",
                        "Kiribati",
                        DEFAULT_FLAG_RES
                    )
                )
                countries.add(
                    CCPCountry(
                        "km",
                        "269",
                        "Comoros",
                        DEFAULT_FLAG_RES
                    )
                )
                countries.add(
                    CCPCountry(
                        "kn",
                        "1",
                        "Saint Kitts and Nevis",
                        DEFAULT_FLAG_RES
                    )
                )
                countries.add(
                    CCPCountry(
                        "kp",
                        "850",
                        "North Korea",
                        DEFAULT_FLAG_RES
                    )
                )
                countries.add(
                    CCPCountry(
                        "kr",
                        "82",
                        "South Korea",
                        DEFAULT_FLAG_RES
                    )
                )
                countries.add(
                    CCPCountry(
                        "kw",
                        "965",
                        "Kuwait",
                        DEFAULT_FLAG_RES
                    )
                )
                countries.add(
                    CCPCountry(
                        "ky",
                        "1",
                        "Cayman Islands",
                        DEFAULT_FLAG_RES
                    )
                )
                countries.add(
                    CCPCountry(
                        "kz",
                        "7",
                        "Kazakhstan",
                        DEFAULT_FLAG_RES
                    )
                )
                countries.add(
                    CCPCountry(
                        "la",
                        "856",
                        "Lao People's Democratic Republic",
                        DEFAULT_FLAG_RES
                    )
                )
                countries.add(
                    CCPCountry(
                        "lb",
                        "961",
                        "Lebanon",
                        DEFAULT_FLAG_RES
                    )
                )
                countries.add(
                    CCPCountry(
                        "lc",
                        "1",
                        "Saint Lucia",
                        DEFAULT_FLAG_RES
                    )
                )
                countries.add(
                    CCPCountry(
                        "li",
                        "423",
                        "Liechtenstein",
                        DEFAULT_FLAG_RES
                    )
                )
                countries.add(
                    CCPCountry(
                        "lk",
                        "94",
                        "Sri Lanka",
                        DEFAULT_FLAG_RES
                    )
                )
                countries.add(
                    CCPCountry(
                        "lr",
                        "231",
                        "Liberia",
                        DEFAULT_FLAG_RES
                    )
                )
                countries.add(
                    CCPCountry(
                        "ls",
                        "266",
                        "Lesotho",
                        DEFAULT_FLAG_RES
                    )
                )
                countries.add(
                    CCPCountry(
                        "lt",
                        "370",
                        "Lithuania",
                        DEFAULT_FLAG_RES
                    )
                )
                countries.add(
                    CCPCountry(
                        "lu",
                        "352",
                        "Luxembourg",
                        DEFAULT_FLAG_RES
                    )
                )
                countries.add(
                    CCPCountry(
                        "lv",
                        "371",
                        "Latvia",
                        DEFAULT_FLAG_RES
                    )
                )
                countries.add(
                    CCPCountry(
                        "ly",
                        "218",
                        "Libya",
                        DEFAULT_FLAG_RES
                    )
                )
                countries.add(
                    CCPCountry(
                        "ma",
                        "212",
                        "Morocco",
                        DEFAULT_FLAG_RES
                    )
                )
                countries.add(
                    CCPCountry(
                        "mc",
                        "377",
                        "Monaco",
                        DEFAULT_FLAG_RES
                    )
                )
                countries.add(
                    CCPCountry(
                        "md",
                        "373",
                        "Moldova, Republic Of",
                        DEFAULT_FLAG_RES
                    )
                )
                countries.add(
                    CCPCountry(
                        "me",
                        "382",
                        "Montenegro",
                        DEFAULT_FLAG_RES
                    )
                )
                countries.add(
                    CCPCountry(
                        "mf",
                        "590",
                        "Saint Martin",
                        DEFAULT_FLAG_RES
                    )
                )
                countries.add(
                    CCPCountry(
                        "mg",
                        "261",
                        "Madagascar",
                        DEFAULT_FLAG_RES
                    )
                )
                countries.add(
                    CCPCountry(
                        "mh",
                        "692",
                        "Marshall Islands",
                        DEFAULT_FLAG_RES
                    )
                )
                countries.add(
                    CCPCountry(
                        "mk",
                        "389",
                        "Macedonia (FYROM)",
                        DEFAULT_FLAG_RES
                    )
                )
                countries.add(
                    CCPCountry(
                        "ml",
                        "223",
                        "Mali",
                        DEFAULT_FLAG_RES
                    )
                )
                countries.add(
                    CCPCountry(
                        "mm",
                        "95",
                        "Myanmar",
                        DEFAULT_FLAG_RES
                    )
                )
                countries.add(
                    CCPCountry(
                        "mn",
                        "976",
                        "Mongolia",
                        DEFAULT_FLAG_RES
                    )
                )
                countries.add(
                    CCPCountry(
                        "mo",
                        "853",
                        "Macau",
                        DEFAULT_FLAG_RES
                    )
                )
                countries.add(
                    CCPCountry(
                        "mp",
                        "1",
                        "Northern Mariana Islands",
                        DEFAULT_FLAG_RES
                    )
                )
                countries.add(
                    CCPCountry(
                        "mq",
                        "596",
                        "Martinique",
                        DEFAULT_FLAG_RES
                    )
                )
                countries.add(
                    CCPCountry(
                        "mr",
                        "222",
                        "Mauritania",
                        DEFAULT_FLAG_RES
                    )
                )
                countries.add(
                    CCPCountry(
                        "ms",
                        "1",
                        "Montserrat",
                        DEFAULT_FLAG_RES
                    )
                )
                countries.add(
                    CCPCountry(
                        "mt",
                        "356",
                        "Malta",
                        DEFAULT_FLAG_RES
                    )
                )
                countries.add(
                    CCPCountry(
                        "mu",
                        "230",
                        "Mauritius",
                        DEFAULT_FLAG_RES
                    )
                )
                countries.add(
                    CCPCountry(
                        "mv",
                        "960",
                        "Maldives",
                        DEFAULT_FLAG_RES
                    )
                )
                countries.add(
                    CCPCountry(
                        "mw",
                        "265",
                        "Malawi",
                        DEFAULT_FLAG_RES
                    )
                )
                countries.add(
                    CCPCountry(
                        "mx",
                        "52",
                        "Mexico",
                        DEFAULT_FLAG_RES
                    )
                )
                countries.add(
                    CCPCountry(
                        "my",
                        "60",
                        "Malaysia",
                        DEFAULT_FLAG_RES
                    )
                )
                countries.add(
                    CCPCountry(
                        "mz",
                        "258",
                        "Mozambique",
                        DEFAULT_FLAG_RES
                    )
                )
                countries.add(
                    CCPCountry(
                        "na",
                        "264",
                        "Namibia",
                        DEFAULT_FLAG_RES
                    )
                )
                countries.add(
                    CCPCountry(
                        "nc",
                        "687",
                        "New Caledonia",
                        DEFAULT_FLAG_RES
                    )
                )
                countries.add(
                    CCPCountry(
                        "ne",
                        "227",
                        "Niger",
                        DEFAULT_FLAG_RES
                    )
                )
                countries.add(
                    CCPCountry(
                        "nf",
                        "672",
                        "Norfolk Islands",
                        DEFAULT_FLAG_RES
                    )
                )
                countries.add(
                    CCPCountry(
                        "ng",
                        "234",
                        "Nigeria",
                        DEFAULT_FLAG_RES
                    )
                )
                countries.add(
                    CCPCountry(
                        "ni",
                        "505",
                        "Nicaragua",
                        DEFAULT_FLAG_RES
                    )
                )
                countries.add(
                    CCPCountry(
                        "nl",
                        "31",
                        "Netherlands",
                        DEFAULT_FLAG_RES
                    )
                )
                countries.add(
                    CCPCountry(
                        "no",
                        "47",
                        "Norway",
                        DEFAULT_FLAG_RES
                    )
                )
                countries.add(
                    CCPCountry(
                        "np",
                        "977",
                        "Nepal",
                        DEFAULT_FLAG_RES
                    )
                )
                countries.add(
                    CCPCountry(
                        "nr",
                        "674",
                        "Nauru",
                        DEFAULT_FLAG_RES
                    )
                )
                countries.add(
                    CCPCountry(
                        "nu",
                        "683",
                        "Niue",
                        DEFAULT_FLAG_RES
                    )
                )
                countries.add(
                    CCPCountry(
                        "nz",
                        "64",
                        "New Zealand",
                        DEFAULT_FLAG_RES
                    )
                )
                countries.add(
                    CCPCountry(
                        "om",
                        "968",
                        "Oman",
                        DEFAULT_FLAG_RES
                    )
                )
                countries.add(
                    CCPCountry(
                        "pa",
                        "507",
                        "Panama",
                        DEFAULT_FLAG_RES
                    )
                )
                countries.add(CCPCountry("pe", "51", "Peru", DEFAULT_FLAG_RES))
                countries.add(
                    CCPCountry(
                        "pf",
                        "689",
                        "French Polynesia",
                        DEFAULT_FLAG_RES
                    )
                )
                countries.add(
                    CCPCountry(
                        "pg",
                        "675",
                        "Papua New Guinea",
                        DEFAULT_FLAG_RES
                    )
                )
                countries.add(
                    CCPCountry(
                        "ph",
                        "63",
                        "Philippines",
                        DEFAULT_FLAG_RES
                    )
                )
                countries.add(
                    CCPCountry(
                        "pk",
                        "92",
                        "Pakistan",
                        DEFAULT_FLAG_RES
                    )
                )
                countries.add(
                    CCPCountry(
                        "pl",
                        "48",
                        "Poland",
                        DEFAULT_FLAG_RES
                    )
                )
                countries.add(
                    CCPCountry(
                        "pm",
                        "508",
                        "Saint Pierre And Miquelon",
                        DEFAULT_FLAG_RES
                    )
                )
                countries.add(
                    CCPCountry(
                        "pn",
                        "870",
                        "Pitcairn Islands",
                        DEFAULT_FLAG_RES
                    )
                )
                countries.add(
                    CCPCountry(
                        "pr",
                        "1",
                        "Puerto Rico",
                        DEFAULT_FLAG_RES
                    )
                )
                countries.add(
                    CCPCountry(
                        "ps",
                        "970",
                        "Palestine",
                        DEFAULT_FLAG_RES
                    )
                )
                countries.add(
                    CCPCountry(
                        "pt",
                        "351",
                        "Portugal",
                        DEFAULT_FLAG_RES
                    )
                )
                countries.add(
                    CCPCountry(
                        "pw",
                        "680",
                        "Palau",
                        DEFAULT_FLAG_RES
                    )
                )
                countries.add(
                    CCPCountry(
                        "py",
                        "595",
                        "Paraguay",
                        DEFAULT_FLAG_RES
                    )
                )
                countries.add(
                    CCPCountry(
                        "qa",
                        "974",
                        "Qatar",
                        DEFAULT_FLAG_RES
                    )
                )
                countries.add(
                    CCPCountry(
                        "re",
                        "262",
                        "Réunion",
                        DEFAULT_FLAG_RES
                    )
                )
                countries.add(
                    CCPCountry(
                        "ro",
                        "40",
                        "Romania",
                        DEFAULT_FLAG_RES
                    )
                )
                countries.add(
                    CCPCountry(
                        "rs",
                        "381",
                        "Serbia",
                        DEFAULT_FLAG_RES
                    )
                )
                countries.add(
                    CCPCountry(
                        "ru",
                        "7",
                        "Russian Federation",
                        DEFAULT_FLAG_RES
                    )
                )
                countries.add(
                    CCPCountry(
                        "rw",
                        "250",
                        "Rwanda",
                        DEFAULT_FLAG_RES
                    )
                )
                countries.add(
                    CCPCountry(
                        "sa",
                        "966",
                        "Saudi Arabia",
                        DEFAULT_FLAG_RES
                    )
                )
                countries.add(
                    CCPCountry(
                        "sb",
                        "677",
                        "Solomon Islands",
                        DEFAULT_FLAG_RES
                    )
                )
                countries.add(
                    CCPCountry(
                        "sc",
                        "248",
                        "Seychelles",
                        DEFAULT_FLAG_RES
                    )
                )
                countries.add(
                    CCPCountry(
                        "sd",
                        "249",
                        "Sudan",
                        DEFAULT_FLAG_RES
                    )
                )
                countries.add(
                    CCPCountry(
                        "se",
                        "46",
                        "Sweden",
                        DEFAULT_FLAG_RES
                    )
                )
                countries.add(
                    CCPCountry(
                        "sg",
                        "65",
                        "Singapore",
                        DEFAULT_FLAG_RES
                    )
                )
                countries.add(
                    CCPCountry(
                        "sh",
                        "290",
                        "Saint Helena, Ascension And Tristan Da Cunha",
                        DEFAULT_FLAG_RES
                    )
                )
                countries.add(
                    CCPCountry(
                        "si",
                        "386",
                        "Slovenia",
                        DEFAULT_FLAG_RES
                    )
                )
                countries.add(
                    CCPCountry(
                        "sk",
                        "421",
                        "Slovakia",
                        DEFAULT_FLAG_RES
                    )
                )
                countries.add(
                    CCPCountry(
                        "sl",
                        "232",
                        "Sierra Leone",
                        DEFAULT_FLAG_RES
                    )
                )
                countries.add(
                    CCPCountry(
                        "sm",
                        "378",
                        "San Marino",
                        DEFAULT_FLAG_RES
                    )
                )
                countries.add(
                    CCPCountry(
                        "sn",
                        "221",
                        "Senegal",
                        DEFAULT_FLAG_RES
                    )
                )
                countries.add(
                    CCPCountry(
                        "so",
                        "252",
                        "Somalia",
                        DEFAULT_FLAG_RES
                    )
                )
                countries.add(
                    CCPCountry(
                        "sr",
                        "597",
                        "Suriname",
                        DEFAULT_FLAG_RES
                    )
                )
                countries.add(
                    CCPCountry(
                        "ss",
                        "211",
                        "South Sudan",
                        DEFAULT_FLAG_RES
                    )
                )
                countries.add(
                    CCPCountry(
                        "st",
                        "239",
                        "Sao Tome And Principe",
                        DEFAULT_FLAG_RES
                    )
                )
                countries.add(
                    CCPCountry(
                        "sv",
                        "503",
                        "El Salvador",
                        DEFAULT_FLAG_RES
                    )
                )
                countries.add(
                    CCPCountry(
                        "sx",
                        "1",
                        "Sint Maarten",
                        DEFAULT_FLAG_RES
                    )
                )
                countries.add(
                    CCPCountry(
                        "sy",
                        "963",
                        "Syrian Arab Republic",
                        DEFAULT_FLAG_RES
                    )
                )
                countries.add(
                    CCPCountry(
                        "sz",
                        "268",
                        "Swaziland",
                        DEFAULT_FLAG_RES
                    )
                )
                countries.add(
                    CCPCountry(
                        "tc",
                        "1",
                        "Turks and Caicos Islands",
                        DEFAULT_FLAG_RES
                    )
                )
                countries.add(
                    CCPCountry(
                        "td",
                        "235",
                        "Chad",
                        DEFAULT_FLAG_RES
                    )
                )
                countries.add(
                    CCPCountry(
                        "tg",
                        "228",
                        "Togo",
                        DEFAULT_FLAG_RES
                    )
                )
                countries.add(
                    CCPCountry(
                        "th",
                        "66",
                        "Thailand",
                        DEFAULT_FLAG_RES
                    )
                )
                countries.add(
                    CCPCountry(
                        "tj",
                        "992",
                        "Tajikistan",
                        DEFAULT_FLAG_RES
                    )
                )
                countries.add(
                    CCPCountry(
                        "tk",
                        "690",
                        "Tokelau",
                        DEFAULT_FLAG_RES
                    )
                )
                countries.add(
                    CCPCountry(
                        "tl",
                        "670",
                        "Timor-leste",
                        DEFAULT_FLAG_RES
                    )
                )
                countries.add(
                    CCPCountry(
                        "tm",
                        "993",
                        "Turkmenistan",
                        DEFAULT_FLAG_RES
                    )
                )
                countries.add(
                    CCPCountry(
                        "tn",
                        "216",
                        "Tunisia",
                        DEFAULT_FLAG_RES
                    )
                )
                countries.add(
                    CCPCountry(
                        "to",
                        "676",
                        "Tonga",
                        DEFAULT_FLAG_RES
                    )
                )
                countries.add(
                    CCPCountry(
                        "tr",
                        "90",
                        "Turkey",
                        DEFAULT_FLAG_RES
                    )
                )
                countries.add(
                    CCPCountry(
                        "tt",
                        "1",
                        "Trinidad &amp; Tobago",
                        DEFAULT_FLAG_RES
                    )
                )
                countries.add(
                    CCPCountry(
                        "tv",
                        "688",
                        "Tuvalu",
                        DEFAULT_FLAG_RES
                    )
                )
                countries.add(
                    CCPCountry(
                        "tw",
                        "886",
                        "Taiwan",
                        DEFAULT_FLAG_RES
                    )
                )
                countries.add(
                    CCPCountry(
                        "tz",
                        "255",
                        "Tanzania, United Republic Of",
                        DEFAULT_FLAG_RES
                    )
                )
                countries.add(
                    CCPCountry(
                        "ua",
                        "380",
                        "Ukraine",
                        DEFAULT_FLAG_RES
                    )
                )
                countries.add(
                    CCPCountry(
                        "ug",
                        "256",
                        "Uganda",
                        DEFAULT_FLAG_RES
                    )
                )
                countries.add(
                    CCPCountry(
                        "us",
                        "1",
                        "United States",
                        DEFAULT_FLAG_RES
                    )
                )
                countries.add(
                    CCPCountry(
                        "uy",
                        "598",
                        "Uruguay",
                        DEFAULT_FLAG_RES
                    )
                )
                countries.add(
                    CCPCountry(
                        "uz",
                        "998",
                        "Uzbekistan",
                        DEFAULT_FLAG_RES
                    )
                )
                countries.add(
                    CCPCountry(
                        "va",
                        "379",
                        "Holy See (vatican City State)",
                        DEFAULT_FLAG_RES
                    )
                )
                countries.add(
                    CCPCountry(
                        "vc",
                        "1",
                        "Saint Vincent &amp; The Grenadines",
                        DEFAULT_FLAG_RES
                    )
                )
                countries.add(
                    CCPCountry(
                        "ve",
                        "58",
                        "Venezuela, Bolivarian Republic Of",
                        DEFAULT_FLAG_RES
                    )
                )
                countries.add(
                    CCPCountry(
                        "vg",
                        "1",
                        "British Virgin Islands",
                        DEFAULT_FLAG_RES
                    )
                )
                countries.add(
                    CCPCountry(
                        "vi",
                        "1",
                        "US Virgin Islands",
                        DEFAULT_FLAG_RES
                    )
                )
                countries.add(
                    CCPCountry(
                        "vn",
                        "84",
                        "Vietnam",
                        DEFAULT_FLAG_RES
                    )
                )
                countries.add(
                    CCPCountry(
                        "vu",
                        "678",
                        "Vanuatu",
                        DEFAULT_FLAG_RES
                    )
                )
                countries.add(
                    CCPCountry(
                        "wf",
                        "681",
                        "Wallis And Futuna",
                        DEFAULT_FLAG_RES
                    )
                )
                countries.add(
                    CCPCountry(
                        "ws",
                        "685",
                        "Samoa",
                        DEFAULT_FLAG_RES
                    )
                )
                countries.add(
                    CCPCountry(
                        "xk",
                        "383",
                        "Kosovo",
                        DEFAULT_FLAG_RES
                    )
                )
                countries.add(
                    CCPCountry(
                        "ye",
                        "967",
                        "Yemen",
                        DEFAULT_FLAG_RES
                    )
                )
                countries.add(
                    CCPCountry(
                        "yt",
                        "262",
                        "Mayotte",
                        DEFAULT_FLAG_RES
                    )
                )
                countries.add(
                    CCPCountry(
                        "za",
                        "27",
                        "South Africa",
                        DEFAULT_FLAG_RES
                    )
                )
                countries.add(
                    CCPCountry(
                        "zm",
                        "260",
                        "Zambia",
                        DEFAULT_FLAG_RES
                    )
                )
                countries.add(
                    CCPCountry(
                        "zw",
                        "263",
                        "Zimbabwe",
                        DEFAULT_FLAG_RES
                    )
                )
                return countries
            }
    }
}

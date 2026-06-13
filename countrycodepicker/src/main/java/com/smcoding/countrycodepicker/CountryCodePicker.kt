package com.smcoding.countrycodepicker

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.content.SharedPreferences
import android.content.res.TypedArray
import android.graphics.PorterDuff
import android.graphics.Typeface
import android.telephony.PhoneNumberUtils
import android.telephony.TelephonyManager
import android.text.Editable
import android.text.Selection
import android.text.TextWatcher
import android.util.AttributeSet
import android.util.Log
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.DrawableRes
import androidx.annotation.IdRes
import io.michaelrocks.libphonenumber.android.NumberParseException
import io.michaelrocks.libphonenumber.android.PhoneNumberUtil
import io.michaelrocks.libphonenumber.android.Phonenumber
import java.util.Locale

class CountryCodePicker : RelativeLayout {
    private var talkBackTextProvider: CCPTalkBackTextProvider? = InternalTalkBackTextProvider()
    var ccpPrefFile: String = "CCP_PREF_FILE"
    var defaultCountryCode: Int = 0
    var defaultCountryNameCode: String? = null
    var context: Context
    var holderView: View? = null
    var inflater: LayoutInflater? = null
    var textViewSelectedCountry: TextView? = null
    var editTextRegisteredCarrierNumber: EditText? = null
    var holder: RelativeLayout? = null
    var imageViewArrow: ImageView? = null
    var imageViewFlag: ImageView? = null
    var linearFlagBorder: LinearLayout? = null
    var linearFlagHolder: LinearLayout? = null
    var selectedCCPCountry: CCPCountry? = null

    private var defaultCountry: CCPCountry? = null
    var relativeClickConsumer: RelativeLayout? = null
    var codePicker: CountryCodePicker? = null
    var currentTextGravity: TextGravity? = null
    var originalHint: String = ""
    var ccpPadding: Int = 0

    var selectedAutoDetectionPref: AutoDetectionPref = AutoDetectionPref.SIM_NETWORK_LOCALE
    var phoneUtil: PhoneNumberUtil? = null
    var rippleEnable: Boolean = true
    var showNameCode: Boolean = true
    var showPhoneCode: Boolean = true

    /**
     * To show/hide phone code from country selection dialog
     *
     * @param isCcpDialogShowPhoneCode
     */
    var isCcpDialogShowPhoneCode: Boolean = true
    var showFlag: Boolean = true
    var showFullName: Boolean = false

    /**
     * Set visibility of fast scroller.
     *
     * @param isShowFastScroller
     */
    var isShowFastScroller: Boolean = true

    /**
     * To show/hide title from country selection dialog
     *
     * @param ccpDialogShowTitle
     */
    var ccpDialogShowTitle: Boolean = true

    /**
     * To show/hide flag from country selection dialog
     *
     * @param ccpDialogShowFlag
     */
    var ccpDialogShowFlag: Boolean = true

    /**
     * To show/hide ripple from country selection dialog
     *
     * @param ccpDialogRippleEnable
     */
    var ccpDialogRippleEnable: Boolean = true

    /**
     * SelectionDialogSearch is the facility to search through the list of country while selecting.
     *
     * @param isSearchAllowed true will allow search and false will hide search box
     */
    var isSearchAllowed: Boolean = true
    var showArrow: Boolean = true
    private var isShowCloseIcon: Boolean = false
    var rememberLastSelection: Boolean = false
    var detectCountryWithAreaCode: Boolean = true

    /**
     * To show/hide name code from country selection dialog
     *
     * @param isDialogInitialScrollToSelectionEnabled
     */
    var isDialogInitialScrollToSelectionEnabled: Boolean = true
        get() = ccpDialogInitialScrollToSelection
    var ccpDialogInitialScrollToSelection: Boolean = false
    var ccpUseEmoji: Boolean = false
    var ccpUseDummyEmojiForPreview: Boolean = false
    private var isInternationalFormattingOnlyEnabled = true
    var hintExampleNumberType: PhoneNumberType = PhoneNumberType.MOBILE
    var selectionMemoryTag: String? = "ccp_last_selection"
    var contentColor: Int = DEFAULT_UNSET
    var arrowColor: Int = DEFAULT_UNSET
    var borderFlagColor: Int = 0
    var dialogTypeFace: Typeface? = null
    var dialogTypeFaceStyle: Int = 0
    var preferredCountries: MutableList<CCPCountry>? = null
    var ccpTextGravity: Int = TEXT_GRAVITY_CENTER

    var countryPreference: String? = null

    /**
     * Sets bubble color for fast scroller
     *
     * @param fastScrollerBubbleColor
     */
    var fastScrollerBubbleColor: Int = 0

    /**
     * @param customMasterCountriesList is list of countries that we need as custom master list
     */
    var customMasterCountriesList: MutableList<CCPCountry>? = null

    /**
     * @return comma separated custom master countries' name code. i.e "gb,us,nz,in,pk"
     */
    var customMasterCountriesParam: String? = null
    var excludedCountriesParam: String? = null
    var customDefaultLanguage: Language? = Language.ENGLISH
    var languageToApply: Language? = Language.ENGLISH

    /**
     * By default, keyboard pops up every time ccp is clicked and selection dialog is opened.
     *
     * @param isDialogKeyboardAutoPopup true: to open keyboard automatically when selection dialog is opened
     * false: to avoid auto pop of keyboard
     */
    var isDialogKeyboardAutoPopup: Boolean = true
    var ccpClickable: Boolean = true
    var isAutoDetectLanguageEnabled: Boolean = false
    var isAutoDetectCountryEnabled: Boolean = false
    var numberAutoFormattingEnabled: Boolean = true
    var hintExampleNumberEnabled: Boolean = false
    var xmlWidth: String? = "notSet"
    var validityTextWatcher: TextWatcher? = null
    var formattingTextWatcher: InternationalPhoneTextWatcher? = null
    var reportedValidity: Boolean = false
    var areaCodeCountryDetectorTextWatcher: TextWatcher? = null
    var countryDetectionBasedOnAreaAllowed: Boolean = false
    var lastCheckedAreaCode: String? = null
    var lastCursorPosition: Int = 0
    var countryChangedDueToAreaCode: Boolean = false
    private var onCountryChangeListener: OnCountryChangeListener? = null
    private var phoneNumberValidityChangeListener: PhoneNumberValidityChangeListener? = null
    private var failureListener: FailureListener? = null

    /**
     * Dialog events listener will give call backs on various dialog events
     *
     * @param dialogEventsListener
     */
    private var dialogEventsListener: DialogEventsListener? = null
    private var customDialogTextProvider: CustomDialogTextProvider? = null

    /**
     * This should be the color for fast scroller handle.
     *
     * @param fastScrollerHandleColor
     */
    var fastScrollerHandleColor: Int = 0
    var dialogBackgroundResId: Int = 0
        private set

    /**
     * This will be color of dialog background
     *
     * @param dialogBackgroundColor
     */
    var dialogBackgroundColor: Int = 0

    /**
     * This color will be applied to
     * Title of dialog
     * Name of country
     * Phone code of country
     * "X" button to clear query
     * preferred country divider if preferred countries defined (semi transparent)
     *
     * @param dialogTextColor
     */
    var dialogTextColor: Int = 0

    /**
     * If device is running above or equal LOLLIPOP version, this will change tint of search edittext background.
     *
     * @param dialogSearchEditTextTintColor
     */
    var dialogSearchEditTextTintColor: Int = 0

    /**
     * This sets text appearance for fast scroller index character
     *
     * @param fastScrollerBubbleTextAppearance should be reference id of textappereance style. i.e. R.style.myBubbleTextAppearance
     */
    var fastScrollerBubbleTextAppearance: Int = 0
    var dialogCornerRadius: Float = 0f
        private set
    private var currentCountryGroup: CCPCountryGroup? = null
    private var customClickListener: OnClickListener? = null
    private var countryCodeHolderClickListener: OnClickListener = OnClickListener { v ->
        if (customClickListener == null) {
            if (isCcpClickable()) {
                if (ccpDialogInitialScrollToSelection) {
                    launchCountrySelectionDialog(selectedCountryNameCode)
                } else {
                    launchCountrySelectionDialog()
                }
            }
        } else {
            customClickListener?.onClick(v)
        }
    }

    constructor(context: Context) : super(context) {
        this.context = context
        init(null)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        this.context = context
        init(attrs)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        this.context = context
        init(attrs)
    }

    fun isNumberAutoFormattingEnabled(): Boolean {
        return numberAutoFormattingEnabled
    }

    /**
     * This will set boolean for numberAutoFormattingEnabled and refresh formattingTextWatcher
     *
     * @param numberAutoFormattingEnabled
     */
    fun setNumberAutoFormattingEnabled(numberAutoFormattingEnabled: Boolean) {
        this.numberAutoFormattingEnabled = numberAutoFormattingEnabled
        if (editTextRegisteredCarrierNumber != null) {
            updateFormattingTextWatcher()
        }
    }

    /**
     * This will set boolean for internationalFormattingOnly and refresh formattingTextWatcher
     *
     * @param internationalFormattingOnly
     */
    fun setInternationalFormattingOnly(internationalFormattingOnly: Boolean) {
        this.isInternationalFormattingOnlyEnabled = internationalFormattingOnly
        if (editTextRegisteredCarrierNumber != null) {
            updateFormattingTextWatcher()
        }
    }

    private fun init(attrs: AttributeSet?) {
        inflater = LayoutInflater.from(context)

        if (attrs != null) {
            xmlWidth = attrs.getAttributeValue(ANDROID_NAME_SPACE, "layout_width")
        }
        removeAllViewsInLayout()
        holderView =
            if (attrs != null && xmlWidth != null && (xmlWidth == LayoutParams.MATCH_PARENT.toString() || xmlWidth == "fill_parent" || xmlWidth == "match_parent")) {
                inflater?.inflate(R.layout.layout_full_width_code_picker, this, true)
            } else {
                inflater?.inflate(R.layout.layout_code_picker, this, true)
            }

        textViewSelectedCountry =
            holderView?.findViewById<View?>(R.id.textView_selectedCountry) as? TextView
        holder = holderView?.findViewById<View?>(R.id.countryCodeHolder) as? RelativeLayout
        imageViewArrow = holderView?.findViewById<View?>(R.id.imageView_arrow) as? ImageView
        imageViewFlag = holderView?.findViewById<View?>(R.id.image_flag) as? ImageView
        linearFlagHolder = holderView?.findViewById<View?>(R.id.linear_flag_holder) as? LinearLayout
        linearFlagBorder = holderView?.findViewById<View?>(R.id.linear_flag_border) as? LinearLayout
        relativeClickConsumer =
            holderView?.findViewById<View?>(R.id.rlClickConsumer) as? RelativeLayout
        codePicker = this
        if (attrs != null) {
            applyCustomProperty(attrs)
        }
        relativeClickConsumer?.setOnClickListener(countryCodeHolderClickListener)
    }

    private fun applyCustomProperty(attrs: AttributeSet?) {
        val a: TypedArray =
            context.theme.obtainStyledAttributes(attrs, R.styleable.CountryCodePicker, 0, 0)
        try {
            showNameCode = a.getBoolean(R.styleable.CountryCodePicker_ccp_showNameCode, true)
            numberAutoFormattingEnabled =
                a.getBoolean(R.styleable.CountryCodePicker_ccp_autoFormatNumber, true)
            showPhoneCode = a.getBoolean(R.styleable.CountryCodePicker_ccp_showPhoneCode, true)
            this.isCcpDialogShowPhoneCode =
                a.getBoolean(R.styleable.CountryCodePicker_ccpDialog_showPhoneCode, showPhoneCode)
            this.isDialogInitialScrollToSelectionEnabled =
                a.getBoolean(R.styleable.CountryCodePicker_ccpDialog_showNameCode, true)
            ccpDialogShowTitle =
                a.getBoolean(R.styleable.CountryCodePicker_ccpDialog_showTitle, true)
            ccpUseEmoji = a.getBoolean(R.styleable.CountryCodePicker_ccp_useFlagEmoji, false)
            ccpUseDummyEmojiForPreview =
                a.getBoolean(R.styleable.CountryCodePicker_ccp_useDummyEmojiForPreview, false)
            ccpDialogShowFlag = a.getBoolean(R.styleable.CountryCodePicker_ccpDialog_showFlag, true)
            ccpDialogInitialScrollToSelection = a.getBoolean(
                R.styleable.CountryCodePicker_ccpDialog_initialScrollToSelection,
                false
            )
            ccpDialogRippleEnable =
                a.getBoolean(R.styleable.CountryCodePicker_ccpDialog_rippleEnable, true)
            showFullName = a.getBoolean(R.styleable.CountryCodePicker_ccp_showFullName, false)
            this.isShowFastScroller =
                a.getBoolean(R.styleable.CountryCodePicker_ccpDialog_showFastScroller, true)
            fastScrollerBubbleColor =
                a.getColor(R.styleable.CountryCodePicker_ccpDialog_fastScroller_bubbleColor, 0)
            fastScrollerHandleColor =
                a.getColor(R.styleable.CountryCodePicker_ccpDialog_fastScroller_handleColor, 0)
            fastScrollerBubbleTextAppearance = a.getResourceId(
                R.styleable.CountryCodePicker_ccpDialog_fastScroller_bubbleTextAppearance,
                0
            )
            this.isAutoDetectLanguageEnabled =
                a.getBoolean(R.styleable.CountryCodePicker_ccp_autoDetectLanguage, false)
            detectCountryWithAreaCode =
                a.getBoolean(R.styleable.CountryCodePicker_ccp_areaCodeDetectedCountry, true)
            rememberLastSelection =
                a.getBoolean(R.styleable.CountryCodePicker_ccp_rememberLastSelection, false)
            hintExampleNumberEnabled =
                a.getBoolean(R.styleable.CountryCodePicker_ccp_hintExampleNumber, false)
            this.isInternationalFormattingOnlyEnabled =
                a.getBoolean(R.styleable.CountryCodePicker_ccp_internationalFormattingOnly, true)
            ccpPadding = a.getDimension(
                R.styleable.CountryCodePicker_ccp_padding,
                context.resources.getDimension(R.dimen.ccp_padding)
            ).toInt()
            relativeClickConsumer?.setPadding(ccpPadding, ccpPadding, ccpPadding, ccpPadding)

            val hintNumberTypeIndex: Int =
                a.getInt(R.styleable.CountryCodePicker_ccp_hintExampleNumberType, 0)
            hintExampleNumberType = PhoneNumberType.entries[hintNumberTypeIndex]

            selectionMemoryTag = a.getString(R.styleable.CountryCodePicker_ccp_selectionMemoryTag)
                ?: "CCP_last_selection"

            val autoDetectionPrefValue: Int =
                a.getInt(R.styleable.CountryCodePicker_ccp_countryAutoDetectionPref, 123)
            selectedAutoDetectionPref =
                AutoDetectionPref.getPrefForValue(autoDetectionPrefValue.toString())

            this.isAutoDetectCountryEnabled =
                a.getBoolean(R.styleable.CountryCodePicker_ccp_autoDetectCountry, false)
            showArrow = a.getBoolean(R.styleable.CountryCodePicker_ccp_showArrow, true)
            refreshArrowViewVisibility()
            this.isShowCloseIcon =
                a.getBoolean(R.styleable.CountryCodePicker_ccpDialog_showCloseIcon, false)
            rippleEnable = a.getBoolean(R.styleable.CountryCodePicker_ccp_rippleEnable, true)
            refreshEnableRipple()
            showFlag(a.getBoolean(R.styleable.CountryCodePicker_ccp_showFlag, true))
            this.isDialogKeyboardAutoPopup =
                a.getBoolean(R.styleable.CountryCodePicker_ccpDialog_keyboardAutoPopup, true)

            val attrLanguage: Int = a.getInt(
                R.styleable.CountryCodePicker_ccp_defaultLanguage,
                Language.ENGLISH.ordinal
            )
            customDefaultLanguage = getLanguageEnum(attrLanguage)
            updateLanguageToApply()

            customMasterCountriesParam =
                a.getString(R.styleable.CountryCodePicker_ccp_customMasterCountries)
            excludedCountriesParam =
                a.getString(R.styleable.CountryCodePicker_ccp_excludedCountries)
            if (!isInEditMode) refreshCustomMasterList()

            countryPreference = a.getString(R.styleable.CountryCodePicker_ccp_countryPreference)
            if (!isInEditMode) refreshPreferredCountries()

            if (a.hasValue(R.styleable.CountryCodePicker_ccp_textGravity)) {
                ccpTextGravity =
                    a.getInt(R.styleable.CountryCodePicker_ccp_textGravity, TEXT_GRAVITY_CENTER)
            }
            applyTextGravity(ccpTextGravity)

            defaultCountryNameCode = a.getString(R.styleable.CountryCodePicker_ccp_defaultNameCode)
            var setUsingNameCode = false
            if (!defaultCountryNameCode.isNullOrEmpty()) {
                val country = if (!isInEditMode) {
                    CCPCountry.getCountryForNameCodeFromLibraryMasterList(
                        context,
                        getLanguageToApply()!!,
                        defaultCountryNameCode
                    )
                } else {
                    CCPCountry.getCountryForNameCodeFromEnglishList(defaultCountryNameCode)
                }
                if (country != null) {
                    setUsingNameCode = true
                    this.defaultCountry = country
                    this.selectedCountry = country
                }
            }

            var defaultPhoneCode: Int =
                a.getInteger(R.styleable.CountryCodePicker_ccp_defaultPhoneCode, -1)
            if (!setUsingNameCode && defaultPhoneCode != -1) {
                if (!isInEditMode) {
                    if (CCPCountry.getCountryForCode(
                            context,
                            getLanguageToApply()!!,
                            preferredCountries,
                            defaultPhoneCode
                        ) == null
                    ) {
                        defaultPhoneCode = LIB_DEFAULT_COUNTRY_CODE
                    }
                    setDefaultCountryUsingPhoneCode(defaultPhoneCode)
                    this.selectedCountry = this.defaultCountry
                } else {
                    var editDefaultCountry =
                        CCPCountry.getCountryForCodeFromEnglishList(defaultPhoneCode.toString())
                            ?: CCPCountry.getCountryForCodeFromEnglishList(LIB_DEFAULT_COUNTRY_CODE.toString())
                    this.defaultCountry = editDefaultCountry
                    this.selectedCountry = editDefaultCountry
                }
            }

            if (this.defaultCountry == null) {
                this.defaultCountry = CCPCountry.getCountryForNameCodeFromEnglishList("IN")
                if (this.selectedCountry == null) this.selectedCountry = this.defaultCountry
            }

            if (this.isAutoDetectCountryEnabled && !isInEditMode) setAutoDetectedCountry(true)
            if (rememberLastSelection && !isInEditMode) loadLastSelectedCountryInCCP()

            val currentArrowColor: Int =
                a.getColor(R.styleable.CountryCodePicker_ccp_arrowColor, DEFAULT_UNSET)
            setArrowColor(currentArrowColor)

            val currentContentColor: Int = a.getColor(
                R.styleable.CountryCodePicker_ccp_contentColor,
                if (isInEditMode) DEFAULT_UNSET else context.resources.getColor(R.color.defaultContentColor)
            )
            if (currentContentColor != DEFAULT_UNSET) setContentColor(currentContentColor)

            val currentBorderFlagColor: Int = a.getColor(
                R.styleable.CountryCodePicker_ccp_flagBorderColor,
                if (isInEditMode) 0 else context.resources.getColor(R.color.defaultBorderFlagColor)
            )
            if (currentBorderFlagColor != 0) setFlagBorderColor(currentBorderFlagColor)

            this.dialogBackgroundColor =
                a.getColor(R.styleable.CountryCodePicker_ccpDialog_backgroundColor, 0)
            setDialogBackground(
                a.getResourceId(
                    R.styleable.CountryCodePicker_ccpDialog_background,
                    0
                )
            )
            this.dialogTextColor = a.getColor(R.styleable.CountryCodePicker_ccpDialog_textColor, 0)
            this.dialogSearchEditTextTintColor =
                a.getColor(R.styleable.CountryCodePicker_ccpDialog_searchEditTextTint, 0)
            setDialogCornerRadius(
                a.getDimension(
                    R.styleable.CountryCodePicker_ccpDialog_cornerRadius,
                    0f
                )
            )

            val textSize: Int =
                a.getDimensionPixelSize(R.styleable.CountryCodePicker_ccp_textSize, 0)
            if (textSize > 0) {
                textViewSelectedCountry?.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize.toFloat())
                setFlagSize(textSize)
                setArrowSize(textSize)
            }

            val arrowSize: Int =
                a.getDimensionPixelSize(R.styleable.CountryCodePicker_ccp_arrowSize, 0)
            if (arrowSize > 0) setArrowSize(arrowSize)

            this.isSearchAllowed =
                a.getBoolean(R.styleable.CountryCodePicker_ccpDialog_allowSearch, true)
            setCcpClickable(a.getBoolean(R.styleable.CountryCodePicker_ccp_clickable, true))
        } catch (e: Exception) {
            Log.e(TAG, "Error applying properties", e)
        } finally {
            a.recycle()
        }
    }

    private fun refreshArrowViewVisibility() {
        imageViewArrow?.visibility = if (showArrow) VISIBLE else GONE
    }

    private fun refreshEnableRipple() {
        if (rippleEnable) {
            val outValue = TypedValue()
            context.theme.resolveAttribute(android.R.attr.selectableItemBackground, outValue, true)
            if (outValue.resourceId != 0) relativeClickConsumer?.setBackgroundResource(outValue.resourceId)
            else relativeClickConsumer?.setBackgroundResource(outValue.data)
        }
    }

    private fun loadLastSelectedCountryInCCP() {
        val sharedPref: SharedPreferences =
            context.getSharedPreferences(ccpPrefFile, Context.MODE_PRIVATE)
        val lastSelectedCountryNameCode: String? = sharedPref.getString(selectionMemoryTag, null)
        if (lastSelectedCountryNameCode != null) setCountryForNameCode(lastSelectedCountryNameCode)
    }

    fun storeSelectedCountryNameCode(selectedCountryNameCode: String?) {
        val sharedPref: SharedPreferences =
            context.getSharedPreferences(ccpPrefFile, Context.MODE_PRIVATE)
        val editor: SharedPreferences.Editor = sharedPref.edit()
        editor.putString(selectionMemoryTag, selectedCountryNameCode)
        editor.apply()
    }

    fun isShowPhoneCode(): Boolean = showPhoneCode

    fun setShowPhoneCode(showPhoneCode: Boolean) {
        this.showPhoneCode = showPhoneCode
        this.selectedCountry = selectedCCPCountry
    }

    fun getCurrentTextGravity(): TextGravity? = currentTextGravity

    fun setCurrentTextGravity(textGravity: TextGravity) {
        this.currentTextGravity = textGravity
        applyTextGravity(textGravity.enumIndex)
    }

    private fun applyTextGravity(enumIndex: Int) {
        textViewSelectedCountry?.gravity = when (enumIndex) {
            TextGravity.LEFT.enumIndex -> Gravity.LEFT
            TextGravity.CENTER.enumIndex -> Gravity.CENTER
            else -> Gravity.RIGHT
        }
    }

    private fun updateLanguageToApply() {
        if (isInEditMode) {
            languageToApply = customDefaultLanguage ?: Language.ENGLISH
        } else {
            if (this.isAutoDetectLanguageEnabled) {
                languageToApply =
                    this.cCPLanguageFromLocale ?: getCustomDefaultLanguage() ?: Language.ENGLISH
            } else {
                languageToApply = getCustomDefaultLanguage() ?: Language.ENGLISH
            }
        }
    }

    private val cCPLanguageFromLocale: Language?
        get() {
            val currentLocale = context.resources.configuration.locale
            for (language in Language.entries) {
                if (language.code.equals(currentLocale.language, ignoreCase = true)) {
                    if (language.country == null || language.country.equals(
                            currentLocale.country,
                            ignoreCase = true
                        )
                    ) return language
                    if (language.script == null || language.script.equals(
                            currentLocale.script,
                            ignoreCase = true
                        )
                    ) return language
                }
            }
            return null
        }

    fun getTextViewSelectedCountry(): TextView? = textViewSelectedCountry

    fun setTextViewSelectedCountry(textViewSelectedCountry: TextView) {
        this.textViewSelectedCountry = textViewSelectedCountry
    }

    fun getImageViewFlag(): ImageView? = imageViewFlag

    fun setImageViewFlag(imageViewFlag: ImageView) {
        this.imageViewFlag = imageViewFlag
    }

    private var selectedCountry: CCPCountry?
        get() {
            if (selectedCCPCountry == null) selectedCCPCountry = this.defaultCountry
            return selectedCCPCountry
        }
        set(value) {
            var countryToSet = value
            if (talkBackTextProvider != null && talkBackTextProvider?.getTalkBackTextForCountry(
                    countryToSet
                ) != null
            ) {
                textViewSelectedCountry?.contentDescription =
                    talkBackTextProvider?.getTalkBackTextForCountry(countryToSet)
            }

            countryDetectionBasedOnAreaAllowed = false
            lastCheckedAreaCode = ""

            if (countryToSet == null) {
                countryToSet = CCPCountry.getCountryForCode(
                    context,
                    getLanguageToApply()!!,
                    preferredCountries,
                    defaultCountryCode
                )
                if (countryToSet == null) return
            }

            this.selectedCCPCountry = countryToSet
            var displayText = ""
            if (showFlag && ccpUseEmoji) {
                displayText += if (isInEditMode) {
                    if (ccpUseDummyEmojiForPreview) "\uD83C\uDFC1\u200B " else CCPCountry.getFlagEmoji(
                        countryToSet
                    ) + "\u200B "
                } else {
                    CCPCountry.getFlagEmoji(countryToSet) + "  "
                }
            }

            if (showFullName) displayText += countryToSet.name
            if (showNameCode) {
                displayText += if (showFullName) " (" + countryToSet.getNameCode()
                    .uppercase() + ")" else " " + countryToSet.getNameCode().uppercase()
            }
            if (showPhoneCode) {
                if (displayText.isNotEmpty()) displayText += "  "
                displayText += "+" + countryToSet.phoneCode
            }

            textViewSelectedCountry?.text = displayText
            if (!showFlag && displayText.isEmpty()) {
                displayText = "+" + countryToSet.phoneCode
                textViewSelectedCountry?.text = displayText
            }

            imageViewFlag?.setImageResource(countryToSet.flagID)
            onCountryChangeListener?.onCountrySelected()
            updateFormattingTextWatcher()
            updateHint()

            if (editTextRegisteredCarrierNumber != null && phoneNumberValidityChangeListener != null) {
                reportedValidity = this.isValidFullNumber
                phoneNumberValidityChangeListener?.onValidityChanged(reportedValidity)
            }

            countryDetectionBasedOnAreaAllowed = true
            if (countryChangedDueToAreaCode) {
                try {
                    editTextRegisteredCarrierNumber?.setSelection(lastCursorPosition)
                    countryChangedDueToAreaCode = false
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            updateCountryGroup()
        }

    private fun updateCountryGroup() {
        currentCountryGroup =
            CCPCountryGroup.getCountryGroupForPhoneCode(this.selectedCountryCodeAsInt)
    }

    private fun updateHint() {
        val et = editTextRegisteredCarrierNumber
        if (et != null && hintExampleNumberEnabled) {
            var formattedNumber: String? = ""
            val exampleNumber = getPhoneUtil()?.getExampleNumberForType(
                this.selectedCountryNameCode,
                this.selectedHintNumberType
            )
            if (exampleNumber != null) {
                formattedNumber = exampleNumber.nationalNumber.toString()
                formattedNumber = PhoneNumberUtils.formatNumber(
                    this.selectedCountryCodeWithPlus + formattedNumber,
                    this.selectedCountryNameCode
                )
                if (formattedNumber != null) {
                    formattedNumber =
                        formattedNumber.substring(this.selectedCountryCodeWithPlus.length).trim()
                }
            }
            if (formattedNumber == null) formattedNumber = originalHint
            et.hint = formattedNumber
        }
    }

    private val selectedHintNumberType: PhoneNumberUtil.PhoneNumberType
        get() = when (hintExampleNumberType) {
            PhoneNumberType.MOBILE -> PhoneNumberUtil.PhoneNumberType.MOBILE
            PhoneNumberType.FIXED_LINE -> PhoneNumberUtil.PhoneNumberType.FIXED_LINE
            PhoneNumberType.FIXED_LINE_OR_MOBILE -> PhoneNumberUtil.PhoneNumberType.FIXED_LINE_OR_MOBILE
            PhoneNumberType.TOLL_FREE -> PhoneNumberUtil.PhoneNumberType.TOLL_FREE
            PhoneNumberType.PREMIUM_RATE -> PhoneNumberUtil.PhoneNumberType.PREMIUM_RATE
            PhoneNumberType.SHARED_COST -> PhoneNumberUtil.PhoneNumberType.SHARED_COST
            PhoneNumberType.VOIP -> PhoneNumberUtil.PhoneNumberType.VOIP
            PhoneNumberType.PERSONAL_NUMBER -> PhoneNumberUtil.PhoneNumberType.PERSONAL_NUMBER
            PhoneNumberType.PAGER -> PhoneNumberUtil.PhoneNumberType.PAGER
            PhoneNumberType.UAN -> PhoneNumberUtil.PhoneNumberType.UAN
            PhoneNumberType.VOICEMAIL -> PhoneNumberUtil.PhoneNumberType.VOICEMAIL
            PhoneNumberType.UNKNOWN -> PhoneNumberUtil.PhoneNumberType.UNKNOWN
        }

    fun getLanguageToApply(): Language? {
        if (languageToApply == null) updateLanguageToApply()
        return languageToApply
    }

    fun setLanguageToApply(languageToApply: Language?) {
        this.languageToApply = languageToApply
    }

    private fun updateFormattingTextWatcher() {
        val et = editTextRegisteredCarrierNumber
        val country = selectedCCPCountry
        if (et != null && country != null) {
            val digitsValue = PhoneNumberUtil.normalizeDigitsOnly(et.text.toString())
            formattingTextWatcher?.let { et.removeTextChangedListener(it) }
            areaCodeCountryDetectorTextWatcher?.let { et.removeTextChangedListener(it) }

            if (numberAutoFormattingEnabled) {
                formattingTextWatcher = InternationalPhoneTextWatcher(
                    context,
                    this.selectedCountryNameCode,
                    this.selectedCountryCodeAsInt,
                    this.isInternationalFormattingOnlyEnabled
                )
                et.addTextChangedListener(formattingTextWatcher)
            }

            if (detectCountryWithAreaCode) {
                areaCodeCountryDetectorTextWatcher = this.countryDetectorTextWatcher
                et.addTextChangedListener(areaCodeCountryDetectorTextWatcher)
            }

            et.setText("")
            et.setText(digitsValue)
            et.setSelection(et.text.length)
        }
    }

    private val countryDetectorTextWatcher: TextWatcher?
        get() {
            if (editTextRegisteredCarrierNumber != null) {
                if (areaCodeCountryDetectorTextWatcher == null) {
                    areaCodeCountryDetectorTextWatcher = object : TextWatcher {
                        var lastCheckedNumber: String? = null
                        override fun beforeTextChanged(
                            s: CharSequence?,
                            start: Int,
                            count: Int,
                            after: Int
                        ) {
                        }

                        override fun onTextChanged(
                            s: CharSequence,
                            start: Int,
                            before: Int,
                            count: Int
                        ) {
                            val picker = this@CountryCodePicker
                            val currentCountry = picker.selectedCountry
                            if (currentCountry != null && (lastCheckedNumber == null || lastCheckedNumber != s.toString()) && picker.countryDetectionBasedOnAreaAllowed) {
                                picker.currentCountryGroup?.let { group ->
                                    val enteredValue = s.toString()
                                    if (enteredValue.length >= group.areaCodeLength) {
                                        val digitsValue =
                                            PhoneNumberUtil.normalizeDigitsOnly(enteredValue)
                                        if (digitsValue.length >= group.areaCodeLength) {
                                            val currentAreaCode =
                                                digitsValue.substring(0, group.areaCodeLength)
                                            if (currentAreaCode != picker.lastCheckedAreaCode) {
                                                val detectedCountry = group.getCountryForAreaCode(
                                                    context,
                                                    picker.getLanguageToApply()!!,
                                                    currentAreaCode
                                                )
                                                if (detectedCountry != currentCountry) {
                                                    picker.countryChangedDueToAreaCode = true
                                                    picker.lastCursorPosition =
                                                        Selection.getSelectionEnd(s)
                                                    picker.selectedCountry = detectedCountry
                                                }
                                                picker.lastCheckedAreaCode = currentAreaCode
                                            }
                                        }
                                    }
                                }
                                lastCheckedNumber = s.toString()
                            }
                        }

                        override fun afterTextChanged(s: Editable?) {}
                    }
                }
            }
            return areaCodeCountryDetectorTextWatcher
        }

    fun getCustomDefaultLanguage(): Language? = customDefaultLanguage

    private fun setCustomDefaultLanguage(customDefaultLanguage: Language?) {
        this.customDefaultLanguage = customDefaultLanguage
        updateLanguageToApply()
        selectedCCPCountry?.let { country ->
            val updatedCountry = CCPCountry.getCountryForNameCodeFromLibraryMasterList(
                context,
                getLanguageToApply()!!,
                country.getNameCode()
            )
            if (updatedCountry != null) this.selectedCountry = updatedCountry
        }
    }

    fun getHolder(): RelativeLayout? = holder

    fun showCloseIcon(showCloseIcon: Boolean) {
        this.isShowCloseIcon = showCloseIcon
    }

    fun getEditTextRegisteredCarrierNumber(): EditText? = editTextRegisteredCarrierNumber

    fun setEditTextRegisteredCarrierNumber(editTextRegisteredCarrierNumber: EditText?) {
        this.editTextRegisteredCarrierNumber = editTextRegisteredCarrierNumber
        this.editTextRegisteredCarrierNumber?.hint?.let { originalHint = it.toString() }
        updateValidityTextWatcher()
        updateFormattingTextWatcher()
        updateHint()
    }

    private fun updateValidityTextWatcher() {
        val et = editTextRegisteredCarrierNumber
        et?.removeTextChangedListener(validityTextWatcher)
        reportedValidity = this.isValidFullNumber
        phoneNumberValidityChangeListener?.onValidityChanged(reportedValidity)

        validityTextWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val picker = this@CountryCodePicker
                picker.phoneNumberValidityChangeListener?.let { listener ->
                    val currentValidity = picker.isValidFullNumber
                    if (currentValidity != picker.reportedValidity) {
                        picker.reportedValidity = currentValidity
                        listener.onValidityChanged(picker.reportedValidity)
                    }
                }
            }
        }
        et?.addTextChangedListener(validityTextWatcher)
    }

    fun setDialogBackground(@IdRes dialogBackgroundResId: Int) {
        this.dialogBackgroundResId = dialogBackgroundResId
    }

    fun setDialogCornerRadius(dialogCornerRadius: Float) {
        this.dialogCornerRadius = dialogCornerRadius
    }

    fun getDialogTypeFace(): Typeface? = dialogTypeFace

    fun setDialogTypeFace(typeFace: Typeface?) {
        try {
            dialogTypeFace = typeFace
            dialogTypeFaceStyle = DEFAULT_UNSET
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun refreshPreferredCountries() {
        val pref = countryPreference
        if (pref.isNullOrEmpty()) {
            preferredCountries = null
        } else {
            val localList = ArrayList<CCPCountry>()
            for (nameCode in pref.split(",").map { it.trim() }.filter { it.isNotEmpty() }) {
                val country = CCPCountry.getCountryForNameCodeFromCustomMasterList(
                    context,
                    customMasterCountriesList,
                    getLanguageToApply()!!,
                    nameCode
                )
                if (country != null && !isAlreadyInList(country, localList)) localList.add(country)
            }
            preferredCountries = if (localList.isEmpty()) null else localList
        }
    }

    fun refreshCustomMasterList() {
        val customParam = customMasterCountriesParam
        val excludedParam = excludedCountriesParam
        if (customParam.isNullOrEmpty()) {
            if (!excludedParam.isNullOrEmpty()) {
                val lowerExcluded = excludedParam.lowercase(Locale.getDefault())
                val libraryMasterList =
                    CCPCountry.getLibraryMasterCountryList(context, getLanguageToApply()!!)
                val filteredList = libraryMasterList.filter {
                    !lowerExcluded.contains(
                        it.getNameCode().lowercase(Locale.getDefault())
                    )
                }.toMutableList()
                customMasterCountriesList = if (filteredList.isNotEmpty()) filteredList else null
            } else customMasterCountriesList = null
        } else {
            val localList = ArrayList<CCPCountry>()
            for (nameCode in customParam.split(",").map { it.trim() }.filter { it.isNotEmpty() }) {
                val country = CCPCountry.getCountryForNameCodeFromLibraryMasterList(
                    context,
                    getLanguageToApply()!!,
                    nameCode
                )
                if (country != null && !isAlreadyInList(country, localList)) localList.add(country)
            }
            customMasterCountriesList = if (localList.isEmpty()) null else localList
        }
    }

    fun setCustomMasterCountries(customMasterCountriesParam: String?) {
        this.customMasterCountriesParam = customMasterCountriesParam
    }

    fun setExcludedCountries(excludedCountries: String?) {
        this.excludedCountriesParam = excludedCountries
        refreshCustomMasterList()
    }

    fun isCcpClickable(): Boolean = ccpClickable

    fun setCcpClickable(ccpClickable: Boolean) {
        this.ccpClickable = ccpClickable
        relativeClickConsumer?.let { consumer ->
            consumer.setOnClickListener(if (ccpClickable) countryCodeHolderClickListener else null)
            consumer.isClickable = ccpClickable
            consumer.isEnabled = ccpClickable
        }
    }

    private fun isAlreadyInList(country: CCPCountry?, list: MutableList<CCPCountry>?): Boolean {
        if (country != null && list != null) {
            return list.any { it.getNameCode().equals(country.getNameCode(), ignoreCase = true) }
        }
        return false
    }

    private fun detectCarrierNumber(fullNumber: String?, country: CCPCountry?): String? {
        if (country?.phoneCode == null || fullNumber.isNullOrEmpty()) return fullNumber
        val indexOfCode = fullNumber.indexOf(country.phoneCode!!)
        return if (indexOfCode == -1) fullNumber else fullNumber.substring(indexOfCode + country.phoneCode!!.length)
    }

    private fun getLanguageEnum(index: Int): Language =
        if (index < Language.entries.size) Language.entries[index] else Language.ENGLISH

    val dialogTitle: String?
        get() {
            val defaultTitle = CCPCountry.getDialogTitle(context, getLanguageToApply()!!)
            return customDialogTextProvider?.getCCPDialogTitle(getLanguageToApply(), defaultTitle)
                ?: defaultTitle
        }

    val searchHintText: String?
        get() {
            val defaultHint = CCPCountry.getSearchHintMessage(context, getLanguageToApply()!!)
            return customDialogTextProvider?.getCCPDialogSearchHintText(
                getLanguageToApply(),
                defaultHint
            ) ?: defaultHint
        }

    val noResultACK: String?
        get() {
            val defaultNoResultACK =
                CCPCountry.getNoResultFoundAckMessage(context, getLanguageToApply()!!)
            return customDialogTextProvider?.getCCPDialogNoResultACK(
                getLanguageToApply(),
                defaultNoResultACK
            ) ?: defaultNoResultACK
        }

    @Deprecated("Use setDefaultCountryUsingNameCode")
    fun setDefaultCountryUsingPhoneCode(defaultCountryCode: Int) {
        val defaultCCPCountry = CCPCountry.getCountryForCode(
            context,
            getLanguageToApply()!!,
            preferredCountries,
            defaultCountryCode
        )
        if (defaultCCPCountry != null) {
            this.defaultCountryCode = defaultCountryCode
            this.defaultCountry = defaultCCPCountry
        }
    }

    fun setDefaultCountryUsingNameCode(defaultCountryNameCode: String?) {
        val defaultCCPCountry = CCPCountry.getCountryForNameCodeFromLibraryMasterList(
            context,
            getLanguageToApply()!!,
            defaultCountryNameCode
        )
        if (defaultCCPCountry != null) {
            this.defaultCountryNameCode = defaultCCPCountry.getNameCode()
            this.defaultCountry = defaultCCPCountry
        }
    }

    fun getDefaultCountryCode(): String? = defaultCountry?.phoneCode

    val defaultCountryCodeAsInt: Int
        get() = try {
            getDefaultCountryCode()?.toInt() ?: 0
        } catch (e: Exception) {
            0
        }

    val defaultCountryCodeWithPlus: String get() = "+" + getDefaultCountryCode()

    val defaultCountryName: String? get() = defaultCountry?.name ?: ""

    fun getDefaultCountryNameCode(): String = defaultCountry?.nameCode?.uppercase() ?: ""

    fun resetToDefaultCountry() {
        this.defaultCountry = CCPCountry.getCountryForNameCodeFromLibraryMasterList(
            context,
            getLanguageToApply()!!,
            getDefaultCountryNameCode()
        )
        this.selectedCountry = this.defaultCountry
    }

    val selectedCountryCode: String? get() = this.selectedCountry?.phoneCode

    val selectedCountryCodeWithPlus: String get() = "+" + this.selectedCountryCode

    val selectedCountryCodeAsInt: Int
        get() = try {
            this.selectedCountryCode?.toInt() ?: 0
        } catch (e: Exception) {
            0
        }

    val selectedCountryName: String? get() = this.selectedCountry?.name

    val selectedCountryEnglishName: String? get() = this.selectedCountry?.englishName

    val selectedCountryNameCode: String get() = this.selectedCountry?.nameCode?.uppercase() ?: ""

    @get:DrawableRes
    val selectedCountryFlagResourceId: Int get() = this.selectedCountry?.flagResID ?: 0

    fun setCountryForPhoneCode(countryCode: Int) {
        val ccpCountry = CCPCountry.getCountryForCode(
            context,
            getLanguageToApply()!!,
            preferredCountries,
            countryCode
        )
        if (ccpCountry == null) {
            if (this.defaultCountry == null) {
                this.defaultCountry = CCPCountry.getCountryForCode(
                    context,
                    getLanguageToApply()!!,
                    preferredCountries,
                    defaultCountryCode
                )
            }
            this.selectedCountry = this.defaultCountry
        } else this.selectedCountry = ccpCountry
    }

    fun setCountryForNameCode(countryNameCode: String?) {
        val country = CCPCountry.getCountryForNameCodeFromLibraryMasterList(
            context,
            getLanguageToApply()!!,
            countryNameCode
        )
        if (country == null) {
            if (this.defaultCountry == null) {
                this.defaultCountry = CCPCountry.getCountryForCode(
                    context,
                    getLanguageToApply()!!,
                    preferredCountries,
                    defaultCountryCode
                )
            }
            this.selectedCountry = this.defaultCountry
        } else this.selectedCountry = country
    }

    fun registerCarrierNumberEditText(editTextCarrierNumber: EditText?) {
        setEditTextRegisteredCarrierNumber(editTextCarrierNumber)
    }

    fun deregisterCarrierNumberEditText() {
        editTextRegisteredCarrierNumber?.let { et ->
            try {
                et.removeTextChangedListener(validityTextWatcher)
            } catch (ignored: Exception) {
            }
            try {
                et.removeTextChangedListener(formattingTextWatcher)
            } catch (ignored: Exception) {
            }
            et.hint = ""
        }
        editTextRegisteredCarrierNumber = null
    }

    @get:Throws(NumberParseException::class)
    private val enteredPhoneNumber: Phonenumber.PhoneNumber?
        get() {
            val et = editTextRegisteredCarrierNumber ?: return null
            return getPhoneUtil()?.parse(
                PhoneNumberUtil.normalizeDigitsOnly(et.text.toString()),
                this.selectedCountryNameCode
            )
        }

    var fullNumber: String?
        get() {
            return try {
                val phoneNumber = this.enteredPhoneNumber ?: return ""
                getPhoneUtil()?.format(phoneNumber, PhoneNumberUtil.PhoneNumberFormat.E164)
                    ?.substring(1) ?: ""
            } catch (e: NumberParseException) {
                (this.selectedCountryCode ?: "") + PhoneNumberUtil.normalizeDigitsOnly(
                    editTextRegisteredCarrierNumber?.text.toString()
                )
            }
        }
        set(fullNumber) {
            var country = CCPCountry.getCountryForNumber(
                context,
                getLanguageToApply()!!,
                preferredCountries,
                fullNumber
            ) ?: this.defaultCountry
            this.selectedCountry = country
            val carrierNumber = detectCarrierNumber(fullNumber, country)
            editTextRegisteredCarrierNumber?.let {
                it.setText(carrierNumber)
                updateFormattingTextWatcher()
            }
        }

    val formattedFullNumber: String
        get() = try {
            val phoneNumber = this.enteredPhoneNumber ?: return ""
            "+" + (getPhoneUtil()?.format(
                phoneNumber,
                PhoneNumberUtil.PhoneNumberFormat.INTERNATIONAL
            )?.substring(1) ?: "")
        } catch (e: NumberParseException) {
            this.fullNumberWithPlus
        }

    val fullNumberWithPlus: String get() = "+" + this.fullNumber

    fun getContentColor(): Int = contentColor

    fun setContentColor(contentColor: Int) {
        this.contentColor = contentColor
        textViewSelectedCountry?.setTextColor(this.contentColor)
        if (this.arrowColor == DEFAULT_UNSET) imageViewArrow?.setColorFilter(
            this.contentColor,
            PorterDuff.Mode.SRC_IN
        )
    }

    fun setArrowColor(arrowColor: Int) {
        this.arrowColor = arrowColor
        if (this.arrowColor == DEFAULT_UNSET) {
            if (contentColor != DEFAULT_UNSET) imageViewArrow?.setColorFilter(
                this.contentColor,
                PorterDuff.Mode.SRC_IN
            )
        } else imageViewArrow?.setColorFilter(this.arrowColor, PorterDuff.Mode.SRC_IN)
    }

    fun setFlagBorderColor(borderFlagColor: Int) {
        this.borderFlagColor = borderFlagColor
        linearFlagBorder?.setBackgroundColor(this.borderFlagColor)
    }

    fun setTextSize(textSize: Int) {
        if (textSize > 0) {
            textViewSelectedCountry?.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize.toFloat())
            setArrowSize(textSize)
            setFlagSize(textSize)
        }
    }

    fun setArrowSize(arrowSize: Int) {
        if (arrowSize > 0) {
            imageViewArrow?.layoutParams?.let { params ->
                params.width = arrowSize
                params.height = arrowSize
                imageViewArrow?.layoutParams = params
            }
        }
    }

    fun showNameCode(showNameCode: Boolean) {
        this.showNameCode = showNameCode
        this.selectedCountry = selectedCCPCountry
    }

    fun showArrow(showArrow: Boolean) {
        this.showArrow = showArrow
        refreshArrowViewVisibility()
    }

    fun setCountryPreference(countryPreference: String?) {
        this.countryPreference = countryPreference
    }

    fun changeDefaultLanguage(language: Language?) {
        setCustomDefaultLanguage(language)
    }

    fun setTypeFace(typeFace: Typeface?) {
        try {
            textViewSelectedCountry?.typeface = typeFace
            setDialogTypeFace(typeFace)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun setDialogTypeFace(typeFace: Typeface?, style: Int) {
        try {
            dialogTypeFace = typeFace
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun setTypeFace(typeFace: Typeface?, style: Int) {
        try {
            textViewSelectedCountry?.setTypeface(typeFace, style)
            setDialogTypeFace(typeFace, style)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun setOnCountryChangeListener(onCountryChangeListener: OnCountryChangeListener?) {
        this.onCountryChangeListener = onCountryChangeListener
    }

    fun setFlagSize(flagSize: Int) {
        imageViewFlag?.layoutParams?.height = flagSize
        imageViewFlag?.requestLayout()
    }

    fun showFlag(showFlag: Boolean) {
        this.showFlag = showFlag
        refreshFlagVisibility()
        if (!isInEditMode) this.selectedCountry = selectedCCPCountry
    }

    private fun refreshFlagVisibility() {
        if (showFlag) {
            linearFlagHolder?.visibility = if (ccpUseEmoji) GONE else VISIBLE
        } else linearFlagHolder?.visibility = GONE
    }

    fun useFlagEmoji(useFlagEmoji: Boolean) {
        this.ccpUseEmoji = useFlagEmoji
        refreshFlagVisibility()
        this.selectedCountry = selectedCCPCountry
    }

    fun showFullName(showFullName: Boolean) {
        this.showFullName = showFullName
        this.selectedCountry = selectedCCPCountry
    }

    fun setPhoneNumberValidityChangeListener(phoneNumberValidityChangeListener: PhoneNumberValidityChangeListener?) {
        this.phoneNumberValidityChangeListener = phoneNumberValidityChangeListener
        if (editTextRegisteredCarrierNumber != null && phoneNumberValidityChangeListener != null) {
            reportedValidity = this.isValidFullNumber
            phoneNumberValidityChangeListener.onValidityChanged(reportedValidity)
        }
    }

    fun setAutoDetectionFailureListener(failureListener: FailureListener?) {
        this.failureListener = failureListener
    }

    fun setCustomDialogTextProvider(customDialogTextProvider: CustomDialogTextProvider?) {
        this.customDialogTextProvider = customDialogTextProvider
    }

    @JvmOverloads
    fun launchCountrySelectionDialog(countryNameCode: String? = null) {
        CountryCodeDialog.openCountryCodeDialog(codePicker!!, countryNameCode)
    }

    val isValidFullNumber: Boolean
        get() {
            val et = editTextRegisteredCarrierNumber
            val country = selectedCCPCountry
            return if (et != null && et.text.isNotEmpty() && country != null) {
                try {
                    val phoneNumber = getPhoneUtil()?.parse(
                        "+" + country.phoneCode + et.text.toString(),
                        country.getNameCode()
                    )
                    getPhoneUtil()?.isValidNumber(phoneNumber) ?: false
                } catch (e: NumberParseException) {
                    false
                }
            } else {
                if (et == null) Toast.makeText(
                    context,
                    "No editText for Carrier number found.",
                    Toast.LENGTH_SHORT
                ).show()
                false
            }
        }

    private fun getPhoneUtil(): PhoneNumberUtil? {
        if (phoneUtil == null) phoneUtil = PhoneNumberUtil.createInstance(context)
        return phoneUtil
    }

    fun setAutoDetectedCountry(loadDefaultWhenFails: Boolean) {
        try {
            var successfullyDetected = false
            for (i in 0 until selectedAutoDetectionPref.representation.length) {
                when (selectedAutoDetectionPref.representation[i]) {
                    '1' -> successfullyDetected = detectSIMCountry(false)
                    '2' -> successfullyDetected = detectNetworkCountry(false)
                    '3' -> successfullyDetected = detectLocaleCountry(false)
                }
                if (successfullyDetected) break
                else failureListener?.onCountryAutoDetectionFailed()
            }
            if (!successfullyDetected && loadDefaultWhenFails) resetToDefaultCountry()
        } catch (e: Exception) {
            Log.w(TAG, "setAutoDetectCountry: Exception ${e.message}")
            if (loadDefaultWhenFails) resetToDefaultCountry()
        }
    }

    fun detectSIMCountry(loadDefaultWhenFails: Boolean): Boolean = try {
        val telephonyManager =
            context.getSystemService(Context.TELEPHONY_SERVICE) as? TelephonyManager
        val simCountryISO = telephonyManager?.simCountryIso
        if (simCountryISO.isNullOrEmpty() || !isNameCodeInCustomMasterList(simCountryISO)) {
            if (loadDefaultWhenFails) resetToDefaultCountry()
            false
        } else {
            this.selectedCountry = CCPCountry.getCountryForNameCodeFromLibraryMasterList(
                context,
                getLanguageToApply()!!,
                simCountryISO
            )
            true
        }
    } catch (e: Exception) {
        if (loadDefaultWhenFails) resetToDefaultCountry()
        false
    }

    private fun isNameCodeInCustomMasterList(nameCode: String?): Boolean {
        val allowedList = CCPCountry.getCustomMasterCountryList(context, this)
        return allowedList?.any { it.nameCode.equals(nameCode, ignoreCase = true) } ?: false
    }

    fun detectNetworkCountry(loadDefaultWhenFails: Boolean): Boolean = try {
        val telephonyManager =
            context.getSystemService(Context.TELEPHONY_SERVICE) as? TelephonyManager
        val networkCountryISO = telephonyManager?.networkCountryIso
        if (networkCountryISO.isNullOrEmpty() || !isNameCodeInCustomMasterList(networkCountryISO)) {
            if (loadDefaultWhenFails) resetToDefaultCountry()
            false
        } else {
            this.selectedCountry = CCPCountry.getCountryForNameCodeFromLibraryMasterList(
                context,
                getLanguageToApply()!!,
                networkCountryISO
            )
            true
        }
    } catch (e: Exception) {
        if (loadDefaultWhenFails) resetToDefaultCountry()
        false
    }

    fun detectLocaleCountry(loadDefaultWhenFails: Boolean): Boolean = try {
        val localeCountryISO = context.resources.configuration.locale.country
        if (localeCountryISO.isNullOrEmpty() || !isNameCodeInCustomMasterList(localeCountryISO)) {
            if (loadDefaultWhenFails) resetToDefaultCountry()
            false
        } else {
            this.selectedCountry = CCPCountry.getCountryForNameCodeFromLibraryMasterList(
                context,
                getLanguageToApply()!!,
                localeCountryISO
            )
            true
        }
    } catch (e: Exception) {
        if (loadDefaultWhenFails) resetToDefaultCountry()
        false
    }

    fun setCountryAutoDetectionPref(selectedAutoDetectionPref: AutoDetectionPref) {
        this.selectedAutoDetectionPref = selectedAutoDetectionPref
    }

    fun onUserTappedCountry(country: CCPCountry) {
        if (codePicker?.rememberLastSelection == true) codePicker?.storeSelectedCountryNameCode(
            country.getNameCode()
        )
        this.selectedCountry = country
    }

    fun setDetectCountryWithAreaCode(detectCountryWithAreaCode: Boolean) {
        this.detectCountryWithAreaCode = detectCountryWithAreaCode
        updateFormattingTextWatcher()
    }

    fun setHintExampleNumberEnabled(hintExampleNumberEnabled: Boolean) {
        this.hintExampleNumberEnabled = hintExampleNumberEnabled
        updateHint()
    }

    fun setHintExampleNumberType(hintExampleNumberType: PhoneNumberType) {
        this.hintExampleNumberType = hintExampleNumberType
        updateHint()
    }

    fun setTalkBackTextProvider(talkBackTextProvider: CCPTalkBackTextProvider?) {
        this.talkBackTextProvider = talkBackTextProvider
        this.selectedCountry = selectedCCPCountry
    }

    fun enableDialogInitialScrollToSelection(initialScrollToSelection: Boolean) {
        this.ccpDialogInitialScrollToSelection = initialScrollToSelection
    }

    fun overrideClickListener(clickListener: OnClickListener?) {
        customClickListener = clickListener
    }

    override fun onDetachedFromWindow() {
        CountryCodeDialog.clear()
        super.onDetachedFromWindow()
    }

    enum class Language(val code: String, val country: String? = null, val script: String? = null) {
        AFRIKAANS("af"), ARABIC("ar"), BASQUE("eu"), BELARUSIAN("by"), BENGALI("bn"),
        CHINESE_SIMPLIFIED("zh", "CN", "Hans"), CHINESE_TRADITIONAL("zh", "TW", "Hant"),
        CZECH("cs"), DANISH("da"), DUTCH("nl"), ENGLISH("en"), FARSI("fa"), FRENCH("fr"),
        GERMAN("de"), GREEK("el"), GUJARATI("gu"), HAUSA("ha"), HEBREW("iw"), HINDI("hi"),
        HUNGARIAN("hu"), INDONESIA("in"), ITALIAN("it"), JAPANESE("ja"), KAZAKH("kk"),
        KOREAN("ko"), LITHUANIAN("lt"), MARATHI("mr"), POLISH("pl"), PORTUGUESE("pt"),
        PUNJABI("pa"), RUSSIAN("ru"), SERBIAN("sr"), SLOVAK("sk"), SLOVENIAN("si"),
        SPANISH("es"), SWEDISH("sv"), TAGALOG("tl"), TAMIL("ta"), THAI("th"), TURKISH("tr"),
        UKRAINIAN("uk"), URDU("ur"), UZBEK("uz"), VIETNAMESE("vi");

        companion object {
            fun forCountryNameCode(code: String?): Language =
                entries.find { it.code == code } ?: ENGLISH
        }
    }

    enum class PhoneNumberType {
        MOBILE, FIXED_LINE, FIXED_LINE_OR_MOBILE, TOLL_FREE, PREMIUM_RATE,
        SHARED_COST, VOIP, PERSONAL_NUMBER, PAGER, UAN, VOICEMAIL, UNKNOWN
    }

    enum class AutoDetectionPref(val representation: String) {
        SIM_ONLY("1"), NETWORK_ONLY("2"), LOCALE_ONLY("3"),
        SIM_NETWORK("12"), NETWORK_SIM("21"), SIM_LOCALE("13"), LOCALE_SIM("31"),
        NETWORK_LOCALE("23"), LOCALE_NETWORK("32"),
        SIM_NETWORK_LOCALE("123"), SIM_LOCALE_NETWORK("132"),
        NETWORK_SIM_LOCALE("213"), NETWORK_LOCALE_SIM("231"),
        LOCALE_SIM_NETWORK("312"), LOCALE_NETWORK_SIM("321");

        companion object {
            fun getPrefForValue(value: String?): AutoDetectionPref =
                entries.find { it.representation == value } ?: SIM_NETWORK_LOCALE
        }
    }

    enum class TextGravity(val enumIndex: Int) { LEFT(-1), CENTER(0), RIGHT(1) }

    interface OnCountryChangeListener {
        fun onCountrySelected()
    }

    interface FailureListener {
        fun onCountryAutoDetectionFailed()
    }

    interface PhoneNumberValidityChangeListener {
        fun onValidityChanged(isValidNumber: Boolean)
    }

    interface DialogEventsListener {
        fun onCcpDialogOpen(dialog: Dialog?)
        fun onCcpDialogDismiss(dialogInterface: DialogInterface?)
        fun onCcpDialogCancel(dialogInterface: DialogInterface?)
    }

    interface CustomDialogTextProvider {
        fun getCCPDialogTitle(language: Language?, defaultTitle: String?): String?
        fun getCCPDialogSearchHintText(language: Language?, defaultSearchHintText: String?): String?
        fun getCCPDialogNoResultACK(language: Language?, defaultNoResultACK: String?): String?
    }

    companion object {
        const val DEFAULT_UNSET: Int = -99
        var TAG: String = "CCP"
        const val LIB_DEFAULT_COUNTRY_CODE: Int = 91
        private const val TEXT_GRAVITY_CENTER = 0
        private const val ANDROID_NAME_SPACE = "http://schemas.android.com/apk/res/android"
    }
}
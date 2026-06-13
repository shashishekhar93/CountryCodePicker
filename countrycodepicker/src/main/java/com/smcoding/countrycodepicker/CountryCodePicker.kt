package com.smcoding.countrycodepicker

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.res.ColorStateList
import android.content.res.TypedArray
import android.graphics.Typeface
import android.net.Uri
import android.telephony.PhoneNumberUtils
import android.telephony.TelephonyManager
import android.text.Editable
import android.text.Selection
import android.text.TextWatcher
import android.util.AttributeSet
import android.util.Log
import android.util.TypedValue
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
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.core.net.toUri
import androidx.core.os.ConfigurationCompat
import io.michaelrocks.libphonenumber.android.PhoneNumberUtil
import io.michaelrocks.libphonenumber.android.Phonenumber

/**
 * Custom View that provides a country code selection UI and integrates with phone number entry.
 * It handles country detection, number formatting, and provides a selection dialog.
 */
class CountryCodePicker @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : RelativeLayout(context, attrs, defStyleAttr) {

    // Properties with default values
    private var talkBackTextProvider: CCPTalkBackTextProvider? = InternalTalkBackTextProvider()
    private val ccpPrefFile: String = "CCP_PREF_FILE"
    var defaultCountryCode: Int = 0
    var initialDefaultCountryNameCode: String? = null
    var originalHint: String = ""
    var ccpPadding: Int = 0
    var rememberLastSelection: Boolean = false
    var selectionMemoryTag: String? = "ccp_last_selection"

    // UI elements
    private var holderView: View? = null
    private var textViewSelectedCountry: TextView? = null
    private var holder: RelativeLayout? = null
    private var imageViewArrow: ImageView? = null
    private var imageViewFlag: ImageView? = null
    private var linearFlagBorder: LinearLayout? = null
    private var linearFlagHolder: LinearLayout? = null
    private var relativeClickConsumer: RelativeLayout? = null

    /**
     * Attached [EditText] for phone number input. 
     * Setting this enables automatic formatting and validation.
     */
    var editTextRegisteredCarrierNumber: EditText? = null
        set(value) {
            field = value
            field?.hint?.let { originalHint = it.toString() }
            updateValidityTextWatcher()
            updateFormattingTextWatcher()
            updateHint()
        }

    /**
     * The currently selected country.
     */
    var selectedCCPCountry: CCPCountry? = null
        private set
    private var defaultCountry: CCPCountry? = null

    /**
     * Utility for phone number parsing and formatting.
     */
    val phoneUtil: PhoneNumberUtil by lazy { PhoneNumberUtil.createInstance(context) }

    // UI visibility and behavior flags
    var showNameCode: Boolean = true
        set(value) {
            field = value; refreshUI()
        }
    var showPhoneCode: Boolean = true
        set(value) {
            field = value; refreshUI()
        }
    var showFlag: Boolean = true
        set(value) {
            field = value; refreshFlagVisibility(); refreshUI()
        }
    var showFullName: Boolean = false
        set(value) {
            field = value; refreshUI()
        }
    var showArrow: Boolean = true
        set(value) {
            field = value; refreshArrowViewVisibility()
        }
    var isSearchAllowed: Boolean = true
    var isShowCloseIcon: Boolean = false
    var isDialogKeyboardAutoPopup: Boolean = true
    var isShowFastScroller: Boolean = true
    var ccpDialogShowTitle: Boolean = true
    var ccpDialogShowFlag: Boolean = true
    var ccpDialogRippleEnable: Boolean = true
    var isCcpDialogShowPhoneCode: Boolean = true
    var ccpDialogInitialScrollToSelection: Boolean = false
    var ccpUseEmoji: Boolean = false
    var ccpUseDummyEmojiForPreview: Boolean = false
    var rippleEnable: Boolean = true
        set(value) {
            field = value; refreshEnableRipple()
        }

    // Auto-detection logic
    var isAutoDetectLanguageEnabled: Boolean = false
    var isAutoDetectCountryEnabled: Boolean = false
    var detectCountryWithAreaCode: Boolean = true
        set(value) {
            field = value; updateFormattingTextWatcher()
        }
    var selectedAutoDetectionPref: AutoDetectionPref = AutoDetectionPref.SIM_NETWORK_LOCALE

    // Formatting and Validation
    var numberAutoFormattingEnabled: Boolean = true
        set(value) {
            field = value; updateFormattingTextWatcher()
        }
    var hintExampleNumberEnabled: Boolean = false
        set(value) {
            field = value; updateHint()
        }
    var isInternationalFormattingOnlyEnabled = true
        set(value) {
            field = value; updateFormattingTextWatcher()
        }
    var hintExampleNumberType: PhoneNumberType = PhoneNumberType.MOBILE
        set(value) {
            field = value; updateHint()
        }

    // Styling properties
    var contentColor: Int = DEFAULT_UNSET
        set(value) {
            field = value
            textViewSelectedCountry?.setTextColor(field)
            if (arrowColor == DEFAULT_UNSET) imageViewArrow?.imageTintList =
                ColorStateList.valueOf(field)
        }
    var arrowColor: Int = DEFAULT_UNSET
        set(value) {
            field = value
            val color = if (field == DEFAULT_UNSET) contentColor else field
            if (color != DEFAULT_UNSET) imageViewArrow?.imageTintList =
                ColorStateList.valueOf(color)
        }
    var borderFlagColor: Int = 0
    var dialogTypeFace: Typeface? = null
    var dialogTypeFaceStyle: Int = 0
    var dialogBackgroundColor: Int = 0
    var dialogTextColor: Int = 0
    var dialogSearchEditTextTintColor: Int = 0
    var dialogCornerRadius: Float = 0f
    var dialogBackgroundResId: Int = 0
        private set

    var fastScrollerBubbleColor: Int = 0
    var fastScrollerHandleColor: Int = 0
    var fastScrollerBubbleTextAppearance: Int = 0

    // Masters and Preferences
    var customMasterCountriesList: MutableList<CCPCountry>? = null
    var customMasterCountriesParam: String? = null
    var excludedCountriesParam: String? = null
    var preferredCountries: MutableList<CCPCountry>? = null
    var countryPreference: String? = null

    // Language handling
    var customDefaultLanguage: Language? = Language.ENGLISH
        set(value) {
            field = value
            updateLanguageToApply()
            selectedCCPCountry?.let { country ->
                CCPCountry.getCountryForNameCodeFromLibraryMasterList(
                    context,
                    languageToApply!!,
                    country.nameCode
                )?.let {
                    selectedCountry = it
                }
            }
        }
    var languageToApply: Language? = Language.ENGLISH
        get() {
            if (field == null) updateLanguageToApply()
            return field
        }

    // Listeners
    var onCountryChangeListener: OnCountryChangeListener? = null
    var phoneNumberValidityChangeListener: PhoneNumberValidityChangeListener? = null
    var failureListener: FailureListener? = null
    var dialogEventsListener: DialogEventsListener? = null
    var customDialogTextProvider: CustomDialogTextProvider? = null
    var onRequestCountryListener: OnRequestCountryListener? = null

    // Internal state management
    private var validityTextWatcher: TextWatcher? = null
    private var formattingTextWatcher: InternationalPhoneTextWatcher? = null
    private var areaCodeCountryDetectorTextWatcher: TextWatcher? = null
    private var reportedValidity: Boolean = false
    private var countryDetectionBasedOnAreaAllowed: Boolean = false
    private var lastCheckedAreaCode: String? = null
    private var lastCursorPosition: Int = 0
    private var countryChangedDueToAreaCode: Boolean = false
    private var currentCountryGroup: CCPCountryGroup? = null
    private var customClickListener: OnClickListener? = null

    init {
        initUI(attrs)
        if (attrs != null) applyCustomProperty(attrs)
    }

    private fun initUI(attrs: AttributeSet?) {
        val inflater = LayoutInflater.from(context)
        val xmlWidth = attrs?.getAttributeValue(ANDROID_NAME_SPACE, "layout_width")

        val isFullWidth =
            xmlWidth != null && (xmlWidth == LayoutParams.MATCH_PARENT.toString() || xmlWidth == "fill_parent" || xmlWidth == "match_parent")
        val layoutRes =
            if (isFullWidth) R.layout.layout_full_width_code_picker else R.layout.item_country_code

        holderView = inflater.inflate(layoutRes, this, true)
        textViewSelectedCountry = holderView?.findViewById(R.id.textView_selectedCountry)
        holder = holderView?.findViewById(R.id.countryCodeHolder)
        imageViewArrow = holderView?.findViewById(R.id.imageView_arrow)
        imageViewFlag = holderView?.findViewById(R.id.image_flag)
        linearFlagHolder = holderView?.findViewById(R.id.linear_flag_holder)
        linearFlagBorder = holderView?.findViewById(R.id.linear_flag_border)
        relativeClickConsumer = holderView?.findViewById(R.id.rlClickConsumer)

        relativeClickConsumer?.setOnClickListener {
            if (customClickListener != null) {
                customClickListener?.onClick(it)
            } else if (isEnabled && isClickable) {
                launchCountrySelectionDialog(if (ccpDialogInitialScrollToSelection) selectedCountryNameCode else null)
            }
        }
    }

    private fun applyCustomProperty(attrs: AttributeSet) {
        val a: TypedArray =
            context.theme.obtainStyledAttributes(attrs, R.styleable.CountryCodePicker, 0, 0)
        try {
            showNameCode = a.getBoolean(R.styleable.CountryCodePicker_ccp_showNameCode, true)
            showPhoneCode = a.getBoolean(R.styleable.CountryCodePicker_ccp_showPhoneCode, true)
            showFlag = a.getBoolean(R.styleable.CountryCodePicker_ccp_showFlag, true)
            showFullName = a.getBoolean(R.styleable.CountryCodePicker_ccp_showFullName, false)
            showArrow = a.getBoolean(R.styleable.CountryCodePicker_ccp_showArrow, true)

            isCcpDialogShowPhoneCode =
                a.getBoolean(R.styleable.CountryCodePicker_ccpDialog_showPhoneCode, showPhoneCode)
            ccpDialogShowTitle =
                a.getBoolean(R.styleable.CountryCodePicker_ccpDialog_showTitle, true)
            ccpDialogShowFlag = a.getBoolean(R.styleable.CountryCodePicker_ccpDialog_showFlag, true)
            ccpDialogRippleEnable =
                a.getBoolean(R.styleable.CountryCodePicker_ccpDialog_rippleEnable, true)
            isSearchAllowed =
                a.getBoolean(R.styleable.CountryCodePicker_ccpDialog_allowSearch, true)
            isShowCloseIcon =
                a.getBoolean(R.styleable.CountryCodePicker_ccpDialog_showCloseIcon, false)
            isDialogKeyboardAutoPopup =
                a.getBoolean(R.styleable.CountryCodePicker_ccpDialog_keyboardAutoPopup, true)
            isShowFastScroller =
                a.getBoolean(R.styleable.CountryCodePicker_ccpDialog_showFastScroller, true)
            ccpDialogInitialScrollToSelection = a.getBoolean(
                R.styleable.CountryCodePicker_ccpDialog_initialScrollToSelection,
                false
            )

            numberAutoFormattingEnabled =
                a.getBoolean(R.styleable.CountryCodePicker_ccp_autoFormatNumber, true)
            ccpUseEmoji = a.getBoolean(R.styleable.CountryCodePicker_ccp_useFlagEmoji, false)
            ccpUseDummyEmojiForPreview =
                a.getBoolean(R.styleable.CountryCodePicker_ccp_useDummyEmojiForPreview, false)
            isAutoDetectLanguageEnabled =
                a.getBoolean(R.styleable.CountryCodePicker_ccp_autoDetectLanguage, false)
            isAutoDetectCountryEnabled =
                a.getBoolean(R.styleable.CountryCodePicker_ccp_autoDetectCountry, false)
            detectCountryWithAreaCode =
                a.getBoolean(R.styleable.CountryCodePicker_ccp_areaCodeDetectedCountry, true)
            rememberLastSelection =
                a.getBoolean(R.styleable.CountryCodePicker_ccp_rememberLastSelection, false)
            hintExampleNumberEnabled =
                a.getBoolean(R.styleable.CountryCodePicker_ccp_hintExampleNumber, false)
            isInternationalFormattingOnlyEnabled =
                a.getBoolean(R.styleable.CountryCodePicker_ccp_internationalFormattingOnly, true)
            rippleEnable = a.getBoolean(R.styleable.CountryCodePicker_ccp_rippleEnable, true)

            ccpPadding = a.getDimension(
                R.styleable.CountryCodePicker_ccp_padding,
                context.resources.getDimension(R.dimen.ccp_padding)
            ).toInt()
            relativeClickConsumer?.setPadding(ccpPadding, ccpPadding, ccpPadding, ccpPadding)

            val textSize = a.getDimensionPixelSize(R.styleable.CountryCodePicker_ccp_textSize, 0)
            if (textSize > 0) {
                textViewSelectedCountry?.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize.toFloat())
                setFlagSize(textSize)
                setArrowSize(textSize)
            }

            val arrowSize = a.getDimensionPixelSize(R.styleable.CountryCodePicker_ccp_arrowSize, 0)
            if (arrowSize > 0) setArrowSize(arrowSize)

            arrowColor = a.getColor(R.styleable.CountryCodePicker_ccp_arrowColor, DEFAULT_UNSET)
            contentColor = a.getColor(
                R.styleable.CountryCodePicker_ccp_contentColor,
                if (isInEditMode) DEFAULT_UNSET else ContextCompat.getColor(
                    context,
                    R.color.defaultContentColor
                )
            )

            val flagBorderColor = a.getColor(
                R.styleable.CountryCodePicker_ccp_flagBorderColor,
                if (isInEditMode) 0 else ContextCompat.getColor(
                    context,
                    R.color.defaultBorderFlagColor
                )
            )
            if (flagBorderColor != 0) setFlagBorderColor(flagBorderColor)

            dialogBackgroundColor =
                a.getColor(R.styleable.CountryCodePicker_ccpDialog_backgroundColor, 0)
            dialogBackgroundResId =
                a.getResourceId(R.styleable.CountryCodePicker_ccpDialog_background, 0)
            dialogTextColor = a.getColor(R.styleable.CountryCodePicker_ccpDialog_textColor, 0)
            dialogSearchEditTextTintColor =
                a.getColor(R.styleable.CountryCodePicker_ccpDialog_searchEditTextTint, 0)
            dialogCornerRadius =
                a.getDimension(R.styleable.CountryCodePicker_ccpDialog_cornerRadius, 0f)

            fastScrollerBubbleColor =
                a.getColor(R.styleable.CountryCodePicker_ccpDialog_fastScroller_bubbleColor, 0)
            fastScrollerHandleColor =
                a.getColor(R.styleable.CountryCodePicker_ccpDialog_fastScroller_handleColor, 0)
            fastScrollerBubbleTextAppearance = a.getResourceId(
                R.styleable.CountryCodePicker_ccpDialog_fastScroller_bubbleTextAppearance,
                0
            )

            customMasterCountriesParam =
                a.getString(R.styleable.CountryCodePicker_ccp_customMasterCountries)
            excludedCountriesParam =
                a.getString(R.styleable.CountryCodePicker_ccp_excludedCountries)
            countryPreference = a.getString(R.styleable.CountryCodePicker_ccp_countryPreference)
            selectionMemoryTag = a.getString(R.styleable.CountryCodePicker_ccp_selectionMemoryTag)
                ?: "CCP_last_selection"

            val hintNumberTypeIndex =
                a.getInt(R.styleable.CountryCodePicker_ccp_hintExampleNumberType, 0)
            hintExampleNumberType = PhoneNumberType.entries[hintNumberTypeIndex]

            val autoDetectionPrefValue =
                a.getInt(R.styleable.CountryCodePicker_ccp_countryAutoDetectionPref, 123)
            selectedAutoDetectionPref =
                AutoDetectionPref.getPrefForValue(autoDetectionPrefValue.toString())

            val attrLanguage = a.getInt(
                R.styleable.CountryCodePicker_ccp_defaultLanguage,
                Language.ENGLISH.ordinal
            )
            customDefaultLanguage = Language.entries[attrLanguage]

            setupInitialCountry(a)

        } catch (e: Exception) {
            Log.e(TAG, "Error applying properties", e)
        } finally {
            a.recycle()
        }
    }

    private fun setupInitialCountry(a: TypedArray) {
        initialDefaultCountryNameCode =
            a.getString(R.styleable.CountryCodePicker_ccp_defaultNameCode)
        var countrySet = false

        if (!initialDefaultCountryNameCode.isNullOrEmpty()) {
            val country = if (isInEditMode) CCPCountry.getCountryForNameCodeFromEnglishList(
                initialDefaultCountryNameCode
            )
            else CCPCountry.getCountryForNameCodeFromLibraryMasterList(
                context,
                languageToApply!!,
                initialDefaultCountryNameCode
            )

            if (country != null) {
                defaultCountry = country
                selectedCountry = country
                countrySet = true
            }
        }

        if (!countrySet) {
            val defaultPhoneCode =
                a.getInteger(R.styleable.CountryCodePicker_ccp_defaultPhoneCode, -1)
            if (defaultPhoneCode != -1) {
                val country =
                    if (isInEditMode) CCPCountry.getCountryForCodeFromEnglishList(defaultPhoneCode.toString())
                    else CCPCountry.getCountryForCode(
                        context,
                        languageToApply!!,
                        preferredCountries,
                        defaultPhoneCode
                    )

                defaultCountry = country ?: CCPCountry.getCountryForNameCodeFromEnglishList("IN")
                selectedCountry = defaultCountry
                countrySet = true
            }
        }

        if (!countrySet) {
            defaultCountry = CCPCountry.getCountryForNameCodeFromEnglishList("IN")
            selectedCountry = defaultCountry
        }

        if (!isInEditMode) {
            if (isAutoDetectCountryEnabled) setAutoDetectedCountry(true)
            if (rememberLastSelection) loadLastSelectedCountryInCCP()
        }
    }

    private fun refreshArrowViewVisibility() {
        imageViewArrow?.visibility = if (showArrow) VISIBLE else GONE
    }

    private fun refreshEnableRipple() {
        if (rippleEnable) {
            val outValue = TypedValue()
            context.theme.resolveAttribute(android.R.attr.selectableItemBackground, outValue, true)
            relativeClickConsumer?.setBackgroundResource(if (outValue.resourceId != 0) outValue.resourceId else outValue.data)
        } else {
            relativeClickConsumer?.background = null
        }
    }

    private fun refreshFlagVisibility() {
        if (showFlag) {
            linearFlagHolder?.visibility = if (ccpUseEmoji) GONE else VISIBLE
        } else {
            linearFlagHolder?.visibility = GONE
        }
    }

    private fun loadLastSelectedCountryInCCP() {
        val sharedPref = context.getSharedPreferences(ccpPrefFile, Context.MODE_PRIVATE)
        sharedPref.getString(selectionMemoryTag, null)?.let { setCountryForNameCode(it) }
    }

    private fun storeSelectedCountryNameCode(selectedCountryNameCode: String?) {
        val sharedPref = context.getSharedPreferences(ccpPrefFile, Context.MODE_PRIVATE)
        sharedPref.edit { putString(selectionMemoryTag, selectedCountryNameCode) }
    }

    private fun updateLanguageToApply() {
        languageToApply = if (isAutoDetectLanguageEnabled && !isInEditMode) {
            cCPLanguageFromLocale ?: customDefaultLanguage ?: Language.ENGLISH
        } else {
            customDefaultLanguage ?: Language.ENGLISH
        }
    }

    private val cCPLanguageFromLocale: Language?
        get() {
            val currentLocale = ConfigurationCompat.getLocales(context.resources.configuration)[0]
            return Language.entries.find { lang ->
                lang.code.equals(currentLocale?.language, true) &&
                        (lang.country == null || lang.country.equals(
                            currentLocale?.country,
                            true
                        )) &&
                        (lang.script == null || lang.script.equals(currentLocale?.script, true))
            }
        }

    private fun refreshUI() {
        selectedCountry = selectedCCPCountry
    }

    /**
     * Gets or sets the currently selected country. Updating this triggers UI changes.
     */
    var selectedCountry: CCPCountry?
        get() = selectedCCPCountry ?: defaultCountry
        set(value) {
            val countryToSet = value ?: CCPCountry.getCountryForCode(
                context,
                languageToApply!!,
                preferredCountries,
                defaultCountryCode
            )
            if (countryToSet == null) return

            talkBackTextProvider?.getTalkBackTextForCountry(countryToSet)?.let {
                textViewSelectedCountry?.contentDescription = it
            }

            countryDetectionBasedOnAreaAllowed = false
            lastCheckedAreaCode = ""
            selectedCCPCountry = countryToSet

            // Construct display text
            var displayText = ""
            if (showFlag && ccpUseEmoji) {
                displayText += if (isInEditMode && ccpUseDummyEmojiForPreview) "\uD83C\uDFC1\u200B "
                else "${CCPCountry.getFlagEmoji(countryToSet)}\u200B  "
            }

            if (showFullName) displayText += countryToSet.name
            if (showNameCode) {
                val code = " ${countryToSet.nameCode?.uppercase()}"
                displayText += if (showFullName) " ($code)" else code
            }
            if (showPhoneCode) {
                if (displayText.isNotEmpty()) displayText += "  "
                displayText += "+${countryToSet.phoneCode}"
            }

            textViewSelectedCountry?.text = displayText.ifEmpty { "+${countryToSet.phoneCode}" }
            imageViewFlag?.setImageResource(countryToSet.flagID)

            onCountryChangeListener?.onCountrySelected()
            updateFormattingTextWatcher()
            updateHint()

            if (editTextRegisteredCarrierNumber != null) {
                reportedValidity = isValidFullNumber
                phoneNumberValidityChangeListener?.onValidityChanged(reportedValidity)
            }

            countryDetectionBasedOnAreaAllowed = true
            if (countryChangedDueToAreaCode) {
                try {
                    editTextRegisteredCarrierNumber?.setSelection(lastCursorPosition)
                    countryChangedDueToAreaCode = false
                } catch (ignored: Exception) {
                }
            }
            updateCountryGroup()
        }

    private fun updateCountryGroup() {
        currentCountryGroup = CCPCountryGroup.getCountryGroupForPhoneCode(selectedCountryCodeAsInt)
    }

    private fun updateHint() {
        val et = editTextRegisteredCarrierNumber ?: return
        if (hintExampleNumberEnabled) {
            var formattedNumber: String? = null
            val exampleNumber =
                phoneUtil.getExampleNumberForType(selectedCountryNameCode, selectedHintNumberType)
            if (exampleNumber != null) {
                val national = exampleNumber.nationalNumber.toString()
                formattedNumber = PhoneNumberUtils.formatNumber(
                    "${selectedCountryCodeWithPlus}$national",
                    selectedCountryNameCode
                )
                formattedNumber =
                    formattedNumber?.substringAfter(selectedCountryCodeWithPlus)?.trim()
            }
            et.hint = formattedNumber ?: originalHint
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

    private fun updateFormattingTextWatcher() {
        val et = editTextRegisteredCarrierNumber ?: return

        val digitsValue = PhoneNumberUtil.normalizeDigitsOnly(et.text.toString())
        formattingTextWatcher?.let { et.removeTextChangedListener(it) }
        areaCodeCountryDetectorTextWatcher?.let { et.removeTextChangedListener(it) }

        if (numberAutoFormattingEnabled) {
            formattingTextWatcher = InternationalPhoneTextWatcher(
                context,
                selectedCountryNameCode,
                selectedCountryCodeAsInt,
                isInternationalFormattingOnlyEnabled
            )
            et.addTextChangedListener(formattingTextWatcher)
        }

        if (detectCountryWithAreaCode) {
            areaCodeCountryDetectorTextWatcher = countryDetectorTextWatcher
            et.addTextChangedListener(areaCodeCountryDetectorTextWatcher)
        }

        et.setText(digitsValue)
        et.setSelection(et.text.length)
    }

    private val countryDetectorTextWatcher: TextWatcher
        get() = areaCodeCountryDetectorTextWatcher ?: object : TextWatcher {
            var lastCheckedNumber: String? = null
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                val entered = s.toString()
                if (entered == lastCheckedNumber || !countryDetectionBasedOnAreaAllowed) return

                currentCountryGroup?.let { group ->
                    if (entered.length >= group.areaCodeLength) {
                        val digits = PhoneNumberUtil.normalizeDigitsOnly(entered)
                        if (digits.length >= group.areaCodeLength) {
                            val areaCode = digits.substring(0, group.areaCodeLength)
                            if (areaCode != lastCheckedAreaCode) {
                                group.getCountryForAreaCode(context, languageToApply!!, areaCode)
                                    ?.let { detected ->
                                        if (detected != selectedCountry) {
                                            countryChangedDueToAreaCode = true
                                            lastCursorPosition = Selection.getSelectionEnd(s)
                                            selectedCountry = detected
                                        }
                                    }
                                lastCheckedAreaCode = areaCode
                            }
                        }
                    }
                }
                lastCheckedNumber = entered
            }

            override fun afterTextChanged(s: Editable?) {}
        }.also { areaCodeCountryDetectorTextWatcher = it }

    private fun updateValidityTextWatcher() {
        val et = editTextRegisteredCarrierNumber ?: return
        et.removeTextChangedListener(validityTextWatcher)

        reportedValidity = isValidFullNumber
        phoneNumberValidityChangeListener?.onValidityChanged(reportedValidity)

        validityTextWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val current = isValidFullNumber
                if (current != reportedValidity) {
                    reportedValidity = current
                    phoneNumberValidityChangeListener?.onValidityChanged(reportedValidity)
                }
            }
        }.also { et.addTextChangedListener(it) }
    }

    /**
     * Set the background resource for the selection dialog.
     */
    fun setDialogBackground(@IdRes resId: Int) {
        dialogBackgroundResId = resId
    }

    /**
     * Set the border color for the country flag.
     */
    fun setFlagBorderColor(color: Int) {
        borderFlagColor = color; linearFlagBorder?.setBackgroundColor(color)
    }

    /**
     * Set the flag size in pixels.
     */
    fun setFlagSize(size: Int) {
        imageViewFlag?.layoutParams?.height = size; imageViewFlag?.requestLayout()
    }

    /**
     * Set the arrow icon size in pixels.
     */
    fun setArrowSize(size: Int) {
        if (size > 0) imageViewArrow?.layoutParams?.apply {
            width = size; height = size; imageViewArrow?.layoutParams = this
        }
    }

    /**
     * Refreshes the list of preferred countries.
     */
    fun refreshPreferredCountries() {
        val pref = countryPreference
        if (pref.isNullOrEmpty()) {
            preferredCountries = null
        } else {
            val list = mutableListOf<CCPCountry>()
            pref.split(",").map { it.trim() }.filter { it.isNotEmpty() }.forEach { code ->
                CCPCountry.getCountryForNameCodeFromCustomMasterList(
                    context,
                    customMasterCountriesList,
                    languageToApply!!,
                    code
                )?.let {
                    if (list.none { existing ->
                            it.nameCode.equals(
                                existing.nameCode,
                                true
                            )
                        }) list.add(it)
                }
            }
            preferredCountries = if (list.isEmpty()) null else list
        }
    }

    /**
     * Refreshes the custom master country list.
     */
    fun refreshCustomMasterList() {
        val custom = customMasterCountriesParam
        val excluded = excludedCountriesParam

        when {
            !custom.isNullOrEmpty() -> {
                val list = mutableListOf<CCPCountry>()
                custom.split(",").map { it.trim() }.filter { it.isNotEmpty() }.forEach { code ->
                    CCPCountry.getCountryForNameCodeFromLibraryMasterList(
                        context,
                        languageToApply!!,
                        code
                    )?.let {
                        if (list.none { existing ->
                                it.nameCode.equals(
                                    existing.nameCode,
                                    true
                                )
                            }) list.add(it)
                    }
                }
                customMasterCountriesList = if (list.isEmpty()) null else list
            }

            !excluded.isNullOrEmpty() -> {
                val lowerExcluded = excluded.lowercase()
                customMasterCountriesList =
                    CCPCountry.getLibraryMasterCountryList(context, languageToApply!!)
                        .filterNot { lowerExcluded.contains(it.nameCode?.lowercase() ?: "") }
                        .toMutableList()
                        .ifEmpty { null }
            }

            else -> customMasterCountriesList = null
        }
    }

    private fun detectCarrierNumber(fullNumber: String, country: CCPCountry): String {
        val code = country.phoneCode ?: return fullNumber
        return if (fullNumber.startsWith(code)) fullNumber.substring(code.length) else fullNumber
    }

    // Public API for country properties
    val selectedCountryCode: String? get() = selectedCountry?.phoneCode
    val selectedCountryCodeWithPlus: String get() = "+$selectedCountryCode"
    val selectedCountryCodeAsInt: Int get() = selectedCountryCode?.toIntOrNull() ?: 0
    val selectedCountryName: String? get() = selectedCountry?.name
    val selectedCountryNameCode: String get() = selectedCountry?.nameCode ?: ""

    @get:DrawableRes
    val selectedCountryFlagResourceId: Int get() = selectedCountry?.flagResID ?: 0

    fun setCountryForPhoneCode(code: Int) {
        selectedCountry =
            CCPCountry.getCountryForCode(context, languageToApply!!, preferredCountries, code)
                ?: defaultCountry
    }

    fun setCountryForNameCode(nameCode: String?) {
        selectedCountry = CCPCountry.getCountryForNameCodeFromLibraryMasterList(
            context,
            languageToApply!!,
            nameCode
        ) ?: defaultCountry
    }

    /**
     * Full phone number without the leading plus sign.
     * Setting this value updates the selected country and carrier number.
     */
    var fullNumber: String?
        get() = try {
            enteredPhoneNumber?.let {
                phoneUtil.format(it, PhoneNumberUtil.PhoneNumberFormat.E164).substring(1)
            } ?: ""
        } catch (e: Exception) {
            (selectedCountryCode ?: "") + PhoneNumberUtil.normalizeDigitsOnly(
                editTextRegisteredCarrierNumber?.text.toString()
            )
        }
        set(value) {
            val country = CCPCountry.getCountryForNumber(
                context,
                languageToApply!!,
                preferredCountries,
                value
            ) ?: defaultCountry
            selectedCountry = country
            value?.let {
                editTextRegisteredCarrierNumber?.setText(
                    detectCarrierNumber(
                        it,
                        country!!
                    )
                )
            }
            updateFormattingTextWatcher()
        }

    private val enteredPhoneNumber: Phonenumber.PhoneNumber?
        get() = try {
            val text = editTextRegisteredCarrierNumber?.text?.toString()
            if (text.isNullOrBlank()) null else phoneUtil.parse(
                PhoneNumberUtil.normalizeDigitsOnly(
                    text
                ), selectedCountryNameCode
            )
        } catch (e: Exception) {
            null
        }

    /**
     * Checks if the current number entered in the [EditText] is a valid full number.
     */
    val isValidFullNumber: Boolean
        get() {
            val et = editTextRegisteredCarrierNumber
            val country = selectedCCPCountry
            return if (et != null && et.text.isNotEmpty() && country != null) {
                try {
                    val phone = phoneUtil.parse("+${country.phoneCode}${et.text}", country.nameCode)
                    phoneUtil.isValidNumber(phone)
                } catch (e: Exception) {
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

    /**
     * Attempts to detect the country based on SIM, Network, or Locale preferences.
     */
    fun setAutoDetectedCountry(loadDefaultWhenFails: Boolean) {
        try {
            var detected = false
            selectedAutoDetectionPref.representation.forEach {
                detected = when (it) {
                    '1' -> detectSIMCountry()
                    '2' -> detectNetworkCountry()
                    '3' -> detectLocaleCountry()
                    else -> false
                }
                if (detected) return@forEach
                else failureListener?.onCountryAutoDetectionFailed()
            }
            if (!detected && loadDefaultWhenFails) selectedCountry = defaultCountry
        } catch (e: Exception) {
            if (loadDefaultWhenFails) selectedCountry = defaultCountry
        }
    }

    private fun detectSIMCountry(): Boolean = try {
        val tm = context.getSystemService(Context.TELEPHONY_SERVICE) as? TelephonyManager
        val iso = tm?.simCountryIso
        if (iso.isNullOrBlank() || !isNameCodeInCustomMasterList(iso)) false
        else {
            selectedCountry = CCPCountry.getCountryForNameCodeFromLibraryMasterList(
                context,
                languageToApply!!,
                iso
            ); true
        }
    } catch (e: Exception) {
        false
    }

    private fun detectNetworkCountry(): Boolean = try {
        val tm = context.getSystemService(Context.TELEPHONY_SERVICE) as? TelephonyManager
        val iso = tm?.networkCountryIso
        if (iso.isNullOrBlank() || !isNameCodeInCustomMasterList(iso)) false
        else {
            selectedCountry = CCPCountry.getCountryForNameCodeFromLibraryMasterList(
                context,
                languageToApply!!,
                iso
            ); true
        }
    } catch (e: Exception) {
        false
    }

    private fun detectLocaleCountry(): Boolean = try {
        val currentLocale = ConfigurationCompat.getLocales(context.resources.configuration)[0]
        val iso = currentLocale?.country
        if (iso.isNullOrBlank() || !isNameCodeInCustomMasterList(iso)) false
        else {
            selectedCountry = CCPCountry.getCountryForNameCodeFromLibraryMasterList(
                context,
                languageToApply!!,
                iso
            ); true
        }
    } catch (e: Exception) {
        false
    }

    private fun isNameCodeInCustomMasterList(nameCode: String?): Boolean {
        return CCPCountry.getCustomMasterCountryList(context, this)
            ?.any { it.nameCode.equals(nameCode, true) } ?: false
    }

    fun onUserTappedCountry(country: CCPCountry) {
        if (rememberLastSelection) storeSelectedCountryNameCode(country.nameCode)
        selectedCountry = country
    }

    /**
     * Title displayed in the selection dialog.
     */
    val dialogTitle: String?
        get() {
            val default = CCPCountry.getDialogTitle(context, languageToApply!!)
            return customDialogTextProvider?.getCCPDialogTitle(languageToApply, default) ?: default
        }

    /**
     * Hint text for the search field in the dialog.
     */
    val searchHintText: String?
        get() {
            val default = CCPCountry.getSearchHintMessage(context, languageToApply!!)
            return customDialogTextProvider?.getCCPDialogSearchHintText(languageToApply, default)
                ?: default
        }

    /**
     * Message shown when no results are found during search.
     */
    val noResultACK: String?
        get() {
            val default = CCPCountry.getNoResultFoundAckMessage(context, languageToApply!!)
            return customDialogTextProvider?.getCCPDialogNoResultACK(languageToApply, default)
                ?: default
        }

    /**
     * Launches the country selection dialog.
     * @param countryNameCode Optional name code to scroll to initially.
     */
    @JvmOverloads
    fun launchCountrySelectionDialog(countryNameCode: String? = null) {
        CountryCodeDialog.openCountryCodeDialog(this, countryNameCode)
    }

    /**
     * Helper to set a functional listener for country requests.
     */
    fun setOnRequestCountryListener(listener: (String) -> Unit) {
        this.onRequestCountryListener = object : OnRequestCountryListener {
            override fun onRequestCountry(query: String) {
                listener(query)
            }
        }
    }

    override fun onDetachedFromWindow() {
        CountryCodeDialog.clear()
        super.onDetachedFromWindow()
    }

    // Enums and Interfaces
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

    interface OnRequestCountryListener {
        fun onRequestCountry(query: String)
    }

    companion object {
        const val DEFAULT_UNSET: Int = -99
        private const val TAG: String = "CCP"
        const val LIB_DEFAULT_COUNTRY_CODE: Int = 91
        private const val ANDROID_NAME_SPACE = "http://schemas.android.com/apk/res/android"
    }
}

/**
 * Extension to enable GitHub-based country requests.
 */
fun CountryCodePicker.enableGithubCountryRequests() {
    setOnRequestCountryListener { countryName ->
        val title = Uri.encode("Missing Country: $countryName")
        val body = Uri.encode(
            """
            Country searched: $countryName
            
            Additional details:
            """.trimIndent()
        )

        val url = "https://github.com/shashishekhar93/CountryCodePicker/issues/new?title=$title&body=$body"

        val intent = Intent(Intent.ACTION_VIEW, url.toUri()).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }
}

package com.smcoding.countrycodepicker

import android.content.Context
import android.telephony.PhoneNumberUtils
import android.util.Log
import android.text.Editable
import android.text.Selection
import android.text.TextWatcher
import io.michaelrocks.libphonenumber.android.AsYouTypeFormatter
import io.michaelrocks.libphonenumber.android.PhoneNumberUtil

/**
 * TextWatcher that formats phone numbers as they are typed.
 * It uses libphonenumber's [AsYouTypeFormatter] to provide correct formatting based on the selected country.
 */
class InternationalPhoneTextWatcher @JvmOverloads constructor(
    context: Context?,
    countryNameCode: String,
    private var countryPhoneCode: Int,
    private val internationalOnly: Boolean = true
) : TextWatcher {
    
    private val phoneNumberUtil: PhoneNumberUtil = PhoneNumberUtil.createInstance(context)
    private var mFormatter: AsYouTypeFormatter? = null
    private var mSelfChange = false
    private var mStopFormatting = false
    private var needUpdateForCountryChange = false
    private var lastFormatted: Editable? = null

    init {
        require(countryNameCode.isNotEmpty()) { "countryNameCode cannot be empty" }
        updateCountry(countryNameCode, countryPhoneCode)
    }

    /**
     * Updates the formatter for a new country.
     */
    fun updateCountry(countryNameCode: String?, countryPhoneCode: Int) {
        this.countryPhoneCode = countryPhoneCode
        mFormatter = phoneNumberUtil.getAsYouTypeFormatter(countryNameCode)
        mFormatter?.clear()
        
        lastFormatted?.let {
            needUpdateForCountryChange = true
            val onlyDigits = PhoneNumberUtil.normalizeDigitsOnly(it.toString())
            it.replace(0, it.length, onlyDigits, 0, onlyDigits.length)
            needUpdateForCountryChange = false
        }
    }

    override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
        if (mSelfChange || mStopFormatting) return
        // If the user manually deleted any non-dialable characters, stop formatting
        if (count > 0 && hasSeparator(s, start, count) && !needUpdateForCountryChange) {
            stopFormatting()
        }
    }

    override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
        if (mSelfChange || mStopFormatting) return
        // If the user inserted any non-dialable characters, stop formatting
        if (count > 0 && hasSeparator(s, start, count)) {
            stopFormatting()
        }
    }

    override fun afterTextChanged(s: Editable) {
        if (mStopFormatting) {
            mStopFormatting = s.isNotEmpty()
            return
        }
        if (mSelfChange) return

        val selectionEnd = Selection.getSelectionEnd(s)
        val isCursorAtEnd = (selectionEnd == s.length)

        val formatted = reformat(s)

        var finalCursorPosition = 0
        if (formatted == s.toString()) {
            finalCursorPosition = selectionEnd
        } else if (isCursorAtEnd) {
            finalCursorPosition = formatted.length
        } else {
            // Find the position of the digit that was before the cursor
            var digitsBeforeCursor = 0
            for (i in 0 until selectionEnd) {
                if (PhoneNumberUtils.isNonSeparator(s[i])) digitsBeforeCursor++
            }

            var digitPassed = 0
            for (i in formatted.indices) {
                if (digitPassed == digitsBeforeCursor) {
                    finalCursorPosition = i
                    break
                }
                if (PhoneNumberUtils.isNonSeparator(formatted[i])) digitPassed++
            }
        }

        // Avoid placing cursor right before a separator
        if (!isCursorAtEnd) {
            while (finalCursorPosition > 0 && !PhoneNumberUtils.isNonSeparator(formatted[finalCursorPosition - 1])) {
                finalCursorPosition--
            }
        }

        try {
            mSelfChange = true
            s.replace(0, s.length, formatted, 0, formatted.length)
            mSelfChange = false
            lastFormatted = s
            Selection.setSelection(s, finalCursorPosition)
        } catch (e: Exception) {
            Log.e(TAG, "Error in afterTextChanged", e)
        }
    }

    private fun reformat(s: CharSequence): String {
        var sequence = s
        mFormatter?.clear()
        
        val countryCallingCode = "+$countryPhoneCode"
        val shouldAddCode = internationalOnly || (sequence.isNotEmpty() && sequence[0] != '0')
        
        if (shouldAddCode) sequence = countryCallingCode + sequence
        
        var formatted = ""
        var lastChar = 0.toChar()

        for (c in sequence) {
            if (PhoneNumberUtils.isNonSeparator(c)) {
                if (lastChar.code != 0) formatted = mFormatter!!.inputDigit(lastChar)
                lastChar = c
            }
        }
        if (lastChar.code != 0) formatted = mFormatter!!.inputDigit(lastChar)

        formatted = formatted.trim()
        
        if (shouldAddCode) {
            if (formatted.length > countryCallingCode.length) {
                val offset = if (formatted.getOrNull(countryCallingCode.length) == ' ') 1 else 0
                formatted = formatted.substring(countryCallingCode.length + offset)
            } else {
                formatted = ""
            }
        }
        
        return formatted
    }

    private fun stopFormatting() {
        mStopFormatting = true
        mFormatter?.clear()
    }

    private fun hasSeparator(s: CharSequence, start: Int, count: Int): Boolean {
        for (i in start until start + count) {
            if (!PhoneNumberUtils.isNonSeparator(s[i])) return true
        }
        return false
    }

    companion object {
        private const val TAG = "IntlPhoneTextWatcher"
    }
}

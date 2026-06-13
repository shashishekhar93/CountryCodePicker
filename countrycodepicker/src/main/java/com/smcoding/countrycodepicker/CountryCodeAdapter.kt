package com.smcoding.countrycodepicker

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import com.smcoding.countrycodepicker.databinding.LayoutRecyclerCountryTileBinding
import com.smcoding.countrycodepicker.recyclerview_listeners.SectionTitleProvider
import java.util.Locale

/**
 * Adapter for the country selection RecyclerView.
 * Handles filtering countries based on search queries and displaying preferred countries at the top.
 */
internal class CountryCodeAdapter(
    private val context: Context,
    countries: List<CCPCountry>?,
    private val codePicker: CountryCodePicker,
    private val rlQueryHolder: RelativeLayout,
    private val editTextSearch: EditText?,
    private val textViewNoResult: TextView,
    private val dialog: Dialog,
    private val imgClearQuery: ImageView
) : RecyclerView.Adapter<CountryCodeAdapter.CountryCodeViewHolder>(), SectionTitleProvider {

    private var filteredCountries: MutableList<CCPCountry?> = mutableListOf()
    private val masterCountries: List<CCPCountry>? = countries
    private val inflater: LayoutInflater = LayoutInflater.from(context)
    private var preferredCountriesCount: Int = 0

    init {
        applyQuery("")
        setSearchBar()
    }

    /**
     * Shows or hides the search bar based on codePicker settings.
     */
    private fun setSearchBar() {
        if (codePicker.isSearchAllowed) {
            imgClearQuery.visibility = View.GONE
            setTextWatcher()
            setQueryClearListener()
        } else {
            rlQueryHolder.visibility = View.GONE
        }
    }

    private fun setQueryClearListener() {
        imgClearQuery.setOnClickListener {
            editTextSearch?.setText("")
        }
    }

    private fun setTextWatcher() {
        editTextSearch?.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                applyQuery(s.toString())
                imgClearQuery.visibility = if (s.isBlank()) View.GONE else View.VISIBLE
            }
        })

        // Hide keyboard when search button is pressed on soft keyboard
        editTextSearch?.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(editTextSearch.windowToken, 0)
                true
            } else {
                false
            }
        }
    }

    /**
     * Filters the country list based on the search query.
     */
    @SuppressLint("NotifyDataSetChanged")
    private fun applyQuery(query: String) {
        textViewNoResult.visibility = View.GONE
        var cleanQuery = query.lowercase(Locale.getDefault())

        if (cleanQuery.isNotEmpty() && cleanQuery[0] == '+') {
            cleanQuery = cleanQuery.substring(1)
        }

        filteredCountries = getFilteredCountries(cleanQuery)

        if (filteredCountries.isEmpty()) {
            textViewNoResult.visibility = View.VISIBLE
        }
        notifyDataSetChanged()
    }

    /**
     * Helper to filter master and preferred countries.
     */
    private fun getFilteredCountries(query: String): MutableList<CCPCountry?> {
        val tempCCPCountryList = mutableListOf<CCPCountry?>()
        preferredCountriesCount = 0

        // Add matching preferred countries first
        codePicker.preferredCountries?.let { preferred ->
            for (country in preferred) {
                if (country.isEligibleForQuery(query)) {
                    tempCCPCountryList.add(country)
                    preferredCountriesCount++
                }
            }

            // Add a null divider if any preferred countries matched
            if (tempCCPCountryList.isNotEmpty()) {
                tempCCPCountryList.add(null)
                preferredCountriesCount++
            }
        }

        // Add matching countries from the main list
        masterCountries?.let { master ->
            for (country in master) {
                if (country.isEligibleForQuery(query)) {
                    tempCCPCountryList.add(country)
                }
            }
        }
        return tempCCPCountryList
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): CountryCodeViewHolder {
        val binding = LayoutRecyclerCountryTileBinding.inflate(inflater, viewGroup, false)
        return CountryCodeViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CountryCodeViewHolder, position: Int) {
        val country = filteredCountries[position]
        holder.setCountry(country)
        
        if (country != null) {
            holder.itemView.setOnClickListener { view ->
                codePicker.onUserTappedCountry(country)
                val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(view.windowToken, 0)
                dialog.dismiss()
            }
        } else {
            holder.itemView.setOnClickListener(null)
        }
    }

    override fun getItemCount(): Int = filteredCountries.size

    /**
     * Provides a section title for the fast scroller.
     */
    override fun getSectionTitle(position: Int): String {
        val ccpCountry = filteredCountries[position]
        return when {
            preferredCountriesCount > position -> "★"
            ccpCountry != null -> ccpCountry.name?.firstOrNull()?.toString() ?: ""
            else -> "☺"
        }
    }

    /**
     * ViewHolder class for country item rows.
     */
    internal inner class CountryCodeViewHolder(val binding: LayoutRecyclerCountryTileBinding) :
        RecyclerView.ViewHolder(binding.root) {
        private val textViewName: TextView = binding.textViewCountryName
        private val textViewCode: TextView = binding.textViewCode
        private val imageViewFlag: ImageView = binding.imageFlag
        private val linearFlagHolder: LinearLayout = binding.linearFlagHolder
        private val divider: View = binding.preferenceDivider

        init {
            applyCustomStyling()
        }

        private fun applyCustomStyling() {
            if (codePicker.dialogTextColor != 0) {
                val color = codePicker.dialogTextColor
                textViewName.setTextColor(color)
                textViewCode.setTextColor(color)
                divider.setBackgroundColor(color)
            }

            if (codePicker.ccpDialogRippleEnable) {
                val outValue = TypedValue()
                context.theme.resolveAttribute(
                    android.R.attr.selectableItemBackground,
                    outValue,
                    true
                )
                binding.root.setBackgroundResource(if (outValue.resourceId != 0) outValue.resourceId else outValue.data)
            }

            codePicker.dialogTypeFace?.let { typeFace ->
                if (codePicker.dialogTypeFaceStyle != CountryCodePicker.DEFAULT_UNSET) {
                    textViewCode.setTypeface(typeFace, codePicker.dialogTypeFaceStyle)
                    textViewName.setTypeface(typeFace, codePicker.dialogTypeFaceStyle)
                } else {
                    textViewCode.typeface = typeFace
                    textViewName.typeface = typeFace
                }
            }
        }

        fun setCountry(ccpCountry: CCPCountry?) {
            val imgCheck: ImageView = binding.imgCheck
            if (ccpCountry != null) {
                divider.visibility = View.GONE
                textViewName.visibility = View.VISIBLE
                textViewCode.visibility =
                    if (codePicker.isCcpDialogShowPhoneCode) View.VISIBLE else View.GONE

                var countryName = ccpCountry.name ?: ""
                if (codePicker.showNameCode) {
                    countryName += " (${ccpCountry.nameCode?.uppercase()})"
                }

                textViewName.text = countryName
                textViewCode.text =
                    context.getString(R.string.ccp_phone_code_format, ccpCountry.phoneCode)

                if (!codePicker.ccpDialogShowFlag || codePicker.ccpUseEmoji) {
                    linearFlagHolder.visibility = View.GONE
                } else {
                    linearFlagHolder.visibility = View.VISIBLE
                    imageViewFlag.setImageResource(ccpCountry.flagID)
                }

                // Selection logic
                val isSelected =
                    ccpCountry.nameCode.equals(codePicker.selectedCCPCountry?.nameCode, true)
                imgCheck.visibility = if (isSelected) View.VISIBLE else View.GONE
                if (isSelected) {
                    textViewName.typeface = ResourcesCompat.getFont(context, R.font.outfit_bold)
                    textViewName.setTextColor(
                        ContextCompat.getColor(
                            context,
                            R.color.ccp_dialog_accent
                        )
                    )
                } else {
                    textViewName.typeface = ResourcesCompat.getFont(context, R.font.outfit_medium)
                    textViewName.setTextColor(
                        ContextCompat.getColor(
                            context,
                            R.color.ccp_dialog_text_primary
                        )
                    )
                }
            } else {
                // Divider row
                divider.visibility = View.VISIBLE
                textViewName.visibility = View.GONE
                textViewCode.visibility = View.GONE
                linearFlagHolder.visibility = View.GONE
                imgCheck.visibility = View.GONE
            }
        }
    }
}

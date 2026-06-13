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
import androidx.recyclerview.widget.RecyclerView
import com.smcoding.countrycodepicker.recyclerviewfastscroll.SectionTitleProvider
import java.util.Locale
internal class CountryCodeAdapter(
    var context: Context,
    countries: MutableList<CCPCountry>?,
    var codePicker: CountryCodePicker,
    var rlQueryHolder: RelativeLayout,
    var editTextSearch: EditText?,
    var textViewNoResult: TextView,
    var dialog: Dialog,
    var imgClearQuery: ImageView
) : RecyclerView.Adapter<CountryCodeAdapter.CountryCodeViewHolder>(), SectionTitleProvider {

    var filteredCountries: MutableList<CCPCountry?> = mutableListOf()
    var masterCountries: MutableList<CCPCountry>? = countries
    var inflater: LayoutInflater = LayoutInflater.from(context)
    var preferredCountriesCount: Int = 0

    init {
        applyQuery("")
        setSearchBar()
    }

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
                imgClearQuery.visibility = if (s.toString().trim().isEmpty()) View.GONE else View.VISIBLE
            }
        })

        editTextSearch?.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(editTextSearch?.windowToken, 0)
                true
            } else {
                false
            }
        }
    }

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

    private fun getFilteredCountries(query: String): MutableList<CCPCountry?> {
        val tempCCPCountryList = mutableListOf<CCPCountry?>()
        preferredCountriesCount = 0
        codePicker.preferredCountries?.let { preferredCountries ->
            for (country in preferredCountries) {
                if (country.isEligibleForQuery(query)) {
                    tempCCPCountryList.add(country)
                    preferredCountriesCount++
                }
            }

            if (tempCCPCountryList.isNotEmpty()) {
                tempCCPCountryList.add(null) // Divider
                preferredCountriesCount++
            }
        }

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
        val rootView = inflater.inflate(R.layout.layout_recycler_country_tile, viewGroup, false)
        return CountryCodeViewHolder(rootView)
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

    override fun getSectionTitle(position: Int): String {
        val ccpCountry = filteredCountries[position]
        return when {
            preferredCountriesCount > position -> "★"
            ccpCountry != null -> ccpCountry.name?.substring(0, 1) ?: ""
            else -> "☺"
        }
    }

    internal inner class CountryCodeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val relativeLayoutMain: RelativeLayout = itemView as RelativeLayout
        val textViewName: TextView = itemView.findViewById(R.id.textView_countryName)
        val textViewCode: TextView = itemView.findViewById(R.id.textView_code)
        val imageViewFlag: ImageView = itemView.findViewById(R.id.image_flag)
        val linearFlagHolder: LinearLayout = itemView.findViewById(R.id.linear_flag_holder)
        val divider: View = itemView.findViewById(R.id.preferenceDivider)

        init {
            if (codePicker.dialogTextColor != 0) {
                textViewName.setTextColor(codePicker.dialogTextColor)
                textViewCode.setTextColor(codePicker.dialogTextColor)
                divider.setBackgroundColor(codePicker.dialogTextColor)
            }

            if (codePicker.ccpDialogRippleEnable) {
                val outValue = TypedValue()
                context.theme.resolveAttribute(android.R.attr.selectableItemBackground, outValue, true)
                if (outValue.resourceId != 0) relativeLayoutMain.setBackgroundResource(outValue.resourceId)
                else relativeLayoutMain.setBackgroundResource(outValue.data)
            }

            codePicker.getDialogTypeFace()?.let { typeFace ->
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
            if (ccpCountry != null) {
                divider.visibility = View.GONE
                textViewName.visibility = View.VISIBLE
                textViewCode.visibility = if (codePicker.isCcpDialogShowPhoneCode) View.VISIBLE else View.GONE

                var countryName = ""
                if (codePicker.ccpDialogShowFlag && codePicker.ccpUseEmoji) {
                    countryName += CCPCountry.getFlagEmoji(ccpCountry) + "   "
                }
                countryName += ccpCountry.name
                if (codePicker.showNameCode) {
                    countryName += " (" + ccpCountry.getNameCode().uppercase() + ")"
                }

                textViewName.text = countryName
                textViewCode.text = "+${ccpCountry.phoneCode}"

                if (!codePicker.ccpDialogShowFlag || codePicker.ccpUseEmoji) {
                    linearFlagHolder.visibility = View.GONE
                } else {
                    linearFlagHolder.visibility = View.VISIBLE
                    imageViewFlag.setImageResource(ccpCountry.flagID)
                }
            } else {
                divider.visibility = View.VISIBLE
                textViewName.visibility = View.GONE
                textViewCode.visibility = View.GONE
                linearFlagHolder.visibility = View.GONE
            }
        }
    }
}

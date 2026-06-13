package com.smcoding.countrycodepicker

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.graphics.drawable.DrawableCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.smcoding.countrycodepicker.databinding.LayoutPickerDialogBinding
import com.smcoding.countrycodepicker.recyclerview_listeners.FastScroller

/**
 * Singleton object that handles the creation and display of the country selection dialog.
 */
internal object CountryCodeDialog {
    private var dialog: Dialog? = null

    /**
     * Opens the country selection dialog.
     * @param codePicker The [CountryCodePicker] instance that initiated the dialog.
     * @param countryNameCode Optional name code to scroll to initially.
     */
    @JvmOverloads
    fun openCountryCodeDialog(codePicker: CountryCodePicker, countryNameCode: String? = null) {
        val context = codePicker.context
        val binding = LayoutPickerDialogBinding.inflate(LayoutInflater.from(context))
        val dialogInstance = Dialog(context, R.style.CCPDialogTheme).apply {
            setContentView(binding.root)
            window?.setLayout(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT
            )
        }
        dialog = dialogInstance

        val masterCountries = CCPCountry.getCustomMasterCountryList(context, codePicker)

        // Initialize views
        val recyclerView = binding.recyclerCountryDialog
        val textViewTitle = binding.textViewTitle
        val rlQueryHolder = binding.rlQueryHolder
        val imgClearQuery = binding.imgClearQuery
        val editTextSearch = binding.editTextSearch
        val textViewNoResult = binding.textViewNoresult
        val dialogRoot = binding.cardViewRoot
        val imgDismiss = binding.imgDismiss
        val imgClose = binding.imgClose

        setupKeyboard(codePicker, editTextSearch, dialogInstance)
        setupTypefaces(codePicker, textViewTitle, editTextSearch, textViewNoResult)
        setupStyling(
            codePicker,
            dialogRoot,
            textViewTitle,
            imgClearQuery,
            imgDismiss,
            imgClose,
            editTextSearch,
            textViewNoResult
        )

        // Set messages from codePicker
        textViewTitle.text = codePicker.dialogTitle
        editTextSearch.hint = codePicker.searchHintText
        textViewNoResult.text = codePicker.noResultACK

        // Compact height if search is disabled
        if (!codePicker.isSearchAllowed) {
            recyclerView.layoutParams =
                (recyclerView.layoutParams as androidx.constraintlayout.widget.ConstraintLayout.LayoutParams).apply {
                    height = RecyclerView.LayoutParams.WRAP_CONTENT
                }
        }

        // Initialize Adapter
        val adapter = CountryCodeAdapter(
            context, masterCountries, codePicker, rlQueryHolder,
            editTextSearch, textViewNoResult, dialogInstance, imgClearQuery
        )
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = adapter

        setupFastScroller(codePicker, binding.fastscroll, recyclerView)

        // Event listeners
        dialogInstance.setOnDismissListener {
            hideKeyboard(context)
            codePicker.dialogEventsListener?.onCcpDialogDismiss(it)
        }
        dialogInstance.setOnCancelListener {
            hideKeyboard(context)
            codePicker.dialogEventsListener?.onCcpDialogCancel(it)
        }

        scrollToCountry(codePicker, countryNameCode, masterCountries, recyclerView)

        dialogInstance.show()
        codePicker.dialogEventsListener?.onCcpDialogOpen(dialogInstance)
    }

    private fun setupKeyboard(
        codePicker: CountryCodePicker,
        editTextSearch: EditText,
        dialog: Dialog
    ) {
        if (codePicker.isSearchAllowed && codePicker.isDialogKeyboardAutoPopup) {
            editTextSearch.requestFocus()
            dialog.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)
        } else {
            dialog.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN)
        }
    }

    private fun setupTypefaces(codePicker: CountryCodePicker, vararg textViews: TextView) {
        codePicker.dialogTypeFace?.let { typeFace ->
            textViews.forEach { tv ->
                if (codePicker.dialogTypeFaceStyle != CountryCodePicker.DEFAULT_UNSET) {
                    tv.setTypeface(typeFace, codePicker.dialogTypeFaceStyle)
                } else {
                    tv.typeface = typeFace
                }
            }
        }
    }

    private fun setupStyling(
        cp: CountryCodePicker, root: View, title: TextView,
        clear: ImageView, dismiss: ImageView, close: ImageView, search: EditText, noResult: TextView
    ) {
        if (root is CardView) {
            if (cp.dialogBackgroundColor != 0) root.setCardBackgroundColor(cp.dialogBackgroundColor)
            root.radius = cp.dialogCornerRadius
        } else {
            if (cp.dialogBackgroundColor != 0) root.setBackgroundColor(cp.dialogBackgroundColor)
        }
        if (cp.dialogBackgroundResId != 0) root.setBackgroundResource(cp.dialogBackgroundResId)

        dismiss.visibility = View.VISIBLE
        dismiss.setOnClickListener { dialog?.dismiss() }

        if (cp.isShowCloseIcon) {
            close.visibility = View.VISIBLE
            close.setOnClickListener { dialog?.dismiss() }
        } else {
            close.visibility = View.GONE
        }

        title.visibility = if (cp.ccpDialogShowTitle) View.VISIBLE else View.GONE

        if (cp.dialogTextColor != 0) {
            val color = cp.dialogTextColor
            val colorStateList = ColorStateList.valueOf(color)
            listOf(clear, dismiss, close).forEach { it.imageTintList = colorStateList }
            listOf(title, noResult, search).forEach { it.setTextColor(color) }
            search.setHintTextColor(
                Color.argb(
                    100,
                    Color.red(color),
                    Color.green(color),
                    Color.blue(color)
                )
            )
        }

        if (cp.dialogSearchEditTextTintColor != 0) {
            search.backgroundTintList = ColorStateList.valueOf(cp.dialogSearchEditTextTintColor)
            setCursorColor(search, cp.dialogSearchEditTextTintColor)
        }
    }

    private fun setupFastScroller(
        cp: CountryCodePicker,
        fastScroller: FastScroller,
        rv: RecyclerView
    ) {
        fastScroller.setRecyclerView(rv)
        if (cp.isShowFastScroller) {
            if (cp.fastScrollerBubbleColor != 0) fastScroller.setBubbleColor(cp.fastScrollerBubbleColor)
            if (cp.fastScrollerHandleColor != 0) fastScroller.setHandleColor(cp.fastScrollerHandleColor)
            if (cp.fastScrollerBubbleTextAppearance != 0) {
                try {
                    fastScroller.setBubbleTextAppearance(cp.fastScrollerBubbleTextAppearance)
                } catch (ignored: Exception) {
                }
            }
        } else {
            fastScroller.visibility = View.GONE
        }
    }

    private fun scrollToCountry(
        cp: CountryCodePicker,
        nameCode: String?,
        countries: List<CCPCountry>?,
        rv: RecyclerView
    ) {
        if (nameCode == null) return
        val isPreferred = cp.preferredCountries?.any { it.nameCode.equals(nameCode, true) } ?: false
        if (!isPreferred) {
            val offset = cp.preferredCountries?.let { if (it.isNotEmpty()) it.size + 1 else 0 } ?: 0
            countries?.indexOfFirst { it.nameCode.equals(nameCode, true) }?.let { index ->
                if (index != -1) rv.scrollToPosition(index + offset)
            }
        }
    }

    private fun hideKeyboard(context: Context) {
        if (context is Activity) {
            val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            val view = context.currentFocus ?: View(context)
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }

    private fun setCursorColor(editText: EditText, color: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val drawable = editText.textCursorDrawable
            drawable?.let {
                val wrappedDrawable = DrawableCompat.wrap(it)
                DrawableCompat.setTint(wrappedDrawable, color)
                editText.textCursorDrawable = wrappedDrawable
            }
        }
    }

    fun clear() {
        dialog?.dismiss()
        dialog = null
    }
}

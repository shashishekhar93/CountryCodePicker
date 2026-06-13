package com.smcoding.countrycodepicker

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import android.os.Build
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.smcoding.countrycodepicker.recyclerviewfastscroll.FastScroller
import java.lang.reflect.Field

internal object CountryCodeDialog {
    private val sEditorField: Field?
    private val sCursorDrawableField: Field?
    private val sCursorDrawableResourceField: Field?
    private var dialog: Dialog? = null

    init {
        var editorField: Field? = null
        var cursorDrawableField: Field? = null
        var cursorDrawableResourceField: Field? = null
        var exceptionThrown = false
        try {
            cursorDrawableResourceField = TextView::class.java.getDeclaredField("mCursorDrawableRes")
            cursorDrawableResourceField.isAccessible = true
            editorField = TextView::class.java.getDeclaredField("mEditor")
            editorField.isAccessible = true
            val drawableFieldClass = editorField.type
            cursorDrawableField = drawableFieldClass.getDeclaredField("mCursorDrawable")
            cursorDrawableField.isAccessible = true
        } catch (e: Exception) {
            exceptionThrown = true
        }
        if (exceptionThrown) {
            sEditorField = null
            sCursorDrawableField = null
            sCursorDrawableResourceField = null
        } else {
            sEditorField = editorField
            sCursorDrawableField = cursorDrawableField
            sCursorDrawableResourceField = cursorDrawableResourceField
        }
    }

    @JvmOverloads
    fun openCountryCodeDialog(codePicker: CountryCodePicker, countryNameCode: String? = null) {
        val context = codePicker.context
        val dialogInstance = Dialog(context)
        dialog = dialogInstance

        val masterCountries = CCPCountry.getCustomMasterCountryList(context, codePicker)
        dialogInstance.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialogInstance.setContentView(R.layout.layout_picker_dialog)
        dialogInstance.window?.setBackgroundDrawable(ContextCompat.getDrawable(context, android.R.color.transparent))

        //dialog views
        val recyclerView: RecyclerView = dialogInstance.findViewById(R.id.recycler_countryDialog)
        val textViewTitle: TextView = dialogInstance.findViewById(R.id.textView_title)
        val rlQueryHolder: RelativeLayout = dialogInstance.findViewById(R.id.rl_query_holder)
        val imgClearQuery: ImageView = dialogInstance.findViewById(R.id.img_clear_query)
        val editTextSearch: EditText = dialogInstance.findViewById(R.id.editText_search)
        val textViewNoResult: TextView = dialogInstance.findViewById(R.id.textView_noresult)
        val dialogRoot: CardView = dialogInstance.findViewById(R.id.cardViewRoot)
        val imgDismiss: ImageView = dialogInstance.findViewById(R.id.img_dismiss)

        //keyboard
        if (codePicker.isSearchAllowed && codePicker.isDialogKeyboardAutoPopup) {
            editTextSearch.requestFocus()
            dialogInstance.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)
        } else {
            dialogInstance.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN)
        }

        // type faces
        try {
            codePicker.getDialogTypeFace()?.let { typeFace ->
                if (codePicker.dialogTypeFaceStyle != CountryCodePicker.DEFAULT_UNSET) {
                    textViewNoResult.setTypeface(typeFace, codePicker.dialogTypeFaceStyle)
                    editTextSearch.setTypeface(typeFace, codePicker.dialogTypeFaceStyle)
                    textViewTitle.setTypeface(typeFace, codePicker.dialogTypeFaceStyle)
                } else {
                    textViewNoResult.typeface = typeFace
                    editTextSearch.typeface = typeFace
                    textViewTitle.typeface = typeFace
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        //dialog background color
        if (codePicker.dialogBackgroundColor != 0) {
            dialogRoot.setCardBackgroundColor(codePicker.dialogBackgroundColor)
        }

        if (codePicker.dialogBackgroundResId != 0) {
            dialogRoot.setBackgroundResource(codePicker.dialogBackgroundResId)
        }

        dialogRoot.radius = codePicker.dialogCornerRadius

        //close button visibility
        if (codePicker.isShowCloseIcon) {
            imgDismiss.visibility = View.VISIBLE
            imgDismiss.setOnClickListener { dialogInstance.dismiss() }
        } else {
            imgDismiss.visibility = View.GONE
        }

        //title
        textViewTitle.visibility = if (codePicker.ccpDialogShowTitle) View.VISIBLE else View.GONE

        //clear button color and title color
        if (codePicker.dialogTextColor != 0) {
            val textColor = codePicker.dialogTextColor
            imgClearQuery.setColorFilter(textColor)
            imgDismiss.setColorFilter(textColor)
            textViewTitle.setTextColor(textColor)
            textViewNoResult.setTextColor(textColor)
            editTextSearch.setTextColor(textColor)
            editTextSearch.setHintTextColor(
                Color.argb(
                    100,
                    Color.red(textColor),
                    Color.green(textColor),
                    Color.blue(textColor)
                )
            )
        }

        //editText tint
        if (codePicker.dialogSearchEditTextTintColor != 0) {
            editTextSearch.backgroundTintList = ColorStateList.valueOf(codePicker.dialogSearchEditTextTintColor)
            setCursorColor(editTextSearch, codePicker.dialogSearchEditTextTintColor)
        }

        //add messages to views
        textViewTitle.text = codePicker.dialogTitle
        editTextSearch.hint = codePicker.searchHintText
        textViewNoResult.text = codePicker.noResultACK

        //this will make dialog compact
        if (!codePicker.isSearchAllowed) {
            val params = recyclerView.layoutParams as RelativeLayout.LayoutParams
            params.height = RecyclerView.LayoutParams.WRAP_CONTENT
            recyclerView.layoutParams = params
        }

        val adapter = CountryCodeAdapter(
            context,
            masterCountries,
            codePicker,
            rlQueryHolder,
            editTextSearch,
            textViewNoResult,
            dialogInstance,
            imgClearQuery
        )
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = adapter

        //fast scroller
        val fastScroller: FastScroller = dialogInstance.findViewById(R.id.fastscroll)
        fastScroller.setRecyclerView(recyclerView)
        if (codePicker.isShowFastScroller) {
            if (codePicker.fastScrollerBubbleColor != 0) {
                fastScroller.setBubbleColor(codePicker.fastScrollerBubbleColor)
            }
            if (codePicker.fastScrollerHandleColor != 0) {
                fastScroller.setHandleColor(codePicker.fastScrollerHandleColor)
            }
            if (codePicker.fastScrollerBubbleTextAppearance != 0) {
                try {
                    fastScroller.setBubbleTextAppearance(codePicker.fastScrollerBubbleTextAppearance)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        } else {
            fastScroller.visibility = View.GONE
        }

        dialogInstance.setOnDismissListener { dialogInterface ->
            hideKeyboard(codePicker.context)
            codePicker.dialogEventsListener?.onCcpDialogDismiss(dialogInterface)
        }

        dialogInstance.setOnCancelListener { dialogInterface ->
            hideKeyboard(codePicker.context)
            codePicker.dialogEventsListener?.onCcpDialogCancel(dialogInterface)
        }

        //auto scroll to mentioned countryNameCode
        if (countryNameCode != null) {
            val isPreferredCountry = codePicker.preferredCountries?.any { it.nameCode.equals(countryNameCode, ignoreCase = true) } ?: false

            if (!isPreferredCountry) {
                var preferredCountriesOffset = 0
                codePicker.preferredCountries?.let {
                    if (it.isNotEmpty()) {
                        preferredCountriesOffset = it.size + 1 //+1 is for divider
                    }
                }
                masterCountries?.let { countries ->
                    for (i in countries.indices) {
                        if (countries[i].nameCode.equals(countryNameCode, ignoreCase = true)) {
                            recyclerView.scrollToPosition(i + preferredCountriesOffset)
                            break
                        }
                    }
                }
            }
        }

        dialogInstance.show()
        codePicker.dialogEventsListener?.onCcpDialogOpen(dialogInstance)
    }

    private fun hideKeyboard(context: Context?) {
        if (context is Activity) {
            val imm = context.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
            var view = context.currentFocus
            if (view == null) {
                view = View(context)
            }
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }

    private fun setCursorColor(editText: EditText, color: Int) {
        if (sCursorDrawableField == null || sCursorDrawableResourceField == null || sEditorField == null) {
            return
        }
        try {
            val drawable = AppCompatResources.getDrawable(editText.context, sCursorDrawableResourceField.getInt(editText))
            drawable?.let {
                it.setColorFilter(color, PorterDuff.Mode.SRC_IN)
                sCursorDrawableField.set(sEditorField.get(editText), arrayOf<Drawable>(it, it))
            }
        } catch (ignored: Exception) {
        }
    }

    fun clear() {
        dialog?.dismiss()
        dialog = null
    }
}

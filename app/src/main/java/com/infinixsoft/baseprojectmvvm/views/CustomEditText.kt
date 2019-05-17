package com.infinixsoft.baseprojectmvvm.views

import android.content.Context
import android.support.design.widget.TextInputEditText
import android.support.design.widget.TextInputLayout
import android.text.TextUtils
import android.util.AttributeSet
import android.view.View.OnFocusChangeListener
import com.infinixsoft.baseprojectmvvm.R


/**
 * Created by Franco on 17/01/2019.
 */
class CustomEditText : TextInputEditText {
    var misEmailInput = false
    init {
        onFocusChangeListener = OnFocusChangeListener { v, hasFocus ->
            if (!hasFocus)
                isValid()
            else {
                val textInputLayout = parent.parent as TextInputLayout
                textInputLayout.error = null
                error = null
                // cleanError()
//                    mInputLabel?.setBackgroundResource(R.drawable.bg_gray_border)
            }
        }
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {

    }

    constructor(context: Context) : super(context) {

    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {

    }

    fun isValid(): Boolean {
        val textInputLayout = parent.parent as TextInputLayout
        error = null
//        mError.visibility = View.GONE
//        mInputLabel?.setBackgroundResource(R.drawable.bg_gray_border)
        textInputLayout.error = null
        if (text.toString().isBlank()) {
            textInputLayout.error = "Complete field"
            error = ""
            return false
        }
        if (misEmailInput && !isValidEmail(text.toString())) {
            textInputLayout.error = context.getString(R.string.error_invalid_email)
            error = ""
            return false
        }
        return true
    }
    companion object {
        fun isValidEmail(target: CharSequence): Boolean {
            return if (TextUtils.isEmpty(target)) {
                false
            } else {
                android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches()
            }
        }
    }
}
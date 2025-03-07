package com.mshetty.tracksearch.search.searchview

import android.content.Context
import android.content.res.Configuration
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.EditorInfo
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import com.mshetty.tracksearch.R
import com.mshetty.tracksearch.databinding.LayoutSearchViewBinding
import com.mshetty.tracksearch.search.searchview.Utils.hideKeyboard
import com.mshetty.tracksearch.search.searchview.Utils.showKeyboard

class CustomSearchView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr), View.OnClickListener {

    private var isOpen = false
    private var clearingFocus = false
    private var currentQuery: CharSequence? = null
    private var onTextChangeListener: OnTextChangeListener? = null

    private var binding: LayoutSearchViewBinding? = null

    private fun init() {
        binding = LayoutSearchViewBinding.inflate(LayoutInflater.from(context), this, true)
        binding?.actionBack?.setOnClickListener(this)
        binding?.actionClear?.setOnClickListener(this)
        initSearchView()
    }

    init {
        init()
    }

    private fun initSearchView() {
        binding?.etSearch?.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                onSubmitQuery()
                true
            } else {
                false
            }
        }

        binding?.etSearch?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                if (s.toString().isEmpty()) {
                  binding?.actionClear?.isVisible = false
                } else {
                    binding?.actionClear?.isVisible = true
                }
                onTextChangeListener?.onTextChanged(s.toString())
            }

            override fun afterTextChanged(s: Editable) {}
        })

        binding?.etSearch?.setOnFocusChangeListener { view, hasFocus ->
            if (hasFocus && !isHardKeyboardAvailable) {
                showKeyboard(view)
            }
        }
    }

    private val isHardKeyboardAvailable: Boolean
        get() = context.resources.configuration.keyboard != Configuration.KEYBOARD_NOKEYS

    private fun closeSearch() {
        if (!isOpen) {
            return
        }
        binding?.etSearch?.setText("")
        clearFocus()
        isOpen = false
    }

    private fun onSubmitQuery() {
        val query = binding?.etSearch?.text
        if (query != null && TextUtils.getTrimmedLength(query) > 0) {
            onTextChangeListener?.onQuerySubmit(query.toString())
            closeSearch()
            binding?.etSearch?.setText("")
        }
    }

    fun setQuery(query: CharSequence?) {
        binding?.etSearch?.setText(query)
        if (query != null) {
            binding?.etSearch?.setSelection(binding?.etSearch?.length() ?: 0)
            currentQuery = query
        }
    }

    fun setOnTextChangeListener(onTextChangeListener: OnTextChangeListener) {
        this.onTextChangeListener = onTextChangeListener
    }

    override fun clearFocus() {
        clearingFocus = true
        hideKeyboard(this@CustomSearchView)
        super.clearFocus()
        binding?.etSearch?.clearFocus()
        clearingFocus = false
    }

    interface OnTextChangeListener {
        fun onTextChanged(s: String)
        fun onQuerySubmit(s: String)
        fun onBackPressed()
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.action_back -> {
                closeSearch()
                onTextChangeListener?.onBackPressed()
            }
            R.id.action_clear -> {
                binding?.etSearch?.setText("")
            }
        }
    }
}
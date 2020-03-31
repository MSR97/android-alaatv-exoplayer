package com.alaatv.component.view

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import com.alaatv.component.R
import com.alaatv.font.FontFace


/*
    this Custom view is for Title Headers on top of Recycler views .
 */

class HeaderWithLine(context: Context, attrs: AttributeSet) : ConstraintLayout(context, attrs) {


    private var _text: String? = resources.getString(R.string.app_name)
    private var _textColor: Int = ContextCompat.getColor(context, R.color.pureWhite)
    private var _textSize: Float = resources.getDimension(R.dimen.text_size_default)
    private var _textFont: Int = 2
    private var _textButton: String? = resources.getString(R.string.app_name)
    private var _colorButton: Int = ContextCompat.getColor(context, R.color.smokyWhite)
    private var _hasButton: Boolean = false
    private var textView: TextView
    private var button: Button
    private var clickListener: OnClickListener? = null

    init {
        View.inflate(context, R.layout.header_with_line, this)

        textView = findViewById(R.id.tx_header)
        button = findViewById(R.id.btn_header)
        button.setOnClickListener {
            clickListener?.onClick(it)
        }
        textView.setOnClickListener {
            clickListener?.onClick(it)
        }

        loadAttributes(attrs)
        setAttributes()


    }

    private fun loadAttributes(attrs: AttributeSet) {
        // Load attributes
        val a = context.obtainStyledAttributes(
                attrs, R.styleable.HeaderWithLine, 0, 0)

        try {
            _text = a.getString(R.styleable.HeaderWithLine_header_text)
            _textColor = a.getColor(R.styleable.HeaderWithLine_header_textColor, _textColor)
            _textFont = a.getInt(R.styleable.HeaderWithLine_header_textFont, 2)
            _textSize = a.getDimension(R.styleable.HeaderWithLine_header_textSize, _textSize)
            _hasButton = a.getBoolean(R.styleable.HeaderWithLine_header_hasButton, false)
            _textButton = a.getString(R.styleable.HeaderWithLine_header_buttonText)
            _colorButton = a.getColor(R.styleable.HeaderWithLine_header_buttonColor, _colorButton)

        } finally {
            a.recycle()
        }
    }


    private fun setAttributes() {
        textView.let {
            it.text = _text
            it.textSize = _textSize
            it.setTextColor(_textColor)
            setTextViewTypeFace(context, it)
        }
        button.let {
            it.text = _textButton
            it.setBackgroundColor(_colorButton)

            if (_hasButton) {
                it.visibility = View.VISIBLE
            } else {
                it.visibility = View.INVISIBLE

            }
        }
        invalidate()
        requestLayout()

    }

    private fun setTextViewTypeFace(context: Context, textView: TextView) {
        val face = FontFace.getInstance().getFontFace(context, _textFont)
        textView.typeface = face
        invalidate()
        requestLayout()
    }

    fun setText(text: String?) {
        _text = text
        setAttributes()
        invalidate()
    }

    fun setHasMoreBtn(boolean: Boolean) {
        _hasButton = boolean
        setAttributes()
        invalidate()
    }

    fun setOnHeaderClickListener(clickListener: OnClickListener) {
        this.clickListener = clickListener
    }

}
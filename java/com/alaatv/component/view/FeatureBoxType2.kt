package com.alaatv.component.view

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.alaatv.component.R
import com.alaatv.component.databinding.FeatureBoxType2Binding

class FeatureBoxType2(context: Context, attrs: AttributeSet? = null) : FrameLayout(context, attrs) {


    private var value: String? = resources.getString(R.string.demo_feature)
    private var icon: Drawable? = ContextCompat.getDrawable(context, R.drawable.ic_calendar_interface_symbol_tool)


    private lateinit var valueTextView: TextView
    private lateinit var iconImageView: AppCompatImageView

    private lateinit var binding: FeatureBoxType2Binding


    init {
        if (!isInEditMode) {
            val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            binding = DataBindingUtil.inflate(inflater, R.layout.feature_box_type_2, this, true)

            bindViews()
            loadAttributes(attrs)
            setAttributes()
        }
    }

    private fun bindViews() {
        valueTextView = binding.value
        iconImageView = binding.icon
    }

    private fun loadAttributes(attrs: AttributeSet?) {
        // Load attributes
        val a = context.obtainStyledAttributes(
                attrs, R.styleable.FeatureBox, 0, 0)

        value = a.getString(R.styleable.FeatureBox_value_featureBox)
        icon = a.getDrawable(R.styleable.FeatureBox_icon_featureBox)


        a.recycle()
    }

    private fun setAttributes() {

        valueTextView.text = value
        iconImageView.setImageDrawable(icon)
        iconImageView.setColorFilter(Color.TRANSPARENT)
        invalidate()
        requestLayout()
    }

    fun setValue(text: String) {
        valueTextView.text = text
        invalidate()
        requestLayout()
    }

    fun setIcon(icon: Drawable?) {
        iconImageView.setImageDrawable(icon)
        invalidate()
        requestLayout()
    }
}
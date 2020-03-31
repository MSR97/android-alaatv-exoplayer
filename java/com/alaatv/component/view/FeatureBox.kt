package com.alaatv.component.view

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.alaatv.component.R
import com.alaatv.component.databinding.FeatureBoxBinding
import it.sephiroth.android.library.xtooltip.ClosePolicy
import it.sephiroth.android.library.xtooltip.Tooltip

class FeatureBox(context: Context, attrs: AttributeSet? = null) : FrameLayout(context, attrs) {


    private var title: String? = resources.getString(R.string.demo_feature)
    private var value: String? = resources.getString(R.string.demo_feature)
    private var icon: Drawable? = ContextCompat.getDrawable(context, R.drawable.ic_calendar_interface_symbol_tool)


    private lateinit var valueTextView: TextView
    private lateinit var iconImageView: AppCompatImageView
    private lateinit var card_icon: CardView

    private lateinit var binding: FeatureBoxBinding


    init {
        if (!isInEditMode) {
            val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            binding = DataBindingUtil.inflate(inflater, R.layout.feature_box, this, true)

            bindViews()
            loadAttributes(attrs)
            setAttributes()
        }
    }

    private fun bindViews() {
        valueTextView = binding.value
        iconImageView = binding.icon
        card_icon = binding.featureDate
    }

    private fun loadAttributes(attrs: AttributeSet?) {
        // Load attributes
        val a = context.obtainStyledAttributes(
                attrs, R.styleable.FeatureBox, 0, 0)

        title = a.getString(R.styleable.FeatureBox_title_featureBox)
        value = a.getString(R.styleable.FeatureBox_value_featureBox)
        icon = a.getDrawable(R.styleable.FeatureBox_icon_featureBox)


        a.recycle()
    }

    private fun initTooltip(view: View) {

        Tooltip.Builder(context!!)
                .anchor(view, 0, 0, true)
                .closePolicy(ClosePolicy.TOUCH_ANYWHERE_CONSUME)
                .showDuration(0)
                .text(title.toString())
                .overlay(false)
                .create()
                .show(view, Tooltip.Gravity.TOP, false)

    }

    private fun setAttributes() {

        valueTextView.text = value
        iconImageView.setImageDrawable(icon)
        iconImageView.setColorFilter(Color.TRANSPARENT)

        card_icon.setOnClickListener {
            initTooltip(it)
        }
        invalidate()
        requestLayout()
    }

    fun setTitle(text: String) {
        title = text
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
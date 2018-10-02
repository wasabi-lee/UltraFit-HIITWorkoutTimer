package io.incepted.ultrafittimer.view

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.util.AttributeSet
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.content.res.TypedArrayUtils
import androidx.preference.PreferenceCategory
import androidx.preference.PreferenceViewHolder
import io.incepted.ultrafittimer.R
import io.incepted.ultrafittimer.UltraFitApp
import javax.inject.Inject

class CustomPrefCategory(context: Context, attributeSet: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) :
        PreferenceCategory(context, attributeSet, defStyleAttr, defStyleRes) {

    constructor(context: Context, attributeSet: AttributeSet?, defStyleAttr: Int) :
            this(context, attributeSet, defStyleAttr, 0)

    @SuppressLint("RestrictedApi")
    constructor(context: Context, attributeSet: AttributeSet?) :
            this(context, attributeSet, TypedArrayUtils.getAttr(context, R.attr.preferenceCategoryStyle,
                    android.R.attr.preferenceCategoryStyle))

    constructor(context: Context) :
            this(context, null)

    @Inject
    lateinit var sharedPref: SharedPreferences

    private var darkMode = false

    init {
        (context.applicationContext as UltraFitApp).getAppComponent().inject(this)
        darkMode = sharedPref.getBoolean("pref_key_theme", false)
    }

    override fun onBindViewHolder(holder: PreferenceViewHolder?) {
        super.onBindViewHolder(holder)
//        val textColor: Int = ContextCompat
//                .getColor(context,
//                        if(darkMode) R.color.colorAccent_header_dark else R.color.colorAccent_header_light)
        holder?.itemView?.findViewById<TextView>(android.R.id.title)?.
                setTextColor(ContextCompat.getColor(context, R.color.colorAccent_header_light))
    }


}
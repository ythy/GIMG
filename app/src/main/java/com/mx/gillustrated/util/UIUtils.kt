package com.mx.gillustrated.util

import android.widget.LinearLayout
import android.widget.Spinner

object UIUtils {

    fun setSpinnerClick(spinners: List<Spinner>) {
        val iterator = spinners.iterator()
        while (iterator.hasNext()) {
            setSpinnerSingleClick(iterator.next())
        }
    }

    fun setSpinnerSingleClick(spinner: Spinner) {
        val llParent = spinner.parent as LinearLayout
        llParent.setOnClickListener { spinner.performClick() }

    }
}

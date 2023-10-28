package com.doubtnut.core.utils

import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet

object ViewGroupUtils {

    fun ViewGroup.applyWidthConstraint(
        parentConstraintLayout: ConstraintLayout,
        widthPercentage: Float?
    ) {
        val constraints = ConstraintSet()
        constraints.clone(parentConstraintLayout)
        constraints.constrainPercentWidth(
            id,
            widthPercentage ?: 0.3F
        )
        constraints.applyTo(parentConstraintLayout)
    }
}

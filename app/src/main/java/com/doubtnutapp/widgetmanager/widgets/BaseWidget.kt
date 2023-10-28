package com.doubtnutapp.widgetmanager.widgets

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.fragment.app.FragmentActivity
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnut.core.widgets.interfaces.WidgetFunctions
import com.doubtnut.core.widgets.ui.CoreWidget
import com.doubtnut.core.widgets.ui.CoreWidgetVH
import com.doubtnutapp.training.CustomToolTip
import com.doubtnutapp.training.OnboardingManager

abstract class BaseWidget<VH : CoreWidgetVH, WM : WidgetEntityModel<*, *>>
@JvmOverloads
constructor(
    context: Context,
    @Suppress("UNUSED_PARAMETER")
    attrs: AttributeSet? = null,
    @Suppress("UNUSED_PARAMETER")
    defStyle: Int = 0
) : CoreWidget<VH, WM>(context, attrs, defStyle), WidgetFunctions<VH, WM> {

    fun showToolTip(
        context: Context,
        title: String,
        description: String,
        buttonText: String,
        view: View,
        voiceUrl: String,
        listener: (View) -> Unit,
        tooltipListener: (View) -> Unit,
    ) {
        OnboardingManager(
            context as FragmentActivity, 0,
            title,
            description,
            buttonText,
            listener,
            tooltipListener,
            CustomToolTip(context),
            voiceUrl
        ).launchTourGuide(view)
    }
}
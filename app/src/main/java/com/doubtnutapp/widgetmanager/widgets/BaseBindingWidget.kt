package com.doubtnutapp.widgetmanager.widgets

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.viewbinding.ViewBinding
import com.doubtnut.core.widgets.ui.CoreWidgetVH
import com.doubtnut.core.widgets.entities.WidgetEntityModel

/**
 * Created by devansh on 12/08/21.
 */
abstract class BaseBindingWidget<VH : CoreWidgetVH, WM : WidgetEntityModel<*, *>, VB : ViewBinding>
@JvmOverloads
constructor(
    context: Context,
    @Suppress("UNUSED_PARAMETER")
    attrs: AttributeSet? = null,
    @Suppress("UNUSED_PARAMETER")
    defStyle: Int = 0
) : BaseWidget<VH, WM>(context, attrs, defStyle) {

    @Deprecated(
        message = "Use getViewBinding() instead to support view binding",
        replaceWith = ReplaceWith("getViewBinding()"),
        level = DeprecationLevel.HIDDEN
    )
    @Throws(UnsupportedOperationException::class)
    override fun getView(): View {
        throw UnsupportedOperationException("Use getViewBinding() instead")
    }

    abstract fun getViewBinding(): VB

}

class BaseBindingViewHolder<VB : ViewBinding>(
    val viewBinding: VB,
    widget: BaseWidget<*, *>
) :
    CoreWidgetVH(widget)
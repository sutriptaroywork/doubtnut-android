package com.doubtnut.core.widgets.ui

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.viewbinding.ViewBinding
import com.doubtnut.core.analytics.IAnalyticsPublisher
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import javax.inject.Inject

abstract class CoreBindingWidget<VH : CoreWidgetVH, WM : WidgetEntityModel<*, *>, VB : ViewBinding>
@JvmOverloads
constructor(
    context: Context,
    @Suppress("UNUSED_PARAMETER")
    attrs: AttributeSet? = null,
    @Suppress("UNUSED_PARAMETER")
    defStyle: Int = 0
) : CoreWidget<VH, WM>(context, attrs, defStyle) {

    /**
     * https://github.com/google/dagger/issues/955#issuecomment-347749874
     *
     * A workaround that seems to work, is adding an unneeded @Inject field to Middle class.
     * This will make dagger generate the MembersInjector in the base module, and it won't be generated in other modules any more.
     */
    @Inject
    lateinit var mAnalyticsPublisher2: IAnalyticsPublisher

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
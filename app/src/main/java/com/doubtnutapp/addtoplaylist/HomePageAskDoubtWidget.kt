package com.doubtnutapp.addtoplaylist

import android.content.Context
import android.view.LayoutInflater
import androidx.annotation.Keep
import com.airbnb.lottie.LottieDrawable
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnut.core.widgets.entities.WidgetAction
import com.doubtnut.core.widgets.entities.WidgetData
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnut.core.widgets.ui.WidgetBindingVH
import com.doubtnutapp.Constants
import com.doubtnutapp.DoubtnutApp
import com.doubtnutapp.R
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnutapp.databinding.WidgetHomepageAskQuestionBinding
import com.doubtnutapp.defaultPrefs
import com.doubtnutapp.screennavigator.CameraScreen
import com.doubtnutapp.screennavigator.Navigator
import com.doubtnutapp.utils.UserUtil
import com.doubtnutapp.widgetmanager.widgets.BaseBindingWidget
import com.doubtnutapp.widgetmanager.widgets.BaseWidget
import javax.inject.Inject

/**
 * Created by Akshat Jindal 13/4/21
 */
class HomePageAskDoubtWidget(context: Context) :
    BaseBindingWidget<HomePageAskDoubtWidgetHolder,
        HomePageAskDoubtWidgetModel, WidgetHomepageAskQuestionBinding>(context) {

    @Inject
    lateinit var screenNavigator: Navigator

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    init {
        DoubtnutApp.INSTANCE.daggerAppComponent!!.inject(this)
    }

    override fun getViewBinding(): WidgetHomepageAskQuestionBinding {
        return WidgetHomepageAskQuestionBinding.inflate(LayoutInflater.from(context), this, true)
    }

    override fun setupViewHolder() {
        this.widgetViewHolder = HomePageAskDoubtWidgetHolder(getViewBinding(), this)
    }

    override fun bindWidget(holder: HomePageAskDoubtWidgetHolder, model: HomePageAskDoubtWidgetModel): HomePageAskDoubtWidgetHolder {
        super.bindWidget(holder, model)
        val binding = holder.binding

        binding.apply {
            cameraAnimation.setAnimation("lottie_cam_anim.zip")
            cameraAnimation.repeatCount = LottieDrawable.INFINITE
//            cameraAnimation.imageAssetsFolder = "camera_animation_images"
            cameraAnimation.playAnimation()
        }

        val userClass = defaultPrefs().getString(Constants.STUDENT_CLASS, "")
            ?: ""
        val hashMap = UserUtil.getConfigMap() ?: HashMap()
        binding.tvSubTitle.text = (hashMap[userClass] as? String?)
            ?: context.getString(R.string.title_homeFeed_turantMilegaSolution)
        binding.tvTitle.text = (hashMap["home_page_camera_title"] as? String?)
            ?: context.getString(R.string.ask_doubt)

        binding.root.setOnClickListener {
            analyticsPublisher.publishEvent(AnalyticsEvent(EventConstants.HOME_PAGE_TOP))
            analyticsPublisher.publishEvent(AnalyticsEvent(EventConstants.EVENT_NEW_HOME_ASK_QUESTION_CLICK_WITH_FULL_ACTION_BAR, ignoreFirebase = false))
            analyticsPublisher.publishEvent(AnalyticsEvent(EventConstants.EVENT_NEW_HOME_ASK_QUESTION_CLICK, ignoreFirebase = false))
            screenNavigator.startActivityFromActivity(context, CameraScreen, null)
        }
        return holder
    }
}

class HomePageAskDoubtWidgetHolder(binding: WidgetHomepageAskQuestionBinding, widget: BaseWidget<*, *>) :
    WidgetBindingVH<WidgetHomepageAskQuestionBinding>(binding, widget)

class HomePageAskDoubtWidgetModel : WidgetEntityModel<HomePageAskDoubtWidgetData, WidgetAction>()

@Keep
data class HomePageAskDoubtWidgetData(
    val x: String? = null
) : WidgetData()

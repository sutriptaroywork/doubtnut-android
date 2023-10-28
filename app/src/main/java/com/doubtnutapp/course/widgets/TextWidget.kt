package com.doubtnutapp.course.widgets

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.os.CountDownTimer
import android.text.TextUtils
import android.text.util.Linkify
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View.OnTouchListener
import android.widget.TextView
import androidx.core.view.updatePadding
import com.doubtnut.core.utils.*
import com.doubtnut.core.widgets.entities.WidgetLayoutConfig
import com.doubtnut.core.widgets.ui.WidgetBindingVH
import com.doubtnutapp.DoubtnutApp
import com.doubtnutapp.base.SgChildWidgetLongClick
import com.doubtnutapp.base.TextWidgetClick
import com.doubtnutapp.data.remote.models.TextWidgetModel
import com.doubtnutapp.databinding.WidgetTextBinding
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnutapp.doubtpecharcha.ui.activity.DoubtP2pActivity
import com.doubtnutapp.feed.view.LinkPreviewView
import com.doubtnutapp.loadImageEtx
import com.doubtnutapp.studygroup.ui.fragment.SgAdminDashboardFragment
import com.doubtnutapp.studygroup.ui.fragment.SgChatFragment
import com.doubtnutapp.studygroup.ui.fragment.SgPersonalChatFragment
import com.doubtnutapp.studygroup.ui.fragment.SgUserReportedMessageFragment
import com.doubtnutapp.utils.Utils
import com.doubtnutapp.widgetmanager.widgets.BaseBindingWidget
import com.doubtnutapp.widgetmanager.widgets.BaseWidget
import me.saket.bettermovementmethod.BetterLinkMovementMethod
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class TextWidget(context: Context) :
    BaseBindingWidget<TextWidget.WidgetHolder, TextWidgetModel, WidgetTextBinding>(context) {

    var source: String? = null

    private var countDownTimer: CountDownTimer? = null

    @Inject
    lateinit var deeplinkAction: DeeplinkAction

    override fun getViewBinding(): WidgetTextBinding {
        return WidgetTextBinding
            .inflate(LayoutInflater.from(context), this, true)
    }

    override fun setupViewHolder() {
        DoubtnutApp.INSTANCE.daggerAppComponent!!.inject(this)
        this.widgetViewHolder = WidgetHolder(getViewBinding(), this)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun bindWidget(holder: WidgetHolder, model: TextWidgetModel): WidgetHolder {
        val newModel = if (
            source == SgChatFragment.STUDY_GROUP ||
            source == DoubtP2pActivity.DOUBT_P2P ||
            source == SgAdminDashboardFragment.STUDY_GROUP_ADMIN_DASHBOARD ||
            source == SgUserReportedMessageFragment.STUDY_GROUP_USER_REPORTED_MESSAGE ||
            source == SgPersonalChatFragment.SOURCE_PERSONAL_CHAT
        ) {
            model.apply {
                layoutConfig = WidgetLayoutConfig(
                    marginTop = 10,
                    marginLeft = 12,
                    marginRight = 12,
                )
                _data = data.copy(
                    textSize = 14F,
                    textColor = "#2f2f2f",
                    backgroundColor = "#00000000",
                    isBold = false,
                )
            }
        } else {
            model
        }
        super.bindWidget(holder, newModel)
        val data = model.data
        val binding = holder.binding

        val showTimer = model.data.startTimeInMillis != null && model.data.startTimeInMillis!! > 0L
        if (!showTimer) {
            if (data.htmlTitle.isNullOrEmpty()) {
                binding.textView.text = data.title.orEmpty()
            } else {
                TextViewUtils.setTextFromHtml(
                    binding.textView,
                    data.htmlTitle.orEmpty()
                )
            }
        }

        data.layoutPadding?.let { textPadding ->
            binding.rootLayout.updatePadding(
                (textPadding.paddingStart ?: 0).dpToPx(),
                (textPadding.paddingTop ?: 0).dpToPx(),
                (textPadding.paddingEnd ?: 0).dpToPx(),
                (textPadding.paddingBottom ?: 0).dpToPx()
            )
        }

        if (source == SgAdminDashboardFragment.STUDY_GROUP_ADMIN_DASHBOARD ||
            source == SgUserReportedMessageFragment.STUDY_GROUP_USER_REPORTED_MESSAGE
        ) {
            binding.textView.maxLines = 8
            binding.textView.ellipsize = TextUtils.TruncateAt.END
        }
        if (data.linkify == null || data.linkify == true) {
            BetterLinkMovementMethod
                .linkify(Linkify.ALL, binding.textView)
                .setOnLinkClickListener { textView: TextView, url: String ->
                    LinkPreviewView.linkClickAction(textView.context, url)
                }
        } else {
            binding.textView.removeLinksUnderline(data.textColor)
        }

        binding.ivStart.apply {
            if (data.imageStart.isNotNullAndNotEmpty2()) {
                layoutParams.height = (data.imageStartHeight ?: 34).dpToPx()
                layoutParams.width = (data.imageStartWidth ?: 34).dpToPx()
                requestLayout()
                visible()
                loadImageEtx(data.imageStart)
            } else {
                gone()
            }
        }

        binding.textView.textSize = data.textSize ?: 16f
        binding.textView.setTextColor(Utils.parseColor(data.textColor, Color.BLACK))
        when {
            data.gravity.equals("center") -> {
                binding.textView.gravity = Gravity.CENTER
            }
            data.gravity.equals("right") -> {
                binding.textView.gravity = Gravity.END
            }
            else -> {
                binding.textView.gravity = Gravity.START
            }
        }
        binding.rootLayout.background = Utils.getShape(
            colorString = data.backgroundColor ?: "#ffffff",
            strokeColor = data.strokeColor ?: "#ffffff",
            cornerRadius = data.cornerRadius ?: 0F,
            strokeWidth = data.strokeWidth ?: 0
        )
        binding.textView.applyTypeface(data.isBold)

        val lastTouchDownXY = FloatArray(2)
        binding.textView.setOnTouchListener(
            OnTouchListener { _, event ->
                if (event.actionMasked == MotionEvent.ACTION_DOWN) {
                    lastTouchDownXY[0] = event.x
                    lastTouchDownXY[1] = event.y
                }
                return@OnTouchListener false
            }
        )

        binding.textView.setOnLongClickListener {
            actionPerformer?.performAction(SgChildWidgetLongClick(model.type, lastTouchDownXY))
            true
        }

        binding.textView.setOnClickListener {
            if (data.deeplink.isNullOrEmpty()) {
                actionPerformer?.performAction(TextWidgetClick(data))
            } else {
                deeplinkAction.performAction(context, data.deeplink)
            }
        }

        if (data.deeplink.isNullOrEmpty() || data.forceHideRightIcon == true) {
            binding.ivEnd.gone()
        } else {
            binding.ivEnd.visible()
        }

        binding.ivEnd.setOnClickListener {
            if (data.deeplink.isNullOrEmpty()) return@setOnClickListener
            deeplinkAction.performAction(context, data.deeplink)
        }

        countDownTimer?.cancel()
        if (showTimer) {
            countDownTimer = object : CountDownTimer(
                (model.data.startTimeInMillis ?: 0L),
                1000
            ) {
                override fun onTick(millisUntilFinished: Long) {
                    val min = (
                            (
                                    TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished) - TimeUnit.HOURS.toMinutes(
                                        TimeUnit.MILLISECONDS.toHours(millisUntilFinished)
                                    )
                                    ).toString()
                            )
                    val sec = (
                            (
                                    TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) - TimeUnit.MINUTES.toSeconds(
                                        TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished)
                                    )
                                    ).toString()
                            )

                    val timer = "$min:$sec"

                    if (data.htmlTitle.isNullOrEmpty()) {
                        binding.textView.text = data.title.orEmpty()
                            .replace("__placeholder__m:s__", timer)
                    } else {
                        TextViewUtils.setTextFromHtml(
                            binding.textView,
                            data.htmlTitle.orEmpty()
                                .replace("__placeholder__m:s__", timer)
                        )
                    }
                }

                override fun onFinish() {
                }
            }
            countDownTimer?.start()
        }

        return holder
    }

    class WidgetHolder(binding: WidgetTextBinding, widget: BaseWidget<*, *>) :
        WidgetBindingVH<WidgetTextBinding>(binding, widget)
}

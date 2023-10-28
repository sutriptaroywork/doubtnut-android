package com.doubtnutapp.domain.camerascreen.entity

import androidx.annotation.Keep
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.google.gson.annotations.SerializedName

/**
 * Created by Anand Gaurav on 2020-01-07.
 */
@Keep
data class CameraSettingEntity(
    @SerializedName("cameraButtonHint") val cameraButtonHint: CameraButtonHint?,
    @SerializedName("bottomOverlay") val bottomOverlay: BottomOverlay?,
    @SerializedName("ncertWatchedPlaylist") val ncertWatchedPlaylist: NcertData?,
    @SerializedName("camera_back_widgets") val widgets: List<WidgetEntityModel<*, *>>?,
    @SerializedName("referral_widget_data") val referralWidgetData: ReferralWidgetData?,
    @SerializedName("deeplink") val deepLink: String?,
    @SerializedName("d0_user_data") val d0UserData: D0UserData?,
    @SerializedName("shorts_data") val shortsData: ShortsData?,
)

@Keep
data class ShortsData(
    @SerializedName("shorts_animation_title") val shortsAnimationTitle: String?,
    @SerializedName("show_shorts") val showShorts: Boolean?,
    @SerializedName("show_shorts_animation") val showShortsAnimation: Boolean?,
    @SerializedName("shorts_animation_timeout") val shortsAnimationTimeout: Long?
)

@Keep
data class ReferralWidgetData(
    @SerializedName("refer_image_url") val imageUrl: String?,
    @SerializedName("title") val title: String?,
    @SerializedName("title_text_color") val titleTextColor: String?,
    @SerializedName("title_text_size") val titleTextSize: String?,
    @SerializedName("subtitle") val subtitle: String?,
    @SerializedName("subtitle_text_color") val subtitleTextColor: String?,
    @SerializedName("subtitle_text_size") val subtitleTextSize: String?,
    @SerializedName("bg_start_color") val bgStartColor: String?,
    @SerializedName("bg_end_color") val bgEndColor: String?,
    @SerializedName("deeplink") val deepLink: String?,
)

@Keep
data class D0UserData(
    @SerializedName("exit_app_on_back_press") val exitOnBackPress: Boolean?,
    @SerializedName("overlay_data") val overlayData: OverlayData?,
    @SerializedName("timer_data") val timerData: TimerData?,
    @SerializedName("hide_search_icon") val hideSearchIcon: Boolean?,
    @SerializedName("hide_ask_history") val hideAskHIstory: Boolean?,
    @SerializedName("hide_audio_tooltip") val hideAudioTooltip: Boolean?
)

@Keep
data class OverlayData(
    @SerializedName("image") val image: String?,
    @SerializedName("title") val title: String,
    @SerializedName("cta") val cta: String
)

@Keep
data class TimerData(
    @SerializedName("expiration_time") val expirationTime: Long?,
    @SerializedName("title") val title: String?,
    @SerializedName("icon") val icon: String?,
)

@Keep
data class CameraButtonHint(
    @SerializedName("durationSec") val durationSec: String?,
    @SerializedName("content") val content: List<String?>?
)

@Keep
data class BottomOverlay(
    @SerializedName("info") val sampleQuestionInfoEntity: SampleQuestionInfoEntity?,
    @SerializedName("subjectList") val subjectList: List<SubjectEntity?>?
)

@Keep
data class SubjectEntity(
    @SerializedName("subject") val subject: String?,
    @SerializedName("imageUrl") val imageUrl: String?
)

@Keep
data class NcertData(
    @SerializedName("title") val title: String?,
    @SerializedName("list") val list: List<NcertItemData>?
)

@Keep
data class NcertItemData(
    @SerializedName("id") val id: String?,
    @SerializedName("name") val name: String?,
    @SerializedName("description") val description: String?,
    @SerializedName("is_last") val isLast: String?,
    @SerializedName("parent") val parent: String?,
    @SerializedName("resource_type") val resourceType: String?,
    @SerializedName("student_class") val studentClass: String?,
    @SerializedName("subject") val subject: String?,
    @SerializedName("main_description") val mainDescription: String?
)

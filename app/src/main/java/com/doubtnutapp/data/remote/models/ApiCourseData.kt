package com.doubtnutapp.data.remote.models

import android.os.Parcelable
import androidx.annotation.Keep
import com.doubtnutapp.course.widgets.PackageDetailWidgetItem
import com.doubtnutapp.course.widgets.WidgetViewPlanButtonData
import com.doubtnut.core.widgets.entities.WidgetAction
import com.doubtnut.core.widgets.entities.WidgetData
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnutapp.domain.newglobalsearch.entities.SearchFilter
import com.doubtnutapp.domain.videoPage.entities.ApiVideoResource
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

/**
 * Created by Anand Gaurav on 16/04/20.
 */
@Keep
data class ApiCourseData(
    @SerializedName("widgets") val widgets: List<WidgetEntityModel<*, *>>,
    @SerializedName("buy_button") val buttonInfo: ButtonInfo?,
    @SerializedName("course_list") val courseList: List<CourseFilterTypeData>?,
    @SerializedName("title") val title: String?,
    @SerializedName("filter_button") val filterText: String?,
    @SerializedName("popup_deeplink") val popupDeeplink: String?,
)

@Keep
data class ApiScheduleData(
    @SerializedName("widgets") val widgets: List<WidgetEntityModel<*, *>>?,
    @SerializedName("cursor") val cursor: Cursor?,
    @SerializedName("today") val today: ScheduleTodayData?,
    @SerializedName("no_time_table") val noSchedule: NoSchedule?,
)

@Keep
data class NoSchedule(
    @SerializedName("image_url") val imageUrl: String?,
    @SerializedName("title") val title: String?,
    @SerializedName("sub_title") val subTitle: String?,
    @SerializedName("button_text") val buttonText: String?,
    @SerializedName("deeplink") val deeplink: String?,
)

@Keep
data class Cursor(
    @SerializedName("prev") val previous: String?,
    @SerializedName("next") val next: String?,
)

@Keep
data class ScheduleTodayData(
    @SerializedName("tag") val tag: String?,
    @SerializedName("day") val day: String?,
    @SerializedName("date") val date: String?,
)

@Keep
data class ApiVmcDetailData(@SerializedName("widgets") val widgets: List<WidgetEntityModel<*, *>>)

@Keep
data class ApiRecordedCourseData(
    @SerializedName("widgets_top") val widgetsTop: List<WidgetEntityModel<*, *>>,
    @SerializedName("widgets") val widgets: List<WidgetEntityModel<*, *>>,
)

@Keep
data class ApiLiveSectionData(
    @SerializedName("widgets") val widgets: List<WidgetEntityModel<*, *>>,
    @SerializedName("trial_button") val buttonInfo: ButtonInfo?,
)

@Keep
data class ApiResourceData(
    @SerializedName("widgets") val widgets: List<WidgetEntityModel<*, *>>,
    @SerializedName("title") val title: String?,
)

@Keep
data class ActivateTrialData(
    @SerializedName("message") val message: String?,
)

@Keep
data class ApiCourseDataV3(
    @SerializedName("widgets") val widgets: List<WidgetEntityModel<*, *>>?,
    @SerializedName("trial_widget") val stickyWidgets: List<WidgetEntityModel<*, *>>?,
    @SerializedName("extra_widgets") val extraWidgets: List<WidgetEntityModel<*, *>>?,
    @SerializedName("tabs") val tabList: List<CourseTabItem>?,
    @SerializedName("chat_header") val title: String?,
    @SerializedName("show_in_app_search") val showInAppSearch: Boolean?,
    @SerializedName("in_app_search_title") val inAppSearchTitle: String?,
    @SerializedName("shareable_message") val shareMessage: String?,
    @SerializedName("control_params") var controlParms: HashMap<String, String>?,
    @SerializedName("feature_name") val featureName: String?,
    @SerializedName("channel") val channel: String?,
    @SerializedName("campaign_id") val campaigniId: String?,
    @SerializedName("share_image") val shareImageUrl: String?,
    @SerializedName("is_trial") val isTrial: Boolean?,
    @SerializedName("is_show") val shouldShowSaleDialog: Boolean?,
    @SerializedName("nudge_id") val nudgeId: Int?,
    @SerializedName("count") val nudgeCount: Int?,
    @SerializedName("buy_button") val buttonInfo: ButtonInfo?,
    @SerializedName("call_data") val callData: CallData?,
    @SerializedName("demo_video") val demoVideo: DemoVideo?,
    @SerializedName("title") val toolbarTitle: String?,
    @SerializedName("subtitle") val toolbarSubtitle: String?,

    @SerializedName("trial_title")
    var trialTitle: String?,
    @SerializedName("trial_title_expired")
    val trialTitleExpired: String?,

    @SerializedName("trial_title_size")
    val trialTitleSize: String?,
    @SerializedName("trial_title_color")
    val trialTitleColor: String?,
    @SerializedName("time")
    val time: Long?,
    @SerializedName("time_text_color")
    val timeTextColor: String?,
    @SerializedName("time_text_size")
    val timeTextSize: String?,
    @SerializedName("bg_color_one")
    var bgColorOne: String?,
    @SerializedName("bg_color_two")
    var bgColorTwo: String?,
    @SerializedName("bg_color_one_expired")
    val bgColorOneExpired: String?,
    @SerializedName("bg_color_two_expired")
    val bgColorTwoExpired: String?,

    @SerializedName("image_url")
    val imageUrl: String?,

    @SerializedName("course_list") val courseList: List<CourseFilterTypeData>?,
    @SerializedName("pop_up_deeplink") val popUpDeeplink: String?,
    @SerializedName("expanded") val expanded: Boolean?,
    @SerializedName("support_options") val supportData: List<SupportData>?,
    @SerializedName("chat_text") val chatText: String?,
    @SerializedName("is_vip") val isVip: Boolean?,
    @SerializedName("calling_card_chat_deeplink") val callingCardChatDeeplink: String?,
    @SerializedName("course_change_popup") val courseHelpData: CourseHelpData?,
    @SerializedName("chat_deeplink") val chatDeeplink: String?,
    @SerializedName("widget_view_plan_button") val viewPlanBtnData: WidgetViewPlanButtonData?,
    @SerializedName("assortment_id") val assortmentId: String?,
    @SerializedName("fab") val fabData: FabData?,
    @SerializedName("deeplink_back") val backDeeplink: String?,
)

@Keep
data class FabData(
    @SerializedName("deeplink") val deeplink: String?,
    @SerializedName("image_url") val imageUrl: String?,
)

@Keep
@Parcelize
data class CourseHelpData(
    @SerializedName("title") val title: String?,
    @SerializedName("subtitle") val subtitle: String?,
    @SerializedName("request_button_text") val requestButtonText: String?,
    @SerializedName("cancel_button_text") val cancelButtonText: String?,
    @SerializedName("deeplink") val deeplink: String?,
) : Parcelable

@Keep
data class DemoVideo(
    @SerializedName("image_url") val imageUrl: String?,
    @SerializedName("image_url_two") val imageUrlTwo: String?,
    @SerializedName("call_us_text") val callUsText: String?,
    @SerializedName("call_us_number") val callUsNumber: String?,
    @SerializedName("bottom_title") val bottomTitle: String?,
    @SerializedName("bottom_sub_title") val bottomSubTitle: String?,
    @SerializedName("button_text") val buttonText: String?,
    @SerializedName("button_color") val buttonColor: String?,
    @SerializedName("button_text_color") val buttonTextColor: String?,
    @SerializedName("button_deeplink") val buttonDeeplink: String?,
    @SerializedName("video_resources") val videoResources: List<ApiVideoResource>?,
    @SerializedName("delay") val delay: String?,
    @SerializedName("qid") val qid: String?,
    @SerializedName("view_id") val viewId: String?,
    @SerializedName("page") val page: String?,
    @SerializedName("text_color") val textColor: String?,
)

@Keep
data class ApiTimetableData(
    @SerializedName("widgets") val widgets: List<WidgetEntityModel<*, *>>,
    @SerializedName("title") val title: String?,
)

@Keep
@Parcelize
data class CourseTabItem(
    @SerializedName("id") val id: String?,
    @SerializedName("text") val title: String?,
) : Parcelable

@Keep
data class ButtonInfo(
    @SerializedName("id") val id: String?,
    @SerializedName("title") val title: String?,
    @SerializedName("amount_to_pay") val amountToPay: String?,
    @SerializedName("amount_strike_through") val amountStrikeThrough: String?,
    @SerializedName("amount_saving") val amountSaving: String?,
    @SerializedName("emi") val emi: PackageDetailWidgetItem.Emi?,
    @SerializedName("deeplink") val deeplink: String?,
    @SerializedName("pay_text") val payText: String?,
    @SerializedName("pay_installment_text") val payInstallmentText: String?,
    @SerializedName("upfront_variant") val variantId: String?,
    @SerializedName("emi_variant") val variantIdInstallment: String?,
    @SerializedName("type") val type: String?,
    @SerializedName("state") val buttonState: String?,
    @SerializedName("multiple_package") val multiplePackage: Boolean?,
    @SerializedName("installment_deeplink") val installmentDeeplink: String?,
    @SerializedName("know_more_text") val knowMoreText: String?,
    @SerializedName("assortment_id") val assortmentId: String?,
)

@Keep
data class SupportData(
    @SerializedName("id") val id: String?,
    @SerializedName("title") val text: String?,
    @SerializedName("number") val data: String?,
    @SerializedName("icon_url") val iconUrl: String?,
    @SerializedName("title_color") val textColor: String?,
    @SerializedName("deeplink") val deeplink: String?,
    @SerializedName("event_params") val eventParams: HashMap<String, Any>?
)

@Keep
data class CallData(
    @SerializedName("title") val title: String,
    @SerializedName("number") val number: String,
)

class PaymentCardWidgetModel : WidgetEntityModel<PaymentCardWidgetData, WidgetAction>()

@Keep
data class PaymentCardWidgetData(
    @SerializedName("text1") val textOne: String?,
    @SerializedName("text2") val textTwo: String?,
    @SerializedName("button_text") val buttonText: String?,
    @SerializedName("event_name") val eventType: String?,
    @SerializedName("payment_deeplink") val paymentDeeplink: String?,
) : WidgetData()

class FacultyGridWidgetModel : WidgetEntityModel<FacultyGridWidgetData, WidgetAction>()

@Keep
data class FacultyGridWidgetData(
    @SerializedName("title") val title: String?,
    @SerializedName("subject_filter") val subjectFilterList: List<SubjectFilterData>?,
    @SerializedName("items") val items: List<FacultyGridItem>?,
    var selectedFilterKey: String? = "",
) : WidgetData()

@Keep
data class SubjectFilterData(
    @SerializedName("key") val key: String?,
    @SerializedName("display") val display: String?,
    @SerializedName("color") val color: String?,
)

class LiveClassInfoWidgetModel : WidgetEntityModel<LiveClassInfoWidgetData, WidgetAction>()

@Keep
data class LiveClassInfoItem(
    @SerializedName("title") val title: String?,
    @SerializedName("image_url") val imageUrl: String?,
)

@Keep
data class LiveClassInfoWidgetData(
    @SerializedName("title") val title: String?,
    @SerializedName("items") val items: List<LiveClassInfoItem>?,
) : WidgetData()

class RankersClassesWidgetModel : WidgetEntityModel<RankersClassesWidgetData, WidgetAction>()

@Keep
data class RankersClassesWidgetData(
    @SerializedName("title") val title: String?,
    @SerializedName("bg_image") val bgImage: String?,
    @SerializedName("items") val items: List<String>?,
) : WidgetData()

class RankersWidgetModel : WidgetEntityModel<RankersWidgetData, WidgetAction>()

@Keep
data class RankersWidgetItem(
    @SerializedName("image_url") val imageUrl: String?,
    @SerializedName("student_name") val studentName: String?,
    @SerializedName("rank") val rank: String?,
)

@Keep
data class RankersWidgetData(
    @SerializedName("title") val title: String?,
    @SerializedName("items") val items: List<RankersWidgetItem>?,
) : WidgetData()

@Keep
data class FacultyGridItem(
    @SerializedName("id") val id: String?,
    @SerializedName("title") val title: String?,
    @SerializedName("subject") val subject: String?,
    @SerializedName("ecm_id") val ecmId: String?,
    @SerializedName("course") val course: String?,
    @SerializedName("image_url") val imageUrl: String?,
    @SerializedName("degree_college") val degreeCollege: String?,
    @SerializedName("color") val color: String?,
    @SerializedName("degree_obtained") val degreeObtained: String?,
    @SerializedName("college") val college: String?,
    @SerializedName("years_experience") val yearsExperience: String?,
    @SerializedName("students") val students: String?,
    @SerializedName("question_id") val questionId: String?,
    @SerializedName("button_text") val buttonText: String?,
    @SerializedName("page") val page: String?,
)

class VerticalListWidgetModel : WidgetEntityModel<VerticalListWidgetData, WidgetAction>()

@Keep
data class VerticalListWidgetData(
    @SerializedName("title") val title: String?,
    @SerializedName("subject_filter") val subjectFilterList: List<SubjectFilterData>?,
    @SerializedName("items") val items: List<FacultyVerticalItem>?,
) : WidgetData()

@Keep
data class FacultyVerticalItem(
    @SerializedName("id") val id: String?,
    @SerializedName("faculty_id") val facultyId: String?,
    @SerializedName("title") val title: String?,
    @SerializedName("bottom_title") val bottomTitle: String?,
    @SerializedName("subject") val subject: String?,
    @SerializedName("course") val course: String?,
    @SerializedName("image_title") val imageTitle: String?,
    @SerializedName("image_subtitle") val imageSubtitle: String?,
    @SerializedName("image_duration") val imageDuration: String?,
    @SerializedName("image_bar_color") val imageBarColor: String?,
    @SerializedName("image_url") val imageUrl: String?,
    @SerializedName("image_bg") val imageBg: String?,
    @SerializedName("bottom_color") val bottomColor: String?,
    @SerializedName("is_vip") val isVip: Boolean?,
    @SerializedName("tag") val tag: FacultyTag?,
    @SerializedName("views") val views: String?,
    @SerializedName("video_count") val videoCount: String?,
    @SerializedName("ecm_id") val ecmId: String?,
    @SerializedName("is_premium") val isPremium: Boolean?,
)

@Keep
data class FacultyTag(
    @SerializedName("background_color") val backgroundColor: String?,
    @SerializedName("text") val text: String?,
    @SerializedName("text_color") val textColor: String?,
)

class ToppersSpeakWidgetModel : WidgetEntityModel<ToppersSpeakWidgetData, WidgetAction>()

@Keep
data class ToppersSpeakWidgetData(
    @SerializedName("title") val title: String?,
    @SerializedName("items") val items: List<ToppersSpeakItem>?,
) : WidgetData()

@Keep
data class ToppersSpeakItem(
    @SerializedName("title") val title: String?,
    @SerializedName("image_url") val imageUrl: String?,
    @SerializedName("description") val description: String?,
    @SerializedName("subtitle") val subtitle: String?,
)

class AllCourseWidgetModel : WidgetEntityModel<AllCourseWidgetData, WidgetAction>()

@Keep
data class AllCourseWidgetData(
    @SerializedName("title") val title: String?,
    @SerializedName("subject_filter") val subjectFilterList: List<SubjectFilterData>?,
    @SerializedName("course_filter") val courseFilterList: List<CourseFilterData>?,
    @SerializedName("items") val items: List<FacultyVerticalItem>?,
    var selectedSubjectKey: String?,
    var selectedCourseKey: String?,
) : WidgetData()

@Keep
data class CourseFilterData(
    @SerializedName("key") val key: String?,
    @SerializedName("value") val value: String?,
    @SerializedName("color") val color: String?,
)

class ButtonWidgetModel : WidgetEntityModel<ButtonWidgetData, WidgetAction>()

@Keep
data class ButtonWidgetData(
    @SerializedName("button_text") val buttonText: String?,
    @SerializedName("bg_color") val bgColor: String? = null,
    @SerializedName("is_all_caps") val isAllCaps: Boolean = true,
    @SerializedName("is_bold") val isBold: Boolean = false,
    @SerializedName("horizontal_bias") val horizontalBias: Float? = null,
) : WidgetData()

class LiveFreeClassWidgetModel : WidgetEntityModel<LiveFreeClassWidgetData, WidgetAction>()

@Keep
data class LiveFreeClassWidgetData(
    @SerializedName("items") val items: List<LiveFreeClassItem>?,
    @SerializedName("title") val title: String?,
    @SerializedName("link_text") val linkText: String?,
    @SerializedName("scroll_direction") val scrollDirection: String?,
) : WidgetData()

@Keep
data class LiveFreeClassItem(
    @SerializedName("id") val id: String?,
    @SerializedName("detail_id") val detailId: String?,
    @SerializedName("top_title") val topTitle: String?,
    @SerializedName("image_url") val imageUrl: String?,
    @SerializedName("subject") val subject: String?,
    @SerializedName("is_live") val isLive: Boolean?,
    @SerializedName("live_text") val liveText: String?,
    @SerializedName("title1") val title1: String?,
    @SerializedName("title2") val title2: String?,
    @SerializedName("students") val students: String?,
    @SerializedName("color") val color: String?,
    @SerializedName("show_reminder") val showReminder: Boolean?,
    @SerializedName("is_reminder_set") val isReminderSet: Int?,
    @SerializedName("button") val button: ButtonData?,
    @SerializedName("page") val page: String?,
    @SerializedName("start_gd") val startGd: String?,
    @SerializedName("mid_gd") val midGd: String?,
    @SerializedName("end_gd") val endGd: String?,
    @SerializedName("image_bg_card") val imageBgCard: String?,
    @SerializedName("player_type") val playerType: String?,
    @SerializedName("is_premium") val isPremium: Boolean?,
    @SerializedName("is_vip") val isVip: Boolean?,
    @SerializedName("board") val board: String?,
    @SerializedName("interested") val interested: String?,
    @SerializedName("reminder_message") val reminderMessage: String?,
    @SerializedName("live_at") val liveAt: String?,
) {
    @Keep
    data class ButtonData(
        @SerializedName("text") val text: String,
        @SerializedName("action") val action: WidgetAction?,
    )
}

class TabCourseWidgetModel : WidgetEntityModel<TabCourseWidgetData, WidgetAction>()

@Keep
data class TabCourseWidgetData(
    @SerializedName("items") val items: List<TabCourseItem>?,
    var selectedType: String? = null,
) : WidgetData()

@Keep
data class TabCourseItem(
    @SerializedName("type") val type: String,
    @SerializedName("display") val display: String?,
)

class CourseFilterWidgetModel : WidgetEntityModel<CourseFilterWidgetData, WidgetAction>()

@Keep
data class CourseFilterWidgetData(
    @SerializedName("title") val title: String?,
    @SerializedName("items") val items: List<CourseFilterItem>?,
    var selectedId: Int? = null,
) : WidgetData()

@Keep
data class CourseFilterItem(
    @SerializedName("id") val id: Int,
    @SerializedName("title") val title: String?,
)

class LiveClassDetailWidgetModel : WidgetEntityModel<LiveClassDetailWidgetData, WidgetAction>()

@Keep
data class LiveClassDetailWidgetData(
    @SerializedName("title") val title: String?,
    @SerializedName("whatsapp_share_message") val whatsappShareMessage: String?,
    @SerializedName("id") val id: String?,
    @SerializedName("course_details") val courseDetail: CourseDetail?,
    @SerializedName("payment_button") val paymentButton: PaymentButton?,
    @SerializedName("items") val items: List<LiveClassDetailItem>?,
    @SerializedName("scroll_direction") val scrollDirection: String?,
    @SerializedName("is_vip") val isVip: Boolean?,
    @SerializedName("course_id") val courseId: String?,
) : WidgetData() {
    @Keep
    data class CourseDetail(
        @SerializedName("collapse_title") val collapseTitle: String?,
        @SerializedName("faculty_name") val facultyName: String?,
        @SerializedName("image_url") val imageUrl: String?,
        @SerializedName("description") val description: String?,
        @SerializedName("students") val students: String?,
        @SerializedName("subject_course") val subjectCourse: String?,
        @SerializedName("experience") val experience: String?,
        @SerializedName("degree_college") val degreeCollege: String?,
        @SerializedName("offering") val offering: List<String>?,
        @SerializedName("resourses") val resources: List<CourseDetailResourceIcon>?,
    ) {
        @Keep
        data class CourseDetailResourceIcon(
            @SerializedName("resource_type") val resourceType: String?,
            @SerializedName("text") val text: String?,
            @SerializedName("image_url") val imageUrl: String?,
            @SerializedName("data") val data: String?,
            @SerializedName("page") val page: String?,
            @SerializedName("player_type") val playerType: String?,
            @SerializedName("is_live") val isLive: Boolean?,
        )
    }

    @Keep
    data class PaymentButton(
        @SerializedName("text") val text: String?,
        @SerializedName("action") val action: WidgetAction?,
    )
}

@Keep
data class LiveClassDetailItem(
    @SerializedName("id") val id: String,
    @SerializedName("is_vip") val isVip: Boolean?,
    @SerializedName("top_title") val topTitle: String?,
    @SerializedName("image_bg") val imageBg: String?,
    @SerializedName("color") val color: String?,
    @SerializedName("is_live") val isLive: Boolean?,
    @SerializedName("live_text") val liveText: String?,
    @SerializedName("title1") val title1: String?,
    @SerializedName("title2") val title2: String?,
    @SerializedName("students") val students: String?,
    @SerializedName("topic") val topic: String?,
    @SerializedName("page") val page: String?,
    @SerializedName("is_premium") val isPremium: Boolean?,
    @SerializedName("start_gd") val startGd: String?,
    @SerializedName("mid_gd") val midGd: String?,
    @SerializedName("end_gd") val endGd: String?,
    @SerializedName("image_bg_card") val imageBgCard: String?,
    @SerializedName("interested") val interested: String?,
    @SerializedName("live_at") val liveAt: String?,
)

class ReminderCardWidgetModel : WidgetEntityModel<ReminderCardWidgetData, WidgetAction>()

@Keep
data class ReminderCardWidgetData(
    @SerializedName("title") val textOne: String?,
    @SerializedName("title2") val textTwo: String?,
    @SerializedName("button") val buttonData: ButtonData?,
) : WidgetData() {
    @Keep
    data class ButtonData(
        @SerializedName("text") val text: String?,
        @SerializedName("action") val action: WidgetAction?,
    )
}

class LiveClassResourceWidgetModel : WidgetEntityModel<LiveClassResourceWidgetData, WidgetAction>()

@Keep
data class LiveClassResourceWidgetData(
    @SerializedName("items") val items: List<LiveClassResourceItem>?,
    @SerializedName("is_vip") val isVip: Boolean?,
    @SerializedName("course_id") val courseId: String?,
) : WidgetData()

@Keep
data class LiveClassResourceItem(
    @SerializedName("chapter") val chapter: ChapterData,
    @SerializedName("resources") val resources: ResourceData,
) {
    @Keep
    data class ChapterData(
        @SerializedName("name") val name: String,
    )

    @Keep
    data class ResourceData(
        @SerializedName("icons") val icons: List<ResourceIconData>?,
        @SerializedName("videos") val videos: List<ResourceVideoData>?,
    )

    @Keep
    data class ResourceIconData(
        @SerializedName("resource_type") val resourceType: String?,
        @SerializedName("text") val text: String,
        @SerializedName("data") val data: String,
    )

    @Keep
    data class ResourceVideoData(
        @SerializedName("resource_type") val resourceType: String,
        @SerializedName("data") val data: ResourceVideo,
    )

    @Keep
    data class ResourceVideo(
        @SerializedName("id") val id: String,
        @SerializedName("page") val page: String,
        @SerializedName("title") val title: String,
        @SerializedName("player_type") val playerType: String,
        @SerializedName("time") val time: String,
        @SerializedName("is_live") val isLive: Boolean?,
    )
}

class SimpleTextWidgetModel : WidgetEntityModel<SimpleTextWidgetData, WidgetAction>()

@Keep
data class SimpleTextWidgetData(
    @SerializedName("title") val title: String?,
    @SerializedName("margin_top") val marginTop: String?
) : WidgetData()

class TextWidgetModel : WidgetEntityModel<TextWidgetData, WidgetAction>()

@Keep
data class TextWidgetData(
    @SerializedName("title") val title: String?,
    @SerializedName("html_title") val htmlTitle: String?,
    @SerializedName("text_color") val textColor: String?,
    @SerializedName("text_size") val textSize: Float?,
    @SerializedName("background_color") val backgroundColor: String?,
    @SerializedName("stroke_color") val strokeColor: String? = null,
    @SerializedName("stroke_width") val strokeWidth: Int? = null,
    @SerializedName("corner_radius") val cornerRadius: Float? = null,
    @SerializedName("isBold") val isBold: Boolean?,
    @SerializedName("linkify") val linkify: Boolean?,
    @SerializedName("alignment") val gravity: String?,
    @SerializedName("layout_padding") val layoutPadding: LayoutPadding?,
    @SerializedName("deeplink") val deeplink: String?,
    @SerializedName("type") val type: String? = null,
    @SerializedName("force_hide_right_icon") val forceHideRightIcon: Boolean?,
    @SerializedName("image_start") val imageStart: String? = null,
    @SerializedName("image_start_width") val imageStartWidth: Int? = null,
    @SerializedName("image_start_height") val imageStartHeight: Int? = null,
    @SerializedName("start_time_in_millis")
    val startTimeInMillis: Long?,
) : WidgetData()

@Keep
data class LayoutPadding(
    @SerializedName("padding_start") val paddingStart: Int?,
    @SerializedName("padding_end") val paddingEnd: Int?,
    @SerializedName("padding_top") val paddingTop: Int?,
    @SerializedName("padding_bottom") val paddingBottom: Int?
) : WidgetData()

class AllCourseWidget2Model : WidgetEntityModel<AllCourseWidget2Data, WidgetAction>()

@Keep
data class AllCourseWidget2Data(
    @SerializedName("title") val title: String?,
    @SerializedName("link_text") val linkText: String?,
    @SerializedName("items") val items: List<AllCourseWidget2Item>?,
) : WidgetData()

@Keep
data class AllCourseWidget2Item(
    @SerializedName("id") val id: String?,
    @SerializedName("image_bg") val imageBg: String?,
    @SerializedName("image_title") val title: String?,
    @SerializedName("image_subtitle") val subTitle: String?,
    @SerializedName("bottom_title") val description: String?,
    @SerializedName("icon_url") val iconUrl: String?,
    @SerializedName("course_type") val courseType: String?,
    @SerializedName("image_bar_color") val imageBarColor: String?,
    @SerializedName("faculty") val facultyImageUrlList: List<String>?,
    @SerializedName("assortment_id") val assortmentId: String?,
    @SerializedName("deeplink") val deeplink: String?,
)

class FilterExamWidgetModel : WidgetEntityModel<FilterExamWidgetData, WidgetAction>()

@Keep
data class FilterExamWidgetData(
    @SerializedName("items") val items: List<FilterExamItem>?,
    @SerializedName("items_promo") val itemsPromo: List<FilterPromoItem>?,
    var selectedFilterId: Int = -1,
    var selectedPromoId: String? = "",
) : WidgetData()

@Keep
data class FilterExamItem(
    @SerializedName("text") val text: String,
    @SerializedName("filter_id") val filterId: Int,
)

@Keep
data class FilterPromoItem(@SerializedName("filter_id") val filterId: String)

class NotifyClassWidgetModel : WidgetEntityModel<NotifyClassWidgetData, WidgetAction>()

@Keep
data class NotifyClassWidgetData(
    @SerializedName("title") val title: String?,
    @SerializedName("bottom_title") val subtitle: String?,
    @SerializedName("button_text") val buttonText: String?,
    @SerializedName("reminder_message") val reminderMessage: String?,
    @SerializedName("id") val id: String?,
    @SerializedName("assortment_id") val assortmentId: String?,
    @SerializedName("live_at") val liveAt: String?,
    @SerializedName("reminder_link") val reminderLink: String?,
) : WidgetData()

class RelatedLecturesWidgetModel : WidgetEntityModel<RelatedLecturesWidgetData, WidgetAction>()

@Keep
data class RelatedLecturesWidgetData(
    @SerializedName("title") val title: String?,
    @SerializedName("bg_color") val bgColor: String?,
    @SerializedName("items") val items: List<RelatedLectureWidgetItem>?,
) : WidgetData()

@Keep
data class RelatedLectureWidgetItem(
    @SerializedName("live_text") val liveText: String?,
    @SerializedName("type") val type: String?,
    @SerializedName("test_id") val testId: Int?,
    @SerializedName("live_text_bg") val liveTextColor: String?,
    @SerializedName("detail_id") val detailId: String?,
    @SerializedName("play_video") val playVideo: Boolean?,
    @SerializedName("subject") val subject: String?,
    @SerializedName("title") val title: String?,
    @SerializedName("subtitle") val subtitle: String?,
    @SerializedName("topics") val topics: String?,
    @SerializedName("reminderLink") val reminderLink: String?,
    @SerializedName("reminder_message") val reminderMessage: String?,
    @SerializedName("resource_reference") val resourceId: String?,
    @SerializedName("live_date") val liveDate: String?,
    @SerializedName("live_at") val liveAt: String?,
    @SerializedName("duration") val duration: String?,
    @SerializedName("is_premium") val isPremium: Boolean?,
    @SerializedName("is_vip") val isVip: Boolean?,
    @SerializedName("id") val id: String?,
    @SerializedName("assortment_id") val assortmentId: String?,
    @SerializedName("page") val page: String?,
    @SerializedName("link") val link: String?,
    @SerializedName("image_url") val imageUrl: String?,
    @SerializedName("is_last_resource") val isLastResource: Boolean,
    @SerializedName("state") val state: Int,
    @SerializedName("deeplink") val deeplink: String?,
    @SerializedName("is_downloadable") val isDownloadable: Boolean,
    @SerializedName("payment_deeplink") val paymentDeeplink: String?,
    @SerializedName("show_emi_dialog") val showEMIDialog: Boolean?,

    )

class TopicsCoveredWidgetModel : WidgetEntityModel<TopicsCoveredWidgetData, WidgetAction>()

@Keep
data class TopicsCoveredWidgetData(
    @SerializedName("title") val title: String?,
    @SerializedName("items") val items: List<String>?,
) : WidgetData()

class LectureWidgetModel : WidgetEntityModel<LectureWidgetData, WidgetAction>()

@Keep
data class LectureWidgetData(
    @SerializedName("title") val title: String?,
    @SerializedName("items") val items: List<LectureWidgetItem>?,
) : WidgetData()

@Keep
data class LectureWidgetItem(
    @SerializedName("resource_type") val resourceType: Int?,
    @SerializedName("player_type") val playerType: String?,
    @SerializedName("title") val title: String?,
    @SerializedName("status") val status: Int?,
    @SerializedName("subject") val subject: String?,
    @SerializedName("image_url") val imageUrl: String?,
    @SerializedName("duration") val duration: String?,
    @SerializedName("live_at") val liveAt: String?,
    @SerializedName("daytime_text") val daytimeText: String?,
    @SerializedName("faculty") val faculty: String?,
    @SerializedName("topic_list") val topicList: List<String>?,
    @SerializedName("page") val page: String?,
    @SerializedName("is_live") val isLive: Boolean?,
    @SerializedName("resource_reference") val resourceReference: String?,
    @SerializedName("is_premium") val isPremium: Boolean,
    @SerializedName("is_vip") val isVip: Boolean,
)

class ResourceWidgetModel : WidgetEntityModel<ResourceWidgetData, WidgetAction>()

@Keep
data class ResourceWidgetData(
    @SerializedName("items") val items: List<ResourceWidgetItem>?,
) : WidgetData()

class NotesWidgetModel : WidgetEntityModel<NotesWidgetData, WidgetAction>()

@Keep
data class NotesWidgetData(
    @SerializedName("items") val items: ArrayList<NotesWidgetItem>?,
    @SerializedName("title") val title: String?,
    @SerializedName("scroll_direction") val scrollDirection: String?,
) : WidgetData()

@Keep
data class NotesWidgetItem(
    @SerializedName("link") val link: String,
    @SerializedName("id") val id: String?,
    @SerializedName("is_bookmark") var isBookmarked: Boolean?,
    @SerializedName("assortment_id") val assortmentId: String?,
    @SerializedName("resource_type") val resourceType: Int?,
    @SerializedName("title") val title: String?,
    @SerializedName("text") val text: String?,
    @SerializedName("image_url") val imageUrl: String?,
    @SerializedName("is_premium") val isPremium: Boolean,
    @SerializedName("is_vip") val isVip: Boolean,
    @SerializedName("payment_deeplink") val paymentDeeplink: String?,
    @SerializedName("show_emi_dialog") val showEMIDialog: Boolean?,
    @SerializedName("border_color") val borderColor: String?,
    @SerializedName("is_video_page") val isVideoPage: Boolean?,
    @SerializedName("icon_url") var iconUrl: String?,
    @SerializedName("source") val source: String?,
    @SerializedName("course_assortment_id") val courseAssortmentId: String?,
)

class LiveClassCategoryWidgetModel : WidgetEntityModel<LiveClassCategoryWidgetData, WidgetAction>()

@Keep
data class LiveClassCategoryWidgetData(
    @SerializedName("items") val items: List<LiveClassCategoryWidgetItem>?,
) : WidgetData()

@Keep
data class LiveClassCategoryWidgetItem(
    @SerializedName("image_url") val imageUrl: String?,
    @SerializedName("title") val title: String?,
    @SerializedName("id") val categoryId: String?,
)

class PurchasedClassesWidgetModel : WidgetEntityModel<PurchasedClassesWidgetData, WidgetAction>()

@Keep
data class PurchasedClassesWidgetData(
    @SerializedName("items") val items: List<PurchasedClassesWidgetItem>?,
) : WidgetData()

@Keep
data class PurchasedClassesWidgetItem(
    @SerializedName("top_title") val timestamp: String?,
    @SerializedName("title") val title: String?,
    @SerializedName("total") val progress: Float?,
    @SerializedName("resources") val resources: List<PurchasedClassesResource?>,
    @SerializedName("deeplink") val deeplink: String?,
    @SerializedName("background_image_url") val backgroundUrl: String?,
    @SerializedName("text_color") val textColor: String?,
)

@Keep
data class ResourceWidgetItem(
    @SerializedName("id") val id: String,
    @SerializedName("resource_type") val resourceType: Int?,
    @SerializedName("title") val title: String?,
    @SerializedName("image_url") val imageUrl: String?,
    @SerializedName("is_premium") val isPremium: Boolean,
    @SerializedName("is_vip") val isVip: Boolean,
)

@Keep
data class PurchasedClassesResource(
    @SerializedName("count") val count: Int?,
    @SerializedName("icon_url") val iconUrl: String?,
    @SerializedName("text") val text: String?,
    @SerializedName("text_color") val textColor: String?,
)

class CourseContentWidgetModel : WidgetEntityModel<CourseContentWidgetData, WidgetAction>()

@Keep
data class CourseContentWidgetData(
    @SerializedName("items") val items: List<CourseContentItem>?,
    @SerializedName("title") val title: String?,
    @SerializedName("scroll_direction") val scrollDirection: String?,
) : WidgetData()

@Keep
data class CourseContentItem(
    @SerializedName("id") val id: String?,
    @SerializedName("top_title") val topTitle: String?,
    @SerializedName("mid_title") val centerTitle: String?,
    @SerializedName("bottom_title") val bottomTitle: String?,
    @SerializedName("bg_img_url") val backgroundImageUrl: String?,
)

class CourseDetailWidgetModel : WidgetEntityModel<CourseDetailWidgetData, WidgetAction>()

@Keep
data class CourseDetailWidgetData(
    @SerializedName("id") val id: String?,
    @SerializedName("title") val title: String?,
    @SerializedName("subtitle") val subtitle: String?,
    @SerializedName("link_text") val linkText: String?,
    @SerializedName("hide_tab") val hideTab: Boolean?,
    @SerializedName("filters") val tabs: List<CourseDetailTabData>,
) : WidgetData()

@Keep
data class CourseDetailTabData(
    @SerializedName("id") val id: String?,
    @SerializedName("title") val title: String?,
    @SerializedName("is_selected") var isSelected: Boolean? = false,
    @SerializedName("list") val list: List<CourseDetailItem>?,
)

@Keep
data class CourseDetailItem(
    @SerializedName("id") val id: String?,
    @SerializedName("detail_id") val detailId: String?,
    @SerializedName("top_title") val topTitle: String?,
    @SerializedName("image_url") val imageUrl: String?,
    @SerializedName("subject") val subject: String?,
    @SerializedName("is_live") val isLive: Boolean?,
    @SerializedName("live_text") val liveText: String?,
    @SerializedName("title1") val title1: String?,
    @SerializedName("title2") val title2: String?,
    @SerializedName("students") val students: String?,
    @SerializedName("color") val color: String?,
    @SerializedName("show_reminder") val showReminder: Boolean?,
    @SerializedName("is_reminder_set") val isReminderSet: Int?,
    @SerializedName("button") val button: ButtonData?,
    @SerializedName("page") val page: String?,
    @SerializedName("start_gd") val startGd: String?,
    @SerializedName("mid_gd") val midGd: String?,
    @SerializedName("end_gd") val endGd: String?,
    @SerializedName("image_bg_card") val imageBgCard: String?,
    @SerializedName("player_type") val playerType: String?,
    @SerializedName("is_premium") val isPremium: Boolean?,
    @SerializedName("is_vip") val isVip: Boolean?,
    @SerializedName("board") val board: String?,
    @SerializedName("interested") val interested: String?,
    @SerializedName("reminder_message") val reminderMessage: String?,
    @SerializedName("live_at") val liveAt: String?,
) {
    @Keep
    data class ButtonData(
        @SerializedName("text") val text: String,
        @SerializedName("action") val action: WidgetAction?,
    )
}

class CourseExamTabWidgetModel : WidgetEntityModel<CourseExamTabWidgetData, WidgetAction>()

@Keep
data class CourseExamTabWidgetData(
    @SerializedName("items") val tabs: List<CourseExamTabData>,
    @SerializedName("title") val title: String?,
) : WidgetData()

@Keep
data class CourseExamTabData(
    @SerializedName("filter_id") val id: Int,
    @SerializedName("text") val title: String?,
    @SerializedName("is_selected") var isSelected: Boolean? = false,
)

class CourseTypeTabWidgetModel : WidgetEntityModel<CourseTypeTabWidgetData, WidgetAction>()

@Keep
data class CourseTypeTabWidgetData(
    @SerializedName("items") val tabs: List<CourseTypeTabData>,
) : WidgetData()

@Keep
data class CourseTypeTabData(
    @SerializedName("filter_id") val id: String,
    @SerializedName("text") val title: String?,
    @SerializedName("is_selected") var isSelected: Boolean? = false,
)

class CourseClassTabWidgetModel : WidgetEntityModel<CourseClassTabWidgetData, WidgetAction>()

@Keep
data class CourseClassTabWidgetData(
    @SerializedName("items") val tabs: List<CourseClassTabData>,
) : WidgetData()

@Keep
data class CourseClassTabData(
    @SerializedName("filter_id") val id: String,
    @SerializedName("text") val title: String?,
    @SerializedName("is_selected") var isSelected: Boolean? = false,
)

class PaymentCardListWidgetModel : WidgetEntityModel<PaymentCardListWidgetData, WidgetAction>()

@Keep
data class PaymentCardListWidgetData(
    @SerializedName("items") val items: List<PaymentCardWidgetItem>,
) : WidgetData()

@Keep
data class PaymentCardWidgetItem(
    @SerializedName("text1") val textOne: String?,
    @SerializedName("text2") val textTwo: String?,
    @SerializedName("button_text") val buttonText: String?,
    @SerializedName("variant_id") val variantId: String?,
    @SerializedName("event_name") val eventType: String?,
    @SerializedName("action") val action: WidgetAction?,
)

class PromoListWidgetModel : WidgetEntityModel<PromoWidgetData, WidgetAction>()

@Keep
data class PromoWidgetData(
    @SerializedName("items") val items: List<PromoWidgetItem>,
    @SerializedName("ratio") val ratio: String?,
    @SerializedName("auto_scroll_time_in_sec") val autoScrollTimeInSec: Long?,
    @SerializedName("margin") val margin: Boolean? = false,
    @SerializedName("parent_position") var parentPosition: Int?,
    @SerializedName("title") var title: String?,
    @SerializedName("width") val width: Int?,
    @SerializedName("height") val height: Int?,
    var selectedPagePosition: Int = 0
) : WidgetData()

@Parcelize
@Keep
data class PromoWidgetItem(
    @SerializedName("image_url") val imageUrl: String,
    @SerializedName("deeplink") val deeplink: String?,
    @SerializedName("id") val id: String?,
    @SerializedName("action") val action: WidgetAction?,
    @SerializedName("width") val width: Int?,
    @SerializedName("height") val height: Int?,
    @SerializedName("scale_type") val scaleType: String?,
) : Parcelable

@Keep
data class ApiCourseDetailData(
    @SerializedName("tab_data") val tabData: TabData?,
    @SerializedName("widgets") val widgets: List<WidgetEntityModel<*, *>>?,
    @SerializedName("title") val title: String?,
    @SerializedName("prev_month") val prevMonth: String?,
    @SerializedName("filter_widgets") val filterWidgets: List<WidgetEntityModel<*, *>>?,
    @SerializedName("single_select_filter_widget") val v2Filters: List<SearchFilter>?
)

@Keep
data class TabData(
    @SerializedName("items") val items: List<TabDataItem>?,
)

@Keep
data class TabDataItem(
    @SerializedName("id") val id: String?,
    @SerializedName("title") val title: String?,
    @SerializedName("is_selected") val isSelected: Boolean?,
)

@Keep
data class ApiSubjectDetailData(
    @SerializedName("title") val title: String?,
    @SerializedName("widgets") val widgets: List<WidgetEntityModel<*, *>>,
)

@Keep
data class NudgeData(
    @SerializedName("close_image_url") val closeImageUrl: String?,
    @SerializedName("widgets") val widgets: List<WidgetEntityModel<*, *>>,
    @SerializedName("extra_params") var extraParams: HashMap<String, Any>? = null,
    @SerializedName("bg_color") val bgColor: String?,
)

@Keep
data class CourseSelectionData(
    @SerializedName("title") val title: String?,
    @SerializedName("subtitle") val subtitle: String?,
    @SerializedName("description") val description: String?,
    @SerializedName("class_list") val classList: List<DropDownData>?,
    @SerializedName("class_text") val classText: String?,
    @SerializedName("exam_text") val examText: String?,
    @SerializedName("medium_text") val mediumText: String?,
    @SerializedName("exam_year_text") val examYearText: String?,
    @SerializedName("hindi_medium_text") val hindiMediumText: String?,
    @SerializedName("eng_medium_text") val engMediumText: String?,
    @SerializedName("medium_text_unselected") val mediumTextUnselected: String?,
    @SerializedName("medium_text_hindi") val mediumTextHindi: String?,
    @SerializedName("medium_text_english") val mediumTextEnglish: String?,
    @SerializedName("select_class_text") val selectClassText: String?,
    @SerializedName("select_exam_text") val selectExamText: String?,
    @SerializedName("select_exam_year_text") val selectExamYearText: String?,
    @SerializedName("select_medium_text") val selectMediumText: String?,
    @SerializedName("button_text") val buttonText: String?,
) {
    @Keep
    data class DropDownData(
        @SerializedName("text") val text: String?,
        @SerializedName("filter_id") val filterId: String?,
        @SerializedName("is_selected") val isSelected: Boolean?,
        @SerializedName("exam_list") val examList: List<DropDownData>?,
        @SerializedName("year_exam") val examYearList: List<DropDownData>?,
    )
}

@Keep
data class CourseListData(
    @SerializedName("title") val title: String?,
    @SerializedName("subtitle") val subtitle: String?,
    @SerializedName("description") val description: String?,
    @SerializedName("widgets") val widgets: List<WidgetEntityModel<*, *>>,
    @SerializedName("message") val message: String?
)

@Keep
data class CallbackData(
    @SerializedName("message") val message: String?,
)

@Keep
data class BookmarkData(
    @SerializedName("message") val message: String?,
    @SerializedName("icon_url") val iconUrl: String?,
)

package com.doubtnutapp.base

import android.net.Uri
import android.view.View
import androidx.annotation.StringRes
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnut.core.widgets.entities.ActionData
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnutapp.SuggestionListItem
import com.doubtnutapp.analytics.model.StructuredEvent
import com.doubtnutapp.bottomnavigation.model.BottomNavigationItemData
import com.doubtnutapp.common.model.FilterListData
import com.doubtnutapp.course.widgets.AllClassesWidgetItem
import com.doubtnutapp.course.widgets.NotesFilterItem
import com.doubtnutapp.course.widgets.PackageInfo
import com.doubtnutapp.data.dictionary.Language
import com.doubtnutapp.data.remote.models.CommentFilter
import com.doubtnutapp.data.remote.models.CourseFilterTypeData
import com.doubtnutapp.data.remote.models.PreComment
import com.doubtnutapp.data.remote.models.TextWidgetData
import com.doubtnutapp.data.remote.models.feed.FeedPostItem
import com.doubtnutapp.data.remote.models.feed.TopOptionWidgetItem
import com.doubtnutapp.data.remote.models.topicboostergame2.Friend
import com.doubtnutapp.data.remote.models.topicboostergame2.Subject
import com.doubtnutapp.dnr.widgets.DnrTotalRewardWidget
import com.doubtnutapp.domain.camerascreen.entity.SubjectEntity
import com.doubtnutapp.domain.newglobalsearch.entities.ChapterDetails
import com.doubtnutapp.domain.newglobalsearch.entities.SearchFilter
import com.doubtnutapp.domain.newglobalsearch.entities.SearchFilterItem
import com.doubtnutapp.domain.payment.entities.CardDetails
import com.doubtnutapp.domain.payment.entities.DoubtPackageInfo
import com.doubtnutapp.doubtpecharcha.model.FilterType
import com.doubtnutapp.freeclasses.widgets.FilterSortWidget
import com.doubtnutapp.home.model.QuizFeedViewItem
import com.doubtnutapp.liveclass.ui.practice_english.AnswerData
import com.doubtnutapp.liveclasshome.ui.FilterData
import com.doubtnutapp.matchquestion.model.MatchedQuestionsList
import com.doubtnutapp.matchquestion.model.ShowMoreViewItem
import com.doubtnutapp.newglobalsearch.model.*
import com.doubtnutapp.paymentplan.widgets.CheckoutV2DialogData
import com.doubtnutapp.paymentplan.widgets.CheckoutV2HeaderDialogData
import com.doubtnutapp.paymentplan.widgets.NetBankingDialogData
import com.doubtnutapp.quiztfs.widgets.DialogData
import com.doubtnutapp.similarVideo.model.SimilarTopicBoosterOptionViewItem
import com.doubtnutapp.similarVideo.model.SimilarTopicBoosterViewItem
import com.doubtnutapp.studygroup.model.ConfirmationPopup
import com.doubtnutapp.studygroup.model.StudyGroup
import com.doubtnutapp.studygroup.ui.activity.StudyGroupActivity
import com.doubtnutapp.ui.mediahelper.ExoPlayerHelper
import com.doubtnutapp.videoPage.model.VideoResource
import com.doubtnutapp.videoPage.model.ViewAnswerData
import com.doubtnutapp.widgetmanager.widgets.*
import com.doubtnutapp.widgets.countrycodepicker.model.CountryCode

class PlayVideo(
    @JvmField val videoId: String,
    @JvmField val page: String,
    @JvmField val playlistId: String,
    @JvmField val categoryTitle: String = "",
    @JvmField val resourceType: String,
    @JvmField val position: Int = -1
)

class PlayAudio(val audioUrl: String)

class PlayMatchedSolutionVideo(
    @JvmField val id: String,
    @JvmField val answerId: Long?,
    @JvmField val viewType: Int,
    @JvmField val currentPosition: Long?,
    @JvmField val showContinueWatching: Boolean?,
    @JvmField val videoResource: VideoResource?,
    @JvmField val resourceType: String,
    @JvmField val html: String?,
    @JvmField val ocrText: String?,
    @JvmField val page: String,
    @JvmField val position: Int
)

class PlayYoutubeResult(val videoId: String, val position: Int)
class OpenTopicPage(
    @JvmField val playlistId: String?,
    @JvmField val title: String?,
    @JvmField val isLast: Int,
    @JvmField val headerTitle: String
)

class OpenContestPage(@JvmField val contestId: String, @JvmField val categoryTitle: String = "")

class OpenPDFPage(
    @JvmField val actionData: ActionData?,
    @JvmField val actionActivity: String?,
    @JvmField val categoryTitle: String = ""
)

class OpenPDFViewScreen(@JvmField val pdfUrl: String)
class OpenNCERTChapter(
    @JvmField val playlistId: String,
    @JvmField val playlistTitle: String,
    @JvmField val clazz: String,
    @JvmField val categoryTitle: String = ""
)

class BannerAction(@JvmField val actionData: ActionData?, @JvmField val actionActivity: String?)

class OpenQuizDetail(
    @JvmField val quizItem: QuizFeedViewItem,
    @JvmField val categoryTitle: String = ""
)

class LikeVideo(@JvmField val videoId: String, val isLiked: Boolean)
class AddToPlayList(@JvmField val videoId: String)
class OpenPage(@JvmField val actionType: String)
class OpenWhatsapp(@JvmField val externalUrl: String)
class OpenWebView(@JvmField val title: String?, @JvmField val webPageUrl: String?)
class LaunchGame(@JvmField val title: String?, @JvmField val url: String?)
class OpenInAppSearch(val searchQuery: String?, val isVoiceSearch: Boolean = false)

class SubmitFeedBack(@JvmField val isLiked: Int, @JvmField val bgColorOfFeedView: String?)

class OnSettingOptionClicked(@JvmField val settingOptionType: String)
class OpenLibraryVideoPlayListScreen(
    @JvmField val playlistId: String,
    @JvmField val playlistName: String,
    val packageDetailsId: String? = ""
)

class OpenTestQuestionActivity(@JvmField val position: Int, @JvmField val subscriptionId: Int)

class OpenMockTestListActivity(@JvmField val position: Int)
class OpenOthersProfile(@JvmField val userId: String)
class OpenPCPopup
class OpenLiveClassFragment(@JvmField val courseId: Int)

// class OpenDetailsLiveClassActivity(@JvmField val courseId: Int, @JvmField val courseDetailId: Int, @JvmField val chapter: String)

class OpenLiveClassesOpenTopicPdf(val pdfUrl: String, val pdfName: String)

object OpenLearnMoreVideo
object ViewNowClicked
object OpenCameraFragment
object PostQuestion
object OpenDailyStreakPage
object FinishActivity
object LoadMoreRecentStatus
object UnbanRequested
object AdStatusUpdated

class FilterSubject(@JvmField val subjectName: String, @JvmField val position: Int)
class OpenLibraryPlayListActivity(
    @JvmField val playlistId: String,
    @JvmField val playlistName: String = "",
    val packageDetailsId: String? = "",
    @JvmField val position: Int = 0
)

class Filter(@JvmField val id: String, @JvmField val position: Int, @JvmField val title: String)

class NewLibrayItemClickEvent(@JvmField val title: String, @JvmField val parentTitle: String)

// class PlayYouTubeVideo(@JvmField val videoId: String, @JvmField val page: String, @JvmField val videoYouTubeId: String)

class OpenBadgeProgressDialog(
    @JvmField val badgeId: String,
    @JvmField val description: String,
    @JvmField val imageUrl: String,
    @JvmField val featureType: String,
    @JvmField val sharingMessage: String,
    @JvmField val actionPage: String
)

// Store Item Click Actions
class OpenDisabledStoreItemDialog(
    @JvmField val itemPrice: Int? = -999,
    @JvmField val redeemStatus: Int
)

class OpenBuyStoreItemDialog(
    @JvmField val resourceId: Int?,
    @JvmField val resourceType: String?,
    @JvmField val title: String?,
    @JvmField val imgUrl: String?,
    @JvmField val redeemStatus: Int,
    @JvmField val id: Int,
    @JvmField val price: Int?,
    @JvmField val isLast: Int
)

object OpenBadgesActivity
object OpenLeaderBoardActivity

class LeaderBoardItemClick(
    @JvmField val userName: String
)

object ViewLevelInfoItemClick

class TrendingRecentSearchItemClicked(val text: String, val isRecentSearch: Boolean = false)

class SearchPlaylistClicked(val playlist: SearchPlaylistViewItem, val itemPosition: Int)
class SearchTopResultClicked(
    val playlist: SearchPlaylistViewItem,
    val itemPosition: Int,
    val itemCount: Int
)

class SearchVipPlaylistClicked(val playlist: SearchPlaylistViewItem)
class SearchPlaylistClickedEvent(
    val clickedData: String,
    val clickedDataTitle: String,
    val itemId: String,
    val section: String,
    val type: String,
    val position: Int,
    val isToppersChoice: Boolean = false,
    val resultCount: Int = 0,
    val assortmentId: String,
)

class TrendingPlaylistClicked(
    val type: String,
    val searchedData: String,
    val itemPosition: Int,
    val isRecentSearch: Boolean = false
)

class TrendingPlaylistMongoEvent(
    val data: TrendingAndRecentFeedViewItem,
    val itemPosition: Int,
    val resultCount: Int,
    val isRecentSearch: Boolean = false
)

class TrendingBookClicked(
    val data: TrendingAndRecentFeedViewItem,
    val itemPosition: Int,
    val resultCount: Int
)

class TrendingCourseClicked(
    val data: TrendingVideoViewItem
)

class LiveClassLectureClicked(
    val data: SearchPlaylistViewItem,
    val itemPosition: Int,
    val resultCount: Int
)

class SearchSuggestionClicked(
    val type: String,
    val suggestionData: String,
    val itemPosition: Int,
    val id: String,
    val version: String,
    val data: SearchSuggestionItem
) {
    companion object {
        const val ACTION_ITEM_SELECTED = "ACTION_ITEM_SELECTED"
        const val ACTION_ITEM_TEXT_SUBMITTED = "ACTION_ITEM_TEXT_SUBMITTED"
    }
}

class UpdateSelectedGridItemList(val id: String, val isChecked: Boolean)

class RatingCheckboxClicked(val text: String)
class RatingCheckboxUnchecked(val text: String)
class OnSubjectChange(val subjectId: Int, val subjectName: String)

class SendHomeWidgetEvent(val eventName: String)
class ClickAction(val event: String, val hashMap: HashMap<String, String>)
class WatchLaterRequest(val id: String)
class BannerClickEvent(val event: String)
class VideoTagClick(val tagName: String, val questionId: String)
class PaymentHelpScreen(@JvmField val title: String, @JvmField val description: String)
class ShowSampleQuestion(@JvmField val subjectEntity: SubjectEntity?)
class SendConceptVideoClickEvent
class SelectPlan(@JvmField val packageInfo: PackageInfo)

class OnTopicBoosterOptionClick(val topicBoosterOptionViewItem: SimilarTopicBoosterOptionViewItem)

class UpdateSimilarTopicBoosterQuestion(
    val similarTopicBoosterViewItem: SimilarTopicBoosterViewItem,
    val topicBoosterPosition: Int,
    val correctOptionPosition: Int?,
    val wrongOptionPosition: Int?
)

class SendViewSolutionTapEvents(val eventName: String)
class PlayTopicBoosterSolutionVideo(
    @JvmField val videoId: String,
    @JvmField val page: String,
    @JvmField val playlistId: String,
    @JvmField val categoryTitle: String = "",
    @JvmField val resourceType: String
)

class PublishEvent(val event: AnalyticsEvent)
class PublishSnowplowEvent(val event: StructuredEvent)
class SendEventOfDemoAnimation(val position: Int, val isPlaying: Boolean)

// New search landing actions
class IasSortByFilterSelected(val tab: SearchTabsItem, val filterValue: SearchFilterItem)
class IasFilterSelected(
    val tab: SearchTabsItem,
    val appliedFilterMap: HashMap<String, String>,
    val isYoutube: Boolean
)

class IasFilterTypeSelected(val anchorView: View, val filterType: SearchFilter, val position: Int)

class IasFilterTypeDeselected(val filterType: SearchFilter, val position: Int)
class IasFilterValueSelected(
    val filterValue: SearchFilterItem,
    val position: Int,
    val parentPosition: Int,
    val isYoutube: Boolean
)

class IasFilterValueDeselected(
    val filterValue: SearchFilterItem,
    val position: Int,
    val parentPosition: Int,
    val isYoutube: Boolean
)

class IasClearAllFilters(val facet: SearchTabsItem, val isYoutube: Boolean)
class ShowAllFilters(val facet: SearchTabsItem, val isYoutube: Boolean)
class RemoveFilter(val filterValue: String)
class YoutubeResultsFetched(val resultCount: Int)
class IasAllChapterClicked(val chapterDetails: ChapterDetails, val resultCount: Int)
class IasFilterValuePopupStateChanged(val isVisible: Boolean)

class NewRecentSearchClicked(val text: String, val position: Int)
class NewTrendingSearchClicked(val text: String, val position: Int)
class NewTrendingRecentDoubtClicked(
    val data: TrendingVideoViewItem,
    val position: Int,
    val resultCount: Int
)

class NewTrendingMostWatchedClicked(val text: String, val position: Int)
class NewTrendingBookClicked(val text: String, val position: Int)
class NewTrendingPdfClicked(val text: String, val position: Int)
class NewTrendingSubjectClicked(
    val data: TrendingSubjectViewItem,
    val position: Int,
    val resultCount: Int
)

class NewTrendingExamPaperClicked(val text: String, val position: Int)
class FilterSelectAction(
    var filterId: Int? = null,
    var filterText: String? = null,
    val type: String = ""
)

class MultiSelectSubjectFilterClick(
    val filterId: String,
    val isSelected: Boolean
)

class CourseV2FilterApplied
class OpenFacultyPage(val facultyId: Int, val ecmId: Int?)
class SeeAllSearchResults(val tabType: String, val category: String, val tabPosition: Int)

class CourseBannerClicked(val tabType: String, val category: String)

class AdvancedFilterClicked(val tabType: String)

class DownloadPDF(val url: String)
class DownloadFailed(val message: String?)
class YoutubeVideo(val videoId: String, val title: String)
class AllChapterResultClicked(
    val item: AllClassesWidgetItem,
    val resultCount: Int,
    val position: Int
)

class ShowFilterOption(val filterData: FilterData)
class OnSelectCourseTab(val selectedType: String)
class UpcomingLiveVideo
class UpdateInterested(val id: String, val isReminder: Boolean)
class RequestVipTrial(val id: String)
class NotificationClickAction(
    val id: String?,
    val type: String?,
    val position: Int,
    val isClicked: Int?
)

class ShowMoreMatches(val position: Int, val showMoreItem: ShowMoreViewItem)

class ShowMoreSolutions

class AutoPlayComplete(
    val matchedQuestion: MatchedQuestionsList,
    val position: Int,
    val timeInMs: Long
)

class MuteAutoPlayVideo(val isMute: Boolean)
class TrackAutoPlayVideo(
    val questionId: String,
    val answerId: Long,
    val answerVideo: String,
    val engagementTime: Long
)

class ChangeViewPagerItem(val item: Int)
object AnimateBottomSheet
class SubmitLiveClassPoll(val key: String)
class OnFeedbackTagClicked(val tag: String, val showEditText: Boolean, val isTagSelected: Boolean)

class OnNotesFilterClicked(
    val id: String?,
    val isSelected: Boolean = false,
    val isMultiSelect: Boolean = false
)

class FetchDetails(val filtersList: List<String>)
class OnFeedbackClosed
class OnCommentTagClicked(val tag: PreComment)
class OnCommentEditTextClicked(val hasFocus: Boolean)
class OnBoardLanguageSelected(val locale: String, val language: String)
object OnBoardingLanguageButtonPressed

class GalleryImageClicked(val uri: Uri, val isDemoQuestion: Boolean)
object GalleryShowMoreClicked
class ImageDirectoryClicked(val bucketId: String?, val directoryName: String)

class RequestCheckout(val variantId: String, val coupon: String?)
class OnChatImageClicked(val imagePath: String?)
class SelectDoubtPlan(@JvmField val doubtPackageInfo: DoubtPackageInfo)
class ShowSaleDialog(
    val shouldShowSaleDialog: Boolean,
    val nudgeMaxCount: Int,
    val nudgeId: Int,
    val source: String
)

class BuyNowClicked
class ActivateVipTrial(val assortmentId: String)
class OnLiveScoreReceived(val liveScore: Any?)
class OnLiveScoreConnectionSuccess()
class CountryCodeItemClicked(val countryCodeData: CountryCode)
object ShowMoreSimilarVideos
class RefreshUI
class OnDeleteMessageClicked(val position: Int, val studentId: String, val postId: String)

class OnReportMessageClicked(
    val position: Int,
    val studentId: String,
    val postId: String,
    val roomId: String
)

class OnChoiceSelected(val title: String, val isChecked: Boolean, val position: Int)
class OnHomeWorkListClicked(val status: Boolean, val qid: String)
class OnInitialSuggestionClicked(val query: String)
class OnLCSSuggestionClicked(val data: SuggestionListItem, val position: Int)
class OnCourseCarouselChildWidgetItmeClicked(
    val title: String,
    val id: String,
    val position: Int,
    source: String?
)

class BottomNavigationItemClicked(val itemData: BottomNavigationItemData, val position: Int)

class TopOptionClicked(val data: TopOptionWidgetItem)
class AutoPlayVideoCompleted(val adapterPosition: Int, val delayToMoveToNext: Long)
object AutoPlayVideoStarted
class OnboardingClicked(val id: String)
object GetFollowerWidgetItems
class AddMoneyClicked(val amount: Int)
class ScheduledQuizNotificationClicked(
    val eventName: String,
    val eventParams: HashMap<String, Any>
)

class CommentFilterSelected(val commentFilter: CommentFilter)
class TopicBoosterGameQuizOptionSelected(val optionNumber: Int, val option: String = "")
class PaymentMethodClicked(
    val position: Int,
    val isSelected: Boolean,
    val isPreferredPaymentOption: Boolean
)

class CardDetailsFilled(val cardDetails: CardDetails, val isPreferredPaymentOption: Boolean)

class UpiFilled(val upi: String, val isPreferredPaymentOption: Boolean)

class NetBankingSelectedBankSelect(val bankCode: String, val isPreferredPaymentOption: Boolean)

class OtherMethodsFilled(
    val method: String,
    val type: String,
    val isPreferredPaymentOption: Boolean
)

class PayButton(val enable: Boolean, val isPreferredPaymentOption: Boolean)
class RewardClicked(val level: Int)
class MarkAttendance
class NotesFilterBySelectAction(var data: CourseFilterTypeData)
class ScheduleMonthFilterSelectAction(var data: CourseFilterTypeData)
class NcertVideoClick(val playlistId: String, val activeChapterId: String?)
class NcertBookClick(val deeplink: String?, val openNewPage: Boolean?)
class InsertChildrenAtNode(val playlistId: String, val children: List<WidgetEntityModel<*, *>>?)

class OnSimilarWidgetClick(val questionId: String, val page: String)
class OneTapBuy(val variantId: String)
class OnCourseSelected(val assortmentId: String, val categoryId: String)
class ShowDayDescription(val dayNumber: Int, val hasGift: Boolean)
class ConnectToPeer
class SubmitP2pFeedback(val studentId: String?, val rating: Float?, val reason: String?)
class CouponApplied(val couponCode: String)
class TopicClicked(val key: String, val position: Int, val topicName: String)
class UpdateTopicBoosterWidgetQuestion(
    val data: TopicBoosterWidget.Data,
    val topicBoosterPosition: Int,
    val correctOptionPosition: Int?,
    val wrongOptionPosition: Int?
)

class StudyGroupClick(
    val position: Int,
    val data: StudyGroup,
)

class FilterOptionClick(
    val key: String,
    val value: String,
)

class StudyGroupClickToShare(
    val position: Int,
    val groupId: String,
    val isSelected: Boolean
)

class SgBlockMember(
    val studentId: String?,
    val name: String?,
    val confirmationPopup: ConfirmationPopup?,
    val adapterPosition: Int,
    val roomId: String? = null,
    val actionSource: StudyGroupActivity.ActionSource? = null,
    val actionType: StudyGroupActivity.ActionType? = null
)

class SgDeleteMessage(
    val widgetType: String?,
    val deleteType: String,
    val messageId: String?,
    val millis: Long?,
    val senderId: String?,
    val confirmationPopup: ConfirmationPopup?,
    val adapterPosition: Int
)

class SgCopyMessage(
    val messageToCopy: String?,
    @StringRes val toastMessage: Int,
    @StringRes val errorMessage: Int
)

class SgRemoveReportedContainer(
    val containerId: String,
    val containerType: String,
    val adapterPosition: Int
)

class CopyLinkToClipBoard(val invitationLink: String?)
class ShareInviteLink
class OpenAudioPlayerDialog(val audioDuration: Long?, val audioUrl: String?)
class SgReportMessage(
    val messageId: String?,
    val senderId: String?,
    val millis: Long?
)

class SgReportMember(
    val reportedStudentId: String,
    val reportedStudentName: String
)

class SgChildWidgetLongClick(val type: String, val lastTouchDownXY: FloatArray)
class TextWidgetClick(val widgetData: TextWidgetData)
class OpenSgUserReportMessageFragment(val deeplink: String?)
class HandleDeeplink(val deeplink: String?)
class SgFriendSelected(val friend: Friend)
class SgRequestPrimaryCtaClick(val adapterPosition: Int, val deeplink: String?)
class SgRequestSecondaryCtaClick(val adapterPosition: Int, val deeplink: String?)
class SgMemberLongPress(
    val adminStatus: Int?,
    val studentId: String,
    val view: View,
    val viewPosition: Int
)

class FilterSelectionAction(val lastSelectedItem: NotesFilterItem)
class ExamSelectionAction(val lastSelectedItem: NotesFilterItem)
class OnNudgeClosed(val nudgeId: String)

class RemoveP2PHomeWidget(val widgetType: String)
class OnNudgeClicked

class FeedPinnedVideoItemVisible(val feedItem: FeedPostItem?)
class FeedPremiumVideoItemVisible(val feedItem: FeedPostItem?)
class FeedPremiumBlockedScreenVisible(val viewAnswerData: ViewAnswerData)
class FeedDNVideoWatched(
    val feedItem: FeedPostItem,
    val videoEngagementStats: ExoPlayerHelper.VideoEngagementStats
)

class RemoveWidget(val widget: WidgetEntityModel<*, *>?)
class CourseRecommendationRadioButtonSelected(
    val submitId: String,
    val responseId: String,
    val response: String
)

class TbgSubjectClicked(val subject: Subject)
class TbgInviteeSelected(val isSelected: Boolean, val studentId: Long)
class TbgChapterClicked(val pair: Pair<String, Int>)
object TbgLevelInfoClicked
class TbgEmojiClicked(val emoji: String)
class TbgQuizHistoryItemClicked(val deeplink: String)

class ShowWhatsappAdminForm
class SubmitWhatsappAdminForm(
    val mobile: String,
    val name: String,
    val state: String,
    val district: String,
    val friendsCount: Int
)

class DropDownListItemSelected(val value: String, val clickedPosition: Int, val listType: String)

class TrySampleQuestion(val imageUrl: String)

class OpenDictionaryLangaugeBottomSheet(val languageArray: ArrayList<Language>)
class OnDictionaryLangaugeSelected(val langauge: Language)
class SearchWordMeaning(val word: String)

class DfPreviousDoubtsViewAllClick(val data: DoubtFeedWidget.Data)
class OnContentFilterSelect(val filter: String)
class OnMediumSelected(val selectedAssortmentId: String)
class OnPdfDownloadOrShareClick(val pdfUrl: String, val type: String)

class RcStatsViewAllClick(val deeplink: String?, val title: String?)
class RcResultHistoryWatchSolutionClick(val deeplink: String?)
class RcTestListItemClicked(val deeplink: String?)

class QuizTFSSingleOptionSelected(val position: Int, val option: String)
class QuizTFSMultiOptionSelected(val position: Int, val option: String)

class Dismiss(val from: String? = null, val to: String? = null)
class PerformDeeplinkAction(val from: String? = null, val to: String? = null)

class ScratchCardClicked(val dialogData: DialogData)

class ShowCheckoutV2Dialog(val data: CheckoutV2HeaderDialogData)

class ScrollToPosition(val position: Int, val itemPosition: Int)

class SubscribeChannel(val teacherId: String, val isSubscribe: Int)

class OnNetBankingClick(
    val data: CheckoutV2DialogData?,
    var delay: Long,
    val parentPosition: Int,
    val childPosition: Int,
    val ignoreAction: Boolean
)

class OnSelectedNetBankingClick(
    val method: String,
    val code: String,
    var delay: Long,
    val parentPosition: Int,
    val childPosition: Int,
    val ignoreAction: Boolean
)

class OnDnWalletPaymentClick(val method: String, var delay: Long)
class ShowTeacherProfile(val teacherId: String, val channelHeading: String?)
class CloseTeacherProfile()

class ChannelTabFilterSelected(
    val filter: ChannelTabFilterWidget.ChannelFilterTab,
    val position: Int
)

class ChannelSubTabFilterSelected(
    val filter: ChannelSubTabFilterWidget.ChannelFilterData,
    val position: Int
)

class ChannelContentFilterSelected(
    val filter: ChannelContentFilterWidget.ChannelFilterData,
    val position: Int
)

class OnWalletClick(
    val data: CheckoutV2DialogData?,
    var delay: Long,
    val parentPosition: Int,
    val childPosition: Int,
    val ignoreAction: Boolean
)

class OnCardMethodClick(
    val data: CheckoutV2DialogData?,
    var delay: Long,
    val parentPosition: Int,
    val childPosition: Int,
    val ignoreAction: Boolean
)

class OnUpiCollectClick(
    val data: CheckoutV2DialogData?,
    var delay: Long,
    val parentPosition: Int,
    val childPosition: Int,
    val ignoreAction: Boolean
)

class OnUpiPaymentClick(
    val method: String,
    var delay: Long,
    val parentPosition: Int,
    val childPosition: Int,
    val ignoreAction: Boolean
)

class OnVideoTabSelected(val tabId: String)
class OnNotesClicked(val link: String?, val id: String?, val iconUrl: String?)
class OnVideoOffsetClicked(val offset: Long?)
class OnPaytmWalletPaymentClick(
    val method: String,
    var type: String,
    var delay: Long,
    val parentPosition: Int,
    val childPosition: Int,
    val ignoreAction: Boolean
)

object OnAddCouponClick
object OnRemoveCouponClick

class OnDeeplinkPaymentClick(
    var delay: Long,
    val parentPosition: Int,
    val childPosition: Int,
    val deeplink: String,
    val ignoreAction: Boolean
)

class OnPaymentLinkShareClick(
    val method: String,
    var delay: Long,
    val parentPosition: Int,
    val childPosition: Int,
    val ignoreAction: Boolean
)

class OnQrPaymentClick(
    val method: String,
    var delay: Long,
    val parentPosition: Int,
    val childPosition: Int,
    val ignoreAction: Boolean
)

class OnCardPaymentClick(
    val cardDetails: CardDetails
)

class OnWalletPaymentClick(
    val method: String,
    val code: String,
    val name: String
)

class OnNetBankingPaymentClick(
    val method: String,
    val code: String
)

class OnUpiCollectPaymentClick(
    val method: String,
    val upiId: String
)

class OnAudioToggle(
    val state: Boolean,
    val audioUrl: String?
)

class OnBankSelectorClicked(val data: NetBankingDialogData?)

class OnWalletToggle(val key: String, val isSelected: Boolean)
class DnrTncClicked(val deeplink: String?, val data: DnrTotalRewardWidget.TncDialogData?)
class DnrOpenWebUrl(val url: String?)

class GetDoubtSolutions(val id: String)

// Practice English
class SubmitSolutionClicked(val answerData: AnswerData)
class TryAgainClicked()
class NextQuestionClicked()
class OptionSelected(val position: Int)
class OnNotesClosed()
class OnHomeworkSubmitted(val id: String)
class OnNextVideoClicked()
class LibraryExamWidgetClick(val id: String, val isChecked: Boolean)
class LibraryWidgetClick(val id: String, val tabIds: List<String>?, val position: Int)

class ClickOnWidgetAction
class SeeDoubtsAction
class OnFilterButtonClicked(val key: String, val value: String)
class OnMultiSelectFilterButtonClicked(val key: String, val value: String)
class OnCategoryFilterApplied(val map: HashMap<String, MutableList<String>>)
class ApplyFilters(
    val bottomSheetType: FilterSortWidget.FilterType,
    val filterName: String,
    val selectedFilters: List<String>
)

class TwoTextsVerticalTabWidgetTabChanged(val tabSelected: String)
class OnFreeLiveClassFilterClicked(val type: String, val deeplink: String? = null)

class AskPermission()
class OnFreeCourseWidgetClicked(val id: Int, val deeplinkForCourseSelected: String?)
class OnAutoPostItemSelected(val id: String)

class MatchPageFeatureAction(val actionToPerform: String?)
class CourseFragmentCloseClicked()
class OnFilterSelected(
    val type: FilterType?,
    val values: java.util.ArrayList<FilterListData.FilterListItem>
)

class OnDoubtPeCharchaRewardCtaClicked(val rewardsData: GradientBannerWithActionButtonWidget.RewardsData?)
class OnDoubtPeCharchaQuestionShareButtonClicked()
class OnSolveNowButtonClicked(val createdTimeLong: Long)
class OnSolutionAccepted(val createdTimeLong: Long, val studentId: String?)
class OnSolutionRejected(val createdTimeLong: Long, val studentIdOfOriginalMessage: String?)
class OnSolveNowAfterRejection(val createdTimeLong: Long)
class OnSolutionMarkedAsFinal(val createdTimeLong: Long)
class OnThisIsNotMyAnswerClicked(val createdTimeLong: Long)
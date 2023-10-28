package com.doubtnutapp.data.remote.repository

import com.doubtnut.core.entitiy.BaseResponse
import com.doubtnutapp.ProgressRequestBody
import com.doubtnutapp.data.remote.RetrofitLiveData
import com.doubtnutapp.data.remote.api.services.TeslaService
import com.doubtnutapp.data.remote.models.ApiResponse
import com.doubtnutapp.data.remote.models.SignedUrl
import com.doubtnutapp.data.remote.models.SubmitPost
import com.doubtnutapp.data.remote.models.userstatus.StatusApiResponse
import com.doubtnut.core.widgets.entities.WidgetAction
import com.doubtnut.core.widgets.entities.WidgetData
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnutapp.feed.view.FeedFragment
import com.doubtnutapp.model.FeedApiResponse
import com.doubtnutapp.toRequestBody
import io.reactivex.Single
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import okhttp3.RequestBody

class TeslaRepository(private val teslaService: TeslaService) {

    fun getSignedUrl(
        contentType: String,
        fileName: String,
        fileExt: String,
        mimeType: String
    ): Single<ApiResponse<SignedUrl>> =
        teslaService.getSignedUrl(contentType, fileName, fileExt, mimeType)

    fun uploadAttachment(url: String, progressRequestBody: ProgressRequestBody) =
        teslaService.uploadAttachment(url, progressRequestBody)

    fun createPost(submitPost: SubmitPost) = teslaService.submitPost(submitPost)

    fun starPost(postId: String, postTopic: String?) = teslaService.starPost(postId, postTopic)
    fun unstarPost(postId: String, postTopic: String?) = teslaService.unstarPost(postId, postTopic)

    fun likePost(postId: String, postTopic: String?) = teslaService.likePost(postId, postTopic)
    fun unlikePost(postId: String, postTopic: String?) = teslaService.unlikePost(postId, postTopic)

    fun reportPost(postId: String) = teslaService.reportPost(
        HashMap<String, String>().apply {
            put("post_id", postId)
        }
    )

    fun deletePost(postId: String) = teslaService.deletePost(
        HashMap<String, String>().apply {
            put("post_id", postId)
        }
    )

//    fun mutePostNotification(requestBody: RequestBody) = teslaService.mutePostNotification(requestBody)

    fun muteFeedPostNotification(requestBody: RequestBody) =
        teslaService.muteNotification(requestBody)

    fun followUser(studentId: String): Single<Unit> {
        val params = HashMap<String, String>().apply {
            put("student_id", studentId)
            put("connection_type", "Follow")
        }
        return teslaService.followUser(params)
    }

    fun unfollowUser(studentId: String): Single<Unit> {
        val params = HashMap<String, String>().apply {
            put("student_id", studentId)
            put("connection_type", "UnFollow")
        }
        return teslaService.unfollowUser(params)
    }

    suspend fun getFeed(
        page: Int,
        source: String,
        offsetCursor: String,
        assortmentId: String
    ): Flow<ApiResponse<FeedApiResponse>> =
        flow {
            emit(
                teslaService.getV3Feed(
                    page, source, 1, offsetCursor, source == FeedFragment.SOURCE_HOME,
                    assortmentId = assortmentId
                )
            )
        } /*True for new home page */

    suspend fun getPinnedPost(): Flow<ApiResponse<WidgetEntityModel<WidgetData, WidgetAction>>> =
        flow { emit(teslaService.getPinnedPost()) }

    fun getPost(postId: String) = teslaService.getPost(postId)

    fun getUserProfile(studentId: String, authPageOpenCount: Int) =
        teslaService.getUserProfile(studentId, authPageOpenCount)

    fun getUserFeed(page: Int, studentId: String) = teslaService.getUserFeed(studentId, page, 1)

    fun getUserFavouritesFeed(page: Int, studentId: String) =
        teslaService.getUserFavouritesFeed(studentId, page, 1)

    fun getCreatePostMeta() = teslaService.getCreatePostMeta()

    fun getPopularTopicFeed(page: Int, topic: String) =
        teslaService.getPopularTopicFeed(topic, page, "popular", 1)

    fun getRecentTopicFeed(page: Int, topic: String) =
        teslaService.getRecentTopicFeed(topic, page, "recent", 1)

    fun getLiveFeed(page: Int) = teslaService.getLiveFeed("live", page)

    fun getLiveUpcomingFeed(page: Int) = teslaService.getLiveFeed("upcoming", page)

    fun getLiveStreamUrl(postId: String) = teslaService.getLiveStreamUrl(postId)

    fun endLiveStream(postId: String) = teslaService.endLiveStream(postId)

    fun getLiveViewerCount(postId: String) = teslaService.getLiveViewerCount(postId)

    fun requestUserVerification(reason: String): RetrofitLiveData<ApiResponse<Any>> {
        return teslaService.requestUserVerification(
            hashMapOf<String, String>().apply {
                put("reason", reason)
            }
        )
    }

    fun getUserVerification() = teslaService.getUserVerification()

    fun liveViewerJoined(postId: String) = teslaService.liveViewerJoined(postId)
    fun liveViewerLeft(postId: String) = teslaService.liveViewerLeft(postId)
    fun postDNActivityGame(gameId: String) = teslaService.postDNActivityGame(gameId)

    fun postPollAction(requestBody: RequestBody) = teslaService.postPoll(requestBody)

    fun getStatusAds(): Single<ApiResponse<StatusApiResponse>> = teslaService.getStatusAds()

    suspend fun getCreatePostViewsVisibilityForFeed() =
        teslaService.getCreatePostActionViewsVisibilityStatus().data;

    suspend fun getAllOneTapPosts(page: String, carouselType: String?) =
        teslaService.getOneTapPosts(page, carouselType).data

    suspend fun createOneTapPost(id: String): BaseResponse {
        val map = HashMap<String, Any>()
        map["post_id"] = id
        return teslaService.createOneTapPost(map.toRequestBody())
    }
}

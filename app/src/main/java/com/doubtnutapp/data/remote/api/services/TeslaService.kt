package com.doubtnutapp.data.remote.api.services

import com.doubtnut.core.entitiy.BaseResponse
import com.doubtnutapp.data.remote.RetrofitLiveData
import com.doubtnutapp.data.remote.models.*
import com.doubtnutapp.data.remote.models.feed.CreatePostMeta
import com.doubtnutapp.data.remote.models.feed.FeedPostItem
import com.doubtnutapp.data.remote.models.userstatus.StatusApiResponse
import com.doubtnut.core.widgets.entities.WidgetAction
import com.doubtnut.core.widgets.entities.WidgetData
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnutapp.feed.entity.CreatePostVisibilityStatusResponse
import com.doubtnutapp.feed.entity.OneTapPostsResponse
import com.doubtnutapp.feed.entity.TopIconsData
import com.doubtnutapp.icons.widgets.FavouriteExploreCardWidget
import com.doubtnutapp.model.FeedApiResponse
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import okhttp3.RequestBody
import retrofit2.http.*

interface TeslaService {

    @GET("v1/tesla/get-signed-upload-url")
    fun getSignedUrl(
        @Query("content_type") contentType: String,
        @Query("file_name") fileName: String,
        @Query("file_ext") fileExt: String,
        @Query("mime_type") mimeType: String
    ): Single<ApiResponse<SignedUrl>>

    @POST("v1/tesla/post-feed")
    fun submitPost(@Body submitPost: SubmitPost): Single<ApiResponse<FeedPostItem>>

    @PUT
    fun uploadAttachment(@Url url: String, @Body requestBody: RequestBody): Single<Unit>

    @GET("v3/tesla/feed")
    suspend fun getV3Feed(
        @Query("page") page: Int,
        @Query("source") source: String,
        @Query("with_video_type") includeVideo: Int,
        @Query("offsetCursor") offsetCursor: String,
        @Query("home_page_experiment") homePageExperiment: Boolean,
        @Query("supported_media_type") supportedMediaTypes: String = listOf(
            "DASH",
            "HLS",
            "RTMP",
            "BLOB",
            "YOUTUBE"
        ).joinToString(","),
        @Query("user_assortment") assortmentId: String,
        @Query("force_show_all_categories") forceShowAllCategories: Boolean = FavouriteExploreCardWidget.forceShowAllCategories,
    ): ApiResponse<FeedApiResponse>

    @GET("v3/tesla/pinnedposts")
    suspend fun getPinnedPost(
        @Query("supported_media_type") supportedMediaTypes: String = listOf(
            "DASH",
            "HLS",
            "RTMP",
            "BLOB"
        ).joinToString(","),
    ): ApiResponse<WidgetEntityModel<WidgetData, WidgetAction>>

    @GET("v3/tesla/post/{post_id}")
    fun getPost(
        @Path("post_id") postId: String,
        @Query("supported_media_type") supportedMediaTypes: String = listOf(
            "DASH",
            "HLS",
            "RTMP",
            "BLOB",
            "YOUTUBE"
        ).joinToString(",")
    ): RetrofitLiveData<ApiResponse<WidgetEntityModel<WidgetData, WidgetAction>>>

    @GET("v1/tesla/bookmark/{post_id}")
    fun starPost(
        @Path("post_id") postId: String,
        @Query("topic") topic: String?,
        @Query("vote") vote: String = "1"
    ): Single<Unit>

    @GET("v1/tesla/bookmark/{post_id}")
    fun unstarPost(
        @Path("post_id") postId: String,
        @Query("topic") topic: String?,
        @Query("vote") vote: String = "0"
    ): Single<Unit>

    @POST("v1/tesla/rate/{post_id}")
    fun likePost(
        @Path("post_id") postId: String,
        @Query("topic") topic: String?,
        @Query("vote") vote: String = "1"
    ): Single<Unit>

    @POST("v1/tesla/rate/{post_id}")
    fun unlikePost(
        @Path("post_id") postId: String,
        @Query("topic") topic: String?,
        @Query("vote") vote: String = "0"
    ): Single<Unit>

    @POST("v1/tesla/report")
    fun reportPost(@Body paramMap: HashMap<String, String>): Single<Unit>

    @POST("v1/tesla/delete")
    fun deletePost(@Body paramMap: HashMap<String, String>): Single<Unit>

    @POST("v2/feed/unsubscribe")
    fun mutePostNotification(@Body params: RequestBody): Single<ApiResponse<MuteNotification>>

    @POST("v2/comment/mute")
    fun muteNotification(@Body params: RequestBody): Single<ApiResponse<MuteNotification>>

    @POST("v1/tesla/follow")
    fun followUser(@Body paramMap: HashMap<String, String>): Single<Unit>

    @POST("v1/tesla/follow")
    fun unfollowUser(@Body paramMap: HashMap<String, String>): Single<Unit>

    @GET("v1/tesla/profile/{student_id}")
    fun getUserProfile(
        @Path("student_id") studentId: String,
        @Query("social_auth_display_count") authPageOpenCount: Int,
    ): RetrofitLiveData<ApiResponse<ProfileData>>

    @GET("v3/tesla/userposts/{student_id}")
    fun getUserFeed(
        @Path("student_id") studentId: String,
        @Query("page") page: Int,
        @Query("with_video_type") includeVideo: Int
    ): RetrofitLiveData<ApiResponse<List<WidgetEntityModel<WidgetData, WidgetAction>>>>

    @GET("v3/tesla/userbookmarks/{student_id}")
    fun getUserFavouritesFeed(
        @Path("student_id") studentId: String,
        @Query("page") page: Int,
        @Query("with_video_type") includeVideo: Int
    ): RetrofitLiveData<ApiResponse<List<WidgetEntityModel<WidgetData, WidgetAction>>>>

    @GET("v1/tesla/createpost/meta")
    fun getCreatePostMeta(): Single<ApiResponse<CreatePostMeta>>

    @GET("v3/tesla/topicpost/{topic}")
    fun getPopularTopicFeed(
        @Path("topic") topic: String,
        @Query("page") page: Int,
        @Query("filter") filter: String,
        @Query("with_video_type") includeVideo: Int
    ): RetrofitLiveData<ApiResponse<List<WidgetEntityModel<WidgetData, WidgetAction>>>>

    @GET("v3/tesla/topicpost/{topic}")
    fun getRecentTopicFeed(
        @Path("topic") topic: String,
        @Query("page") page: Int,
        @Query("filter") filter: String,
        @Query("with_video_type") includeVideo: Int
    ): RetrofitLiveData<ApiResponse<List<WidgetEntityModel<WidgetData, WidgetAction>>>>

    @GET("v1/tesla/livepost/{post_id}/streamurl")
    fun getLiveStreamUrl(
        @Path(value = "post_id") postId: String
    ): Single<ApiResponse<String>>

    @GET("v1/tesla/livepost/{post_id}/endstream")
    fun endLiveStream(
        @Path(value = "post_id") postId: String
    ): Single<ApiResponse<Any>>

    @GET("v3/tesla/livepost/{filter}")
    fun getLiveFeed(
        @Path(value = "filter") filter: String,
        @Query("page") page: Int
    ): RetrofitLiveData<ApiResponse<List<WidgetEntityModel<WidgetData, WidgetAction>>>>

    @POST("v1/tesla/livepost/verification/request")
    fun requestUserVerification(@Body paramMap: HashMap<String, String>): RetrofitLiveData<ApiResponse<Any>>

    @GET("v1/tesla/livepost/isverified/check")
    fun getUserVerification(): Single<ApiResponse<UserVerificationInfo>>

    @GET("v1/tesla/livepost/{post_id}/viewer_count")
    fun getLiveViewerCount(
        @Path(value = "post_id") postId: String
    ): Observable<ApiResponse<Any>>

    @GET("v1/tesla/livepost/{post_id}/viewer_join")
    fun liveViewerJoined(
        @Path(value = "post_id") postId: String
    ): Single<ApiResponse<Any>>

    @GET("v1/tesla/livepost/{post_id}/viewer_left")
    fun liveViewerLeft(
        @Path(value = "post_id") postId: String
    ): Single<ApiResponse<Any>>

    @GET("v1/tesla/gamePost/{game_Id}/start")
    fun postDNActivityGame(
        @Path(value = "game_Id") gameId: String
    ): Completable

    @POST("/v2/feed/submit-poll")
    fun postPoll(
        @Body params: RequestBody
    ): Single<ApiResponse<PollingResultResponse>>

    @GET("/v1/stories/ads")
    fun getStatusAds(): Single<ApiResponse<StatusApiResponse>>

    @GET("v1/tesla/post_create_status")
    suspend fun getCreatePostActionViewsVisibilityStatus(): ApiResponse<CreatePostVisibilityStatusResponse>

    @GET("v1/tesla/one-tap-post")
    suspend fun getOneTapPosts(
        @Query("page") page: String?,
        @Query("carousel_type") carouselType: String?
    ): ApiResponse<OneTapPostsResponse>

    @POST("v1/tesla/one-tap-post")
    suspend fun createOneTapPost(@Body body: RequestBody): BaseResponse
}

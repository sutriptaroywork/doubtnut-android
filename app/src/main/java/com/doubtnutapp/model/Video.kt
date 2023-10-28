package com.doubtnutapp.model

import android.os.Parcelable
import androidx.annotation.Keep
import com.doubtnutapp.data.newlibrary.model.ApiVideoObj
import com.doubtnutapp.domain.common.entities.model.VideoEntity
import com.doubtnutapp.domain.videoPage.entities.ApiVideoResource
import com.doubtnutapp.videoPage.model.VideoResource
import kotlinx.android.parcel.Parcelize

@Parcelize
@Keep
data class Video(
        val questionId: String?,
        var autoPlay: Boolean,
        var viewId: String? = "",
        var videoResources: List<VideoResource>?,
        var videoPosition: Long? = 0,
        var videoPage: String? = "",
        val showFullScreen: Boolean?,
        val aspectRatio: String?
) : Parcelable {

    companion object {
        fun fromVideoEntity(videoEntity: VideoEntity?): Video? {
            if (videoEntity == null) return null
            return Video(
                    questionId = videoEntity.questionId,
                    autoPlay = videoEntity.autoPlay,
                    viewId = videoEntity.viewId,
                    videoResources = videoEntity.videoResources?.map { getVideoResource(it) },
                    showFullScreen = videoEntity.showFullScreen,
                    aspectRatio = videoEntity.aspectRatio
            )
        }

        /*
        ideally, we shouldn't need this but timeline feed is currently not adhering to
        clean architecture and is directly using ApiVideoObj from data models
         */
        fun fromVideoObj(videoEntity: ApiVideoObj?): Video? {
            if (videoEntity == null) return null
            return Video(
                    questionId = videoEntity.questionId,
                    autoPlay = videoEntity.autoPlay ?: false,
                    viewId = videoEntity.viewId,
                    videoResources = videoEntity.resources?.map { getVideoResource(it) },
                    showFullScreen = videoEntity.showFullScreen,
                    aspectRatio = videoEntity.aspectRatio
            )
        }

        private fun getVideoResource(apiVideoResource: ApiVideoResource) = with(apiVideoResource) {
            VideoResource(
                    resource = resource,
                    drmScheme = drmScheme,
                    drmLicenseUrl = drmLicenseUrl,
                    mediaType = mediaType,
                    isPlayed = false,
                    dropDownList = dropDownList?.map {
                        VideoResource.PlayBackData(
                                resource = it.resource, drmScheme = it.drmScheme,
                                drmLicenseUrl = it.drmLicenseUrl,
                                mediaType = it.mediaType, display = it.display)
                    },
                    timeShiftResource = timeShiftResource?.let {
                        VideoResource.PlayBackData(
                                resource = it.resource, drmScheme = it.drmScheme,
                                drmLicenseUrl = it.drmLicenseUrl,
                                mediaType = it.mediaType, display = it.display)
                    },
                    offset = offset
            )
        }
    }
}


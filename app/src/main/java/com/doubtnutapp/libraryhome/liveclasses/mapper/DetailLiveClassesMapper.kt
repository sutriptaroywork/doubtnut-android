package com.doubtnutapp.libraryhome.liveclasses.mapper

import com.doubtnutapp.R
import com.doubtnutapp.common.promotional.model.PromotionalActionDataViewItem
import com.doubtnutapp.data.base.Mapper
import com.doubtnutapp.domain.liveclasseslibrary.entities.*
import com.doubtnutapp.libraryhome.liveclasses.model.*
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.reflect.TypeToken
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DetailLiveClassesMapper @Inject constructor() :
    Mapper<DetailLiveClassEntity, List<LiveClassesFeedViewItem>> {

    override fun map(srcObject: DetailLiveClassEntity): List<LiveClassesFeedViewItem> {
        val dataList = mutableListOf<LiveClassesFeedViewItem>()
        dataList.addAll(getPdfList(srcObject.pdfList))
        dataList.add(getTimerViewItem(srcObject))
        dataList.addAll(getLiveClassResourceList(srcObject.liveClassList))
        dataList.addAll(getVideoList(srcObject.videoList))
        return dataList
    }

    private fun getPdfList(pdfList: List<DetailLiveClassPdfEntity>): List<PdfViewItem> =
        pdfList.map {
            getPdfViewItem(it)
        }

    private fun getLiveClassResourceList(resourceList: List<LiveClassResourceEntity>): List<LiveClassResourceViewItem> =
        resourceList.map {
            LiveClassResourceViewItem(
                it.type,
                it.title,
                it.qId,
                it.imageUrl,
                it.liveAt,
                it.topicList,
                it.page,
                R.layout.item_live_class_resource
            )
        }

    private fun getPdfViewItem(pdfEntity: DetailLiveClassPdfEntity): PdfViewItem =
        with(pdfEntity) {
            PdfViewItem(
                name,
                imageLink,
                pdfLink,
                R.layout.item_detail_live_class_pdf
            )
        }

    private fun getTimerViewItem(entity: DetailLiveClassEntity): TimerViewItem =
        with(entity) {
            TimerViewItem(
                chapterName,
                videoCount,
                liveStatus,
                timer,
                R.layout.item_live_class_timer
            )
        }

    private fun getVideoList(videoList: List<LiveClassesCourseItem>): List<LiveClassesFeedViewItem> =
        videoList.map {
            when (it) {
                is DetailLiveClassVideoEntity -> getVideoViewItem(it)

                is DetailLiveClassBanner -> getBannerViewItem(it)

                else -> throw IllegalArgumentException("Wrong detail structure class type")
            }
        }

    private fun getVideoViewItem(videoEntity: DetailLiveClassVideoEntity): VideoViewItem =
        with(videoEntity) {
            VideoViewItem(
                type,
                title,
                status,
                qId,
                youtubeId ?: "",
                imageUrl,
                duration,
                liveAt,
                dayText,
                timeText,
                resourceType,
                topicList,
                mapPageData(pageData) as HashMap<String, String>,
                event,
                R.layout.item_live_class_video
            )
        }

    private fun mapPageData(jsonObject: JsonObject?): HashMap<String, String>? {
        return if (jsonObject != null)
            Gson().fromJson(
                jsonObject.toString(),
                object : TypeToken<HashMap<String, String>>() {}.type
            )
        else
            null
    }

    private fun getBannerViewItem(detailLiveClassBanner: DetailLiveClassBanner): BannerViewItem =
        with(detailLiveClassBanner) {
            BannerViewItem(
                type = type,
                imageUrl = imageUrl,
                resourceType = resourceType,
                isLast = isLast,
                actionActivity = actionActivity,
                actionData = actionData?.let {
                    PromotionalActionDataViewItem(
                        playlistId = it.playlistId,
                        playlistTitle = it.playlistTitle,
                        isLast = it.isLast,
                        facultyId = it.facultyId,
                        ecmId = it.ecmId,
                        subject = it.subject
                    )
                },
                viewType = R.layout.item_detail_live_class_banner
            )
        }
}
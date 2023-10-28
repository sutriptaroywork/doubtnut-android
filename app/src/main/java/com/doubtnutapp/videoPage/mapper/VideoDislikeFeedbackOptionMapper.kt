package com.doubtnutapp.videoPage.mapper

import com.doubtnutapp.data.base.Mapper
import com.doubtnutapp.domain.videoPage.entities.VideoDislikeFeedbackOptionEntity
import com.doubtnutapp.videoPage.model.VideoDislikeFeedbackOption
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VideoDislikeFeedbackOptionMapper @Inject constructor() :
    Mapper<List<VideoDislikeFeedbackOptionEntity>, List<VideoDislikeFeedbackOption>> {

    override fun map(srcObject: List<VideoDislikeFeedbackOptionEntity>): List<VideoDislikeFeedbackOption> =
        with(srcObject) {
            this.map {
                VideoDislikeFeedbackOption(
                    it.content,
                    false,
                    true
                )
            }
        }

}
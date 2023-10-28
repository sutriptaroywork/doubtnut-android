package com.doubtnutapp.matchquestion.mapper

import com.doubtnutapp.data.base.Mapper
import com.doubtnutapp.matchquestion.model.MatchQuestionBanner
import com.doubtnutapp.matchquestion.model.MatchQuestionBannerEntityData
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MatchQuestionBannerMapper @Inject constructor() :
    Mapper<MatchQuestionBannerEntityData, MatchQuestionBanner> {

    override fun map(srcObject: MatchQuestionBannerEntityData): MatchQuestionBanner =
        with(srcObject) {
            MatchQuestionBanner(
                content,
                dnCash
            )
        }
}
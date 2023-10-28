package com.doubtnutapp.data.homefeed

import com.doubtnutapp.data.library.mapper.ClassEntityMapper
import com.doubtnutapp.domain.homefeed.interactor.IncompleteChapterWidgetData
import com.doubtnutapp.domain.homefeed.repository.HomeScreenRepository
import com.doubtnutapp.domain.library.entities.ClassListEntity
import com.google.gson.JsonObject
import io.reactivex.Completable
import io.reactivex.Single
import javax.inject.Inject

class HomeScreenRepositoryImpl @Inject constructor(
    private val homeScreenApiService: HomeScreenApiService,
    private val classMapper: ClassEntityMapper
) : HomeScreenRepository {

    override fun submitStudentRating(rating: Int): Completable =
        homeScreenApiService.submitStudentRating(rating.toString())

    override fun submitStudentRatingFeedback(feedback: JsonObject): Completable =
        homeScreenApiService.submitStudentRatingFeedback(feedback)

    override fun studentRatingCross(): Completable =
        homeScreenApiService.studentRatingCross()

    override fun getClassesList(): Single<ClassListEntity> =
        homeScreenApiService.getClassesList()
            .map {
                classMapper.map(it.data)
            }

    override fun getIncompleteChapterList(): Single<IncompleteChapterWidgetData> {
        return homeScreenApiService.getIncompleteChapter().map { it.data }
    }
}

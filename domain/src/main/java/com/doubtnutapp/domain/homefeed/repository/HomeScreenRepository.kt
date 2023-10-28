package com.doubtnutapp.domain.homefeed.repository

import com.doubtnutapp.domain.homefeed.interactor.IncompleteChapterWidgetData
import com.doubtnutapp.domain.library.entities.ClassListEntity
import com.google.gson.JsonObject
import io.reactivex.Completable
import io.reactivex.Single

interface HomeScreenRepository {

    fun submitStudentRating(rating: Int): Completable

    fun submitStudentRatingFeedback(feedback: JsonObject): Completable

    fun studentRatingCross(): Completable

    fun getClassesList(): Single<ClassListEntity>

    fun getIncompleteChapterList(): Single<IncompleteChapterWidgetData>
}

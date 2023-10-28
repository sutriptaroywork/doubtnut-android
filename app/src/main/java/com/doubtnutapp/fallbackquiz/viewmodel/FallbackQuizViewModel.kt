package com.doubtnutapp.fallbackquiz.viewmodel

import androidx.core.content.edit
import com.doubtnutapp.base.BaseViewModel
import com.doubtnutapp.defaultPrefs
import com.doubtnutapp.fallbackquiz.db.FallbackQuizModel
import com.doubtnutapp.fallbackquiz.db.FallbackQuizRepository
import io.reactivex.disposables.CompositeDisposable
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

class FallbackQuizViewModel @Inject constructor(
    compositeDisposable: CompositeDisposable,
    private val fallbackQuizRepository: FallbackQuizRepository
) :
    BaseViewModel(compositeDisposable) {

    var imageQuestionMap = hashMapOf<String, String>()
    var eventDayMap = hashMapOf<String, String>()

    init {
        imageQuestionMap["104097692"] =
            "https://doubtnut-static.s3.ap-south-1.amazonaws.com/images/2022/03/23/12-37-32-938-PM_fallback_one.webp"
        imageQuestionMap["66073146"] =
            "https://doubtnut-static.s3.ap-south-1.amazonaws.com/images/2022/03/23/12-41-27-678-PM_fallback_two.webp"
        imageQuestionMap["104097708"] =
            "https://doubtnut-static.s3.ap-south-1.amazonaws.com/images/2022/03/23/12-40-50-146-PM_fallback_three.webp"
        imageQuestionMap["104097707"] =
            "https://doubtnut-static.s3.ap-south-1.amazonaws.com/images/2022/03/23/12-42-17-780-PM_fallback_four.webp"
        imageQuestionMap["104097706"] =
            "https://doubtnut-static.s3.ap-south-1.amazonaws.com/images/2022/03/23/12-42-48-396-PM_fallback_five.webp"
        imageQuestionMap["104097705"] =
            "https://doubtnut-static.s3.ap-south-1.amazonaws.com/images/2022/03/23/12-44-16-957-PM_fallback_six.webp"
        imageQuestionMap["104097704"] =
            "https://doubtnut-static.s3.ap-south-1.amazonaws.com/images/2022/03/23/12-44-42-182-PM_fallback_seven.webp"
        imageQuestionMap["104097691"] =
            "https://doubtnut-static.s3.ap-south-1.amazonaws.com/images/2022/03/23/12-45-12-080-PM_fallback_eight.webp"
        imageQuestionMap["104097702"] =
            "https://doubtnut-static.s3.ap-south-1.amazonaws.com/images/2022/03/23/12-45-32-950-PM_fallback_nine.webp"
        imageQuestionMap["104097701"] =
            "https://doubtnut-static.s3.ap-south-1.amazonaws.com/images/2022/03/23/12-45-57-249-PM_fallback_ten.webp"

        eventDayMap["104097692"] = "fallback_day1"
        eventDayMap["66073146"] = "fallback_day2"
        eventDayMap["104097708"] = "fallback_day3"
        eventDayMap["104097707"] = "fallback_day4"
        eventDayMap["104097706"] = "fallback_day5"
        eventDayMap["104097705"] = "fallback_day6"
        eventDayMap["104097704"] = "fallback_day7"
        eventDayMap["104097691"] = "fallback_day8"
        eventDayMap["104097702"] = "fallback_day9"
        eventDayMap["104097701"] = "fallback_day10"
    }

    fun getCurrentFallbackQuiz(): FallbackQuizModel {
        var currentModel: FallbackQuizModel
        val currentDay = defaultPrefs().getInt("fallback_day", 0)
        runBlocking {
            currentModel = fallbackQuizRepository.getCurrentFallbackQuiz()
        }
        defaultPrefs().edit {
            putInt("fallback_day", (currentDay + 1) % 10)
        }
        return currentModel
    }

}
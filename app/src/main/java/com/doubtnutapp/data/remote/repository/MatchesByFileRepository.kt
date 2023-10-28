package com.doubtnutapp.data.remote.repository

import androidx.core.content.edit
import com.doubtnut.core.data.DefaultDataStore
import com.doubtnutapp.Constants
import com.doubtnutapp.DoubtnutApp
import com.doubtnutapp.data.common.UserPreferenceImpl
import com.doubtnutapp.db.DoubtnutDatabase
import com.doubtnutapp.defaultPrefs
import com.doubtnutapp.matchquestion.service.MatchQuestionService
import com.doubtnutapp.toRequestBody
import com.doubtnutapp.workmanager.MatchQuesNotificationManager
import io.reactivex.schedulers.Schedulers

class MatchesByFileRepository(
    private val matchQuestionService: MatchQuestionService,
    private val database: DoubtnutDatabase,
    private val defaultDataStore: DefaultDataStore
) {

    fun getMatchesByFileName(fileName: String, askedQuestionImageUri: String, source: String, imageString: String) {

        val userPreference = UserPreferenceImpl(defaultPrefs(), defaultDataStore)

        val params: HashMap<String, Any> = HashMap()
        params["question_image"] = imageString
        params["student_id"] = userPreference.getUserStudentId()
        params["topic"] = "test"
        params["question"] = "about to only mathematics"
        params["subject"] = "MATHS"
        params["chapter"] = "DEFAULT"
        params["class"] = userPreference.getUserClass()
        params["locale"] = userPreference.getSelectedLanguage()
        params["source"] = source
        params["file_name"] = fileName
        params["supported_media_type"] = listOf("DASH", "HLS", "RTMP", "BLOB")

        matchQuestionService.getMatchesByFileName(params.toRequestBody()).map { response ->
            response.data
        }.also { apiMatchQuestion ->
            apiMatchQuestion.subscribeOn(Schedulers.io())
                .subscribe(
                    { matchQuestion ->

                        matchQuestion.delayNotification?.let { delayNotification ->

                            if (matchQuestion.matchedQuestions.isNotEmpty()) {

                                val currentTimeStamp = System.currentTimeMillis()

                                database.matchQuestionDao().insertMatchesByFileName(
                                    matchQuestion,
                                    askedQuestionImageUri,
                                    currentTimeStamp
                                )

                                // Reset count for in app match result dialog for this question
                                defaultPrefs().edit {
                                    putInt(Constants.IN_APP_MATCH_DIALOG_COUNT, 0)
                                }

                                MatchQuesNotificationManager.handleStickyNotification(
                                    DoubtnutApp.INSTANCE,
                                    matchQuestion.questionId,
                                    delayNotification.title,
                                    delayNotification.message,
                                    askedQuestionImageUri,
                                    notificationId = (currentTimeStamp / (100 * 60)).toInt()
                                )
                            }
                        }
                    },
                    { throwable ->
                        throwable?.printStackTrace()
                    }
                )
        }
    }

    fun deleteMatches(questionId: String) =
        database.matchQuestionDao().deleteMatchQuestion(questionId)
}

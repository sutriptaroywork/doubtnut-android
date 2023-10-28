package com.doubtnutapp.fallbackquiz.db

import com.doubtnutapp.defaultPrefs
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

class FallbackQuizRepository @Inject constructor(private val fallbackQuizDao: FallbackQuizDao) {

    companion object {
        private const val TAG = "FallbackQuizRepository"
    }

    private suspend fun saveFallbackListToLocal(fallbackQuizModels: List<FallbackQuizModel>) {
        fallbackQuizDao.saveFallbackQuiz(fallbackQuizModels)
    }

    suspend fun getCurrentFallbackQuiz(): FallbackQuizModel {
        val currentDay = defaultPrefs().getInt("fallback_day", 0)
        return fallbackQuizDao.getNthFallbackQuiz(currentDay)
    }

//    suspend fun clearAllFallbackQuiz() {
//        return fallbackQuizDao.deleteAllFallbackQuiz()
//    }

    private suspend fun getQuizSize(): List<FallbackQuizModel> {
        return fallbackQuizDao.getAllFallbackQuizes()
    }

    fun setAllFallbackQuizData() {
        var localQuizList: List<FallbackQuizModel>
        val fallbackQuizModels = ArrayList<FallbackQuizModel>()
        val responseList = listOf(
            """{
                    "type": "quiz_motivation",
                    "heading": "Motivation for the Day!",
                    "heading_icon": "https://d10lpgp6xz60nq.cloudfront.net/engagement_framework/BAB19F80-76DF-9E93-E436-E45B58A67096.webp",
                    "thumbnail_link": "https://d10lpgp6xz60nq.cloudfront.net/q-thumbnail/104097692.png",
                    "button_text": "WATCH VIDEO",
                    "is_skipable": true,
                    "deeplink": "doubtnutapp://video?qid=104097692&page=QUIZ_NOTIFICATION",
                    "overlay_image": "https://d10lpgp6xz60nq.cloudfront.net/engagement_framework/1BBA82AE-C576-11B7-A9D8-B64BB7EC6E98.webp",
                    "fallback": 1,
                    "question_id": 104097692
                }""",
            """{
                    "type": "quiz_motivation",
                    "heading": "Motivation for the Day!",
                    "heading_icon": "https://d10lpgp6xz60nq.cloudfront.net/engagement_framework/BAB19F80-76DF-9E93-E436-E45B58A67096.webp",
                    "thumbnail_link": "https://d10lpgp6xz60nq.cloudfront.net/q-thumbnail/66073146.png",
                    "button_text": "WATCH VIDEO",
                    "is_skipable": true,
                    "deeplink": "doubtnutapp://video?qid=66073146&page=QUIZ_NOTIFICATION",
                    "overlay_image": "https://d10lpgp6xz60nq.cloudfront.net/engagement_framework/1BBA82AE-C576-11B7-A9D8-B64BB7EC6E98.webp",
                    "fallback": 2,
                    "question_id": 66073146
                }""",
            """{
                    "type": "quiz_motivation",
                    "heading": "Motivation for the Day!",
                    "heading_icon": "https://d10lpgp6xz60nq.cloudfront.net/engagement_framework/BAB19F80-76DF-9E93-E436-E45B58A67096.webp",
                    "thumbnail_link": "https://d10lpgp6xz60nq.cloudfront.net/q-thumbnail/104097708.png",
                    "button_text": "WATCH VIDEO",
                    "is_skipable": true,
                    "deeplink": "doubtnutapp://video?qid=104097708&page=QUIZ_NOTIFICATION",
                    "overlay_image": "https://d10lpgp6xz60nq.cloudfront.net/engagement_framework/1BBA82AE-C576-11B7-A9D8-B64BB7EC6E98.webp",
                    "fallback": 3,
                    "question_id": 104097708
                }""",
            """{
                    "type": "quiz_motivation",
                    "heading": "Motivation for the Day!",
                    "heading_icon": "https://d10lpgp6xz60nq.cloudfront.net/engagement_framework/BAB19F80-76DF-9E93-E436-E45B58A67096.webp",
                    "thumbnail_link": "https://d10lpgp6xz60nq.cloudfront.net/q-thumbnail/104097707.png",
                    "button_text": "WATCH VIDEO",
                    "is_skipable": true,
                    "deeplink": "doubtnutapp://video?qid=104097707&page=QUIZ_NOTIFICATION",
                    "overlay_image": "https://d10lpgp6xz60nq.cloudfront.net/engagement_framework/1BBA82AE-C576-11B7-A9D8-B64BB7EC6E98.webp",
                    "fallback": 4,
                    "question_id": 104097707
                }""",
            """{
                    "type": "quiz_motivation",
                    "heading": "Motivation for the Day!",
                    "heading_icon": "https://d10lpgp6xz60nq.cloudfront.net/engagement_framework/BAB19F80-76DF-9E93-E436-E45B58A67096.webp",
                    "thumbnail_link": "https://d10lpgp6xz60nq.cloudfront.net/q-thumbnail/104097706.png",
                    "button_text": "WATCH VIDEO",
                    "is_skipable": true,
                    "deeplink": "doubtnutapp://video?qid=104097706&page=QUIZ_NOTIFICATION",
                    "overlay_image": "https://d10lpgp6xz60nq.cloudfront.net/engagement_framework/1BBA82AE-C576-11B7-A9D8-B64BB7EC6E98.webp",
                    "fallback": 5,
                    "question_id": 104097706
                }""",
            """{
                    "type": "quiz_motivation",
                    "heading": "Motivation for the Day!",
                    "heading_icon": "https://d10lpgp6xz60nq.cloudfront.net/engagement_framework/BAB19F80-76DF-9E93-E436-E45B58A67096.webp",
                    "thumbnail_link": "https://d10lpgp6xz60nq.cloudfront.net/q-thumbnail/104097705.png",
                    "button_text": "WATCH VIDEO",
                    "is_skipable": true,
                    "deeplink": "doubtnutapp://video?qid=104097705&page=QUIZ_NOTIFICATION",
                    "overlay_image": "https://d10lpgp6xz60nq.cloudfront.net/engagement_framework/1BBA82AE-C576-11B7-A9D8-B64BB7EC6E98.webp",
                    "fallback": 6,
                    "question_id": 104097705
                }""",
            """{
                    "type": "quiz_motivation",
                    "heading": "Motivation for the Day!",
                    "heading_icon": "https://d10lpgp6xz60nq.cloudfront.net/engagement_framework/BAB19F80-76DF-9E93-E436-E45B58A67096.webp",
                    "thumbnail_link": "https://d10lpgp6xz60nq.cloudfront.net/q-thumbnail/104097704.png",
                    "button_text": "WATCH VIDEO",
                    "is_skipable": true,
                    "deeplink": "doubtnutapp://video?qid=104097704&page=QUIZ_NOTIFICATION",
                    "overlay_image": "https://d10lpgp6xz60nq.cloudfront.net/engagement_framework/1BBA82AE-C576-11B7-A9D8-B64BB7EC6E98.webp",
                    "fallback": 7,
                    "question_id": 104097704
                }""",
            """{
                    "type": "quiz_motivation",
                    "heading": "Motivation for the Day!",
                    "heading_icon": "https://d10lpgp6xz60nq.cloudfront.net/engagement_framework/BAB19F80-76DF-9E93-E436-E45B58A67096.webp",
                    "thumbnail_link": "https://d10lpgp6xz60nq.cloudfront.net/q-thumbnail/104097691.png",
                    "button_text": "WATCH VIDEO",
                    "is_skipable": true,
                    "deeplink": "doubtnutapp://video?qid=104097691&page=QUIZ_NOTIFICATION",
                    "overlay_image": "https://d10lpgp6xz60nq.cloudfront.net/engagement_framework/1BBA82AE-C576-11B7-A9D8-B64BB7EC6E98.webp",
                    "fallback": 8,
                    "question_id": 104097691
                }""",
            """{
                    "type": "quiz_motivation",
                    "heading": "Motivation for the Day!",
                    "heading_icon": "https://d10lpgp6xz60nq.cloudfront.net/engagement_framework/BAB19F80-76DF-9E93-E436-E45B58A67096.webp",
                    "thumbnail_link": "https://d10lpgp6xz60nq.cloudfront.net/q-thumbnail/104097702.png",
                    "button_text": "WATCH VIDEO",
                    "is_skipable": true,
                    "deeplink": "doubtnutapp://video?qid=104097702&page=QUIZ_NOTIFICATION",
                    "overlay_image": "https://d10lpgp6xz60nq.cloudfront.net/engagement_framework/1BBA82AE-C576-11B7-A9D8-B64BB7EC6E98.webp",
                    "fallback": 9,
                    "question_id": 104097702
                }""",
            """{
                    "type": "quiz_motivation",
                    "heading": "Motivation for the Day!",
                    "heading_icon": "https://d10lpgp6xz60nq.cloudfront.net/engagement_framework/BAB19F80-76DF-9E93-E436-E45B58A67096.webp",
                    "thumbnail_link": "https://d10lpgp6xz60nq.cloudfront.net/q-thumbnail/104097701.png",
                    "button_text": "WATCH VIDEO",
                    "is_skipable": true,
                    "deeplink": "doubtnutapp://video?qid=104097701&page=QUIZ_NOTIFICATION",
                    "overlay_image": "https://d10lpgp6xz60nq.cloudfront.net/engagement_framework/1BBA82AE-C576-11B7-A9D8-B64BB7EC6E98.webp",
                    "fallback": 10,
                    "question_id": 104097701
                }"""
        )
        runBlocking {
            localQuizList = getQuizSize()
        }

        if (localQuizList.isEmpty()) {
            for (i in responseList.indices) {
                val objectJSON = responseList[i]
                fallbackQuizModels.add(Gson().fromJson(objectJSON, FallbackQuizModel::class.java))
            }
            CoroutineScope(Dispatchers.IO).launch {
                saveFallbackListToLocal(fallbackQuizModels)
            }
        }
    }

}
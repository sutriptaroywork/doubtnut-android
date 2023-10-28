package com.doubtnutapp.data.remote.repository

import com.doubtnutapp.data.remote.api.services.NetworkService
import com.doubtnutapp.data.remote.models.ApiResponse
import com.doubtnutapp.data.remote.models.topicboostergame2.*
import com.doubtnutapp.data.toRequestBody
import com.doubtnutapp.studygroup.service.StudyGroupMicroService
import com.doubtnutapp.studygroup.ui.fragment.SgSelectFriendFragment
import com.doubtnutapp.toInt
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import okhttp3.RequestBody
import javax.inject.Inject

/**
 * Created by devansh on 23/2/21.
 */

class TopicBoosterGameRepository2 @Inject constructor(
    private val networkService: NetworkService,
    private val studyGroupMicroService: StudyGroupMicroService
) {

    fun getTbgHomeData(): Flow<ApiResponse<TbgHomeData>> {
        return flow { emit(networkService.getTbgHomeData()) }
    }

    fun getLevels(): Flow<ApiResponse<LevelData>> {
        return flow { emit(networkService.getLevels()) }
    }

    fun getChapters(subject: String): Flow<ApiResponse<ChapterSelectionData>> {
        return flow {
            val requestBody = hashMapOf("subject" to subject).toRequestBody()
            emit(networkService.getChapters(requestBody))
        }
    }

    fun getTbgInviteData(topic: String, source: String?): Flow<ApiResponse<TbgInviteData>> {
        return flow {
            val requestBody = hashMapOf("topic" to topic).toRequestBody()
            when (source) {
                SgSelectFriendFragment.TAG -> {
                    emit(studyGroupMicroService.getTbgInviteData())
                }
                else -> {
                    emit(networkService.getTbgInviteData(requestBody))
                }
            }
        }
    }

    fun getFriendsList(tabId: Int, source: String?): Flow<ApiResponse<FriendsList>> {
        return flow {
            val requestBody = hashMapOf("id" to tabId).toRequestBody()
            when (source) {
                SgSelectFriendFragment.TAG -> {
                    emit(studyGroupMicroService.getFriendsList(requestBody))
                }
                else -> {
                    emit(networkService.getFriendsList(requestBody))
                }
            }
        }
    }

    fun getGameData(
        chapterAlias: String,
        gameId: String,
        inviteeIds: Array<String>?,
        isWhatsApp: Boolean,
    ): Flow<ApiResponse<TbgGameData>> {
        val requestBody = hashMapOf(
            "chapter_alias" to chapterAlias,
            "game_id" to gameId,
            "invitee_ids" to inviteeIds.orEmpty(),
            "is_whatsapp" to isWhatsApp.toInt(),
        ).toRequestBody()
        return flow { emit(networkService.getGameData(requestBody)) }
    }

    fun acceptInvite(
        gameId: String,
        inviterId: String,
        isInviterOnline: Boolean,
        chapterAlias: String
    ): Flow<ApiResponse<TbgGameData>> {
        return flow {
            emit(networkService.acceptInvite(gameId, inviterId, isInviterOnline.toInt(), chapterAlias))
        }
    }

    fun sendTbgInvitation(
        studentIds: List<Long>,
        gameId: String,
        topic: String
    ): Flow<ApiResponse<Unit>> {
        return flow {
            val requestBody = hashMapOf(
                "student_ids" to studentIds,
                "game_id" to gameId,
                "topic" to topic
            ).toRequestBody()
            emit(networkService.sendTbgInvitation(requestBody))
        }
    }

    fun getLeaderboard(): Flow<ApiResponse<Leaderboard>> {
        return flow { emit(networkService.getLeaderboard()) }
    }

    fun getLeaderboardList(id: Int, page: Int): Flow<ApiResponse<Leaderboard>> {
        val requestBody = mapOf(
            "id" to id,
            "page" to page
        ).toRequestBody()
        return flow { emit(networkService.getLeaderboardList(requestBody)) }
    }

    fun sendNumberInvitation(
        gameId: String,
        mobileNo: String,
        chapter: String,
        source: String?
    ): Flow<ApiResponse<NumberInvite>> {
        val requestBody = mapOf(
            "game_id" to gameId,
            "mobile" to mobileNo,
            "topic" to chapter
        ).toRequestBody()

        return flow {
            when (source) {
                SgSelectFriendFragment.TAG -> {
                    emit(studyGroupMicroService.sendNumberInvitation(requestBody))
                }
                else -> {
                    emit(networkService.sendNumberInvitation(requestBody))
                }
            }
        }
    }

    fun submitResult(requestBody: RequestBody): Flow<ApiResponse<TbgResult>> {
        return flow { emit(networkService.submitTbgResult(requestBody)) }
    }

    fun getPreviousResult(gameId: String): Flow<ApiResponse<TbgResult>> {
        return flow { emit(networkService.getTbgPreviousResult(gameId)) }
    }

    fun getQuizHistory(page: Int): Flow<ApiResponse<QuizHistoryViewMore>> {
        val requestBody = mapOf(
            "page" to page,
        ).toRequestBody()
        return flow { emit(networkService.getQuizHistory(requestBody)) }
    }
}

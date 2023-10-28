package com.doubtnutapp.socket

import com.doubtnutapp.Log
import com.doubtnutapp.data.fromJson
import com.doubtnutapp.data.remote.models.*
import com.doubtnutapp.data.remote.models.topicboostergame2.*
import com.doubtnutapp.dnr.model.DnrRewardSgMessage
import com.doubtnutapp.dnr.model.DnrSgMessageReward
import com.doubtnutapp.liveclass.adapter.*
import com.doubtnutapp.socket.entity.LiveScoreData
import com.doubtnutapp.studygroup.model.*
import com.google.gson.Gson
import org.json.JSONObject

/**
 * Created by Anand Gaurav on 18/09/20.
 */
object SocketUtil {

    fun getMappedResponse(arg: Array<Any>?, gson: Gson, url: String): Any? {
        var responseData: Any? = null
        try {
            val completeResponse = arg?.getOrNull(0) ?: return null
            val response = JSONObject(completeResponse as String)
            val type: String = (response).get("type") as String
            val responseJson = response.get("response") as JSONObject
            when (type) {
                "live_class_quiz" -> {
                    val quizResponse: LiveClassQuestionResponse = gson.fromJson(responseJson.toString())
                    val dataList: MutableList<LiveClassQuizQuestionData?> = mutableListOf()
                    dataList.addAll(quizResponse.widgets.map { it.data })
                    responseData = LiveClassQuestionDataList(dataList,
                            quizResponse.quizResourceId,
                            quizResponse.liveClassResourceId,
                            quizResponse.liveAt ?: 0)
                }
                "live_class_polls" -> {
                    val widgets = responseJson.get("widgets").toString()
                    val detailId = responseJson.get("detail_id").toString().toLongOrNull() ?: 0
                    val list =
                            gson.fromJson<List<PollWidgets>>(widgets)
                    val dataList = mutableListOf<LiveClassPollData?>()
                    list.forEach {
                        it.data?.publishId = it.publishId ?: 0
                        dataList.add(it.data)
                    }
                    responseData = LiveClassPollsList(dataList, detailId)
                }
                "live_class_communication" -> {
                    responseData = gson.fromJson<LiveClassAnnouncementData>(responseJson.toString())
                }
                "viewers" -> {
                    responseData = gson.fromJson<LiveClassStats>(responseJson.toString())
                }
                "chat_message" -> {
                    responseData = gson.fromJson<LiveClassChatData>(responseJson.toString())
                }
                "banned" -> {
                    responseData = gson.fromJson<BanUserData>(responseJson.toString())
                }
                "report" -> {
                    responseData = gson.fromJson<ReportUserData>(responseJson.toString())
                }
                "chat_message_study_group" -> {
                    responseData = gson.fromJson<StudyGroupChatWrapper>(responseJson.toString())
                }
                "study_chat_message" -> {
                    responseData = gson.fromJson<StudyGroupChatWrapper>(responseJson.toString())
                }
                "block" -> {
                    responseData = gson.fromJson<BlockStudyGroupMember>(responseJson.toString())
                }
                "chat_online" -> {
                    responseData = gson.fromJson<MemberToJoinData>(responseJson.toString())
                }
                "inviter_online" -> {
                    responseData = gson.fromJson<InviterOnline>(responseJson.toString())
                }
                "game_begin" -> {
                    responseData = gson.fromJson<GameBegin>(responseJson.toString())
                }
                "game_message" -> {
                    responseData = gson.fromJson<GameMessage>(responseJson.toString())
                }
                "game_emoji" -> {
                    responseData = gson.fromJson<GameEmoji>(responseJson.toString())
                }
                "question_submit" -> {
                    responseData = gson.fromJson<QuestionSubmit>(responseJson.toString())
                }
                "report-group", "report-message", "report-member" -> {
                    responseData = gson.fromJson<SgReport>(responseJson.toString())
                }
                "delete-message", "delete-reported-messages" -> {
                    responseData = gson.fromJson<SgDelete>(responseJson.toString())
                }
                "update_message_restriction" -> {
                    responseData = gson.fromJson<SgUpdateMessageRestriction>(responseJson.toString())
                }
                "chats", "groups" -> {
                    responseData = gson.fromJson<SgListUpdate>(responseJson.toString())
                }
                "block_chat" -> {
                    responseData = gson.fromJson<SgBlockChat>(responseJson.toString())
                }
                "reward_message_dnr" -> {
                    responseData = DnrRewardSgMessage(showRewardPopup = true)
                }
                "score" -> {
                    responseData = gson.fromJson<LiveScoreData>(responseJson.toString())
                }
            }
        } catch (e: Exception) {
            Log.e(Throwable("Exception in socket-io $url", e), "JsonParseException")
            responseData = null
        }
        return responseData
    }

}

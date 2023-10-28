package com.doubtnut.noticeboard.data.remote

import com.doubtnut.core.common.data.entity.SgWidgetListData
import com.doubtnut.noticeboard.data.entity.NoticeBoardData
import io.reactivex.Single
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class NoticeBoardRepository @Inject constructor(
    private val noticeBoardService: NoticeBoardService,
    private val noticeBoardMicroService: NoticeBoardMicroService
) {

    fun getNotices(type: String)
            : Flow<NoticeBoardData> =
        flow {
            if (data == null) {
                noticeBoardService.getNotices(TYPE_TODAY_ALL).let {
                    data = it.data
                }
            }
            if (type == TYPE_TODAY_ALL) {
                emit(
                    data!!
                )
            } else {
                emit(
                    NoticeBoardData(
                        items = data?.items?.filter { it.isToday == true },
                        emptyMessage = data?.emptyMessage
                    )
                )
            }
        }

    fun getTodaySpecialGroups(): Single<SgWidgetListData> =
        noticeBoardMicroService.getTodaySpecialGroups().map { it.data }

    companion object {
        fun clear() {
            data = null
        }

        @Suppress("SpellCheckingInspection")
        const val TYPE_TODAY_SPECIAL = "todays_special"
        const val TYPE_TODAY_ALL = ""

        var data: NoticeBoardData? = null
        var unreadNoticeIds = HashSet<String>()
    }
}


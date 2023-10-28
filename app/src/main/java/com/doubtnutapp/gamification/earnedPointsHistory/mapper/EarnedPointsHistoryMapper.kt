package com.doubtnutapp.gamification.earnedPointsHistory.mapper


import com.doubtnutapp.R
import com.doubtnutapp.data.base.Mapper
import com.doubtnutapp.domain.gamification.earnedPointsHistory.entity.EarnedPointsHistoryListEntity
import com.doubtnutapp.gamification.earnedPointsHistory.model.EarnedPointsBaseFeedViewItem
import com.doubtnutapp.gamification.earnedPointsHistory.model.EarnedPointsHistoryHeaderDataModel
import com.doubtnutapp.gamification.earnedPointsHistory.model.EarnedPointsHistoryListDataModel
import com.doubtnutapp.utils.DateUtils
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EarnedPointsHistoryMapper @Inject constructor() : Mapper<List<EarnedPointsHistoryListEntity>, List<EarnedPointsBaseFeedViewItem>> {

    override fun map(srcObject: List<EarnedPointsHistoryListEntity>) : List<EarnedPointsBaseFeedViewItem> {

        val listOfEntryPoints = mutableListOf<EarnedPointsBaseFeedViewItem>()
        val map = srcObject.sortedByDescending {
            DateUtils.stringToDate(it.createdAt)
        }.groupBy {
            DateUtils.formatStringDate(it.createdAt)
        }

        val keys = map.keys
        for (key in keys) {
            map[key]?.apply {
                listOfEntryPoints.add(
                        EarnedPointsHistoryHeaderDataModel(
                                key,
                                R.layout.item_earned_point_history_header
                        )
                )
                listOfEntryPoints.addAll(
                        map {
                            EarnedPointsHistoryListDataModel(
                                    it.isActive,
                                    it.activity,
                                    it.referId,
                                    it.userId,
                                    it.xp,
                                    it.action,
                                    DateUtils.formatStringDate(it.createdAt),
                                    it.id,
                                    it.actionDisplay,
                                    R.layout.item_earned_point_history
                            )
                        }
                )
            }
        }

        return listOfEntryPoints
    }
}
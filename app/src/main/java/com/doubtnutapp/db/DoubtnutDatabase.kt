package com.doubtnutapp.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.doubtnut.database.BaseDatabase
import com.doubtnut.database.dao.NoticeBoardDao
import com.doubtnut.database.entity.NoticeBoard
import com.doubtnutapp.data.remote.models.feed.TopOptionWidgetItem
import com.doubtnutapp.db.dao.*
import com.doubtnutapp.db.entity.*
import com.doubtnutapp.downloadedVideos.OfflineMediaDao
import com.doubtnutapp.downloadedVideos.OfflineMediaItem
import com.doubtnutapp.fallbackquiz.db.FallbackQuizDao
import com.doubtnutapp.fallbackquiz.db.FallbackQuizModel
import com.doubtnutapp.model.AppActiveFeedback
import com.doubtnutapp.model.AppEvent
import com.doubtnutapp.networkstats.db.VideoNetworkDao
import com.doubtnutapp.networkstats.models.VideoStatsData
import com.doubtnutapp.scheduledquiz.db.ScheduledNotificationDao
import com.doubtnutapp.scheduledquiz.di.model.ScheduledQuizNotificationModel
import com.doubtnutapp.video.VideoStatusTrack
import com.doubtnutapp.video.VideoStatusTrackDao

@Database(
    entities = [
        AppEvent::class,
        AppActiveFeedback::class,
        LocalMatchQuestion::class,
        LocalMatchQuestionList::class,
        LocalMatchFacet::class,
        OfflineMediaItem::class,
        VideoStatusTrack::class,
        StatusMeta::class,
        TopOptionWidgetItem::class,
        ScheduledQuizNotificationModel::class,
        FallbackQuizModel::class,
        LocalOfflineOcr::class,
        NoticeBoard::class,
        VideoStatsData::class,
        VideoViewStats::class
    ],
    version = 40
)

@TypeConverters(TypeConverter::class)
abstract class DoubtnutDatabase : RoomDatabase(), BaseDatabase {
    abstract fun eventsDao(): EventsDao
    abstract fun feedbackDao(): FeedbackDao
    abstract fun matchQuestionDao(): MatchQuestionDao
    abstract fun offlineMediaDao(): OfflineMediaDao
    abstract fun videoStatusTrackDao(): VideoStatusTrackDao
    abstract fun statusMetaDao(): StatusMetaDao
    abstract fun topOptionWidgetItemDao(): TopOptionWidgetItemDao
    abstract fun scheduledNotifDao(): ScheduledNotificationDao
    abstract fun fallbackQuizDao(): FallbackQuizDao
    abstract fun offlineOcrDao(): OfflineOcrDao
    abstract fun videoNetworkDao(): VideoNetworkDao
    abstract fun videoViewStatsDao(): VideoViewStatsDao

    abstract override fun noticeBoardDao(): NoticeBoardDao
}

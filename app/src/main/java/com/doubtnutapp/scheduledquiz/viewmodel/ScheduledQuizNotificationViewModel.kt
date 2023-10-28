package com.doubtnutapp.scheduledquiz.viewmodel

import com.doubtnutapp.DoubtnutApp
import com.doubtnutapp.base.BaseViewModel
import com.doubtnutapp.scheduledquiz.db.ScheduledNotificationDao
import com.doubtnutapp.scheduledquiz.db.repository.ScheduledQuizNotificationRepository
import com.doubtnutapp.scheduledquiz.di.model.ScheduledQuizNotificationModel
import io.reactivex.disposables.CompositeDisposable
import javax.inject.Inject

class ScheduledQuizNotificationViewModel
@Inject constructor(
    compositeDisposable: CompositeDisposable,
    private val scheduledQuizNotificationRepository: ScheduledQuizNotificationRepository
) :
    BaseViewModel(compositeDisposable) {

    var scheduledNotificationDao: ScheduledNotificationDao? =
        DoubtnutApp.INSTANCE.getDatabase()?.scheduledNotifDao()

    fun getCurrentNotif(): ScheduledQuizNotificationModel? {
        return scheduledQuizNotificationRepository.getTopScheduledQuizNotification();
    }

}
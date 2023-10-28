package com.doubtnutapp.ui.quiz

import android.content.Context
import com.evernote.android.job.JobManager

object EvernoteUtils {

    fun init(context: Context) {
        try {
            JobManager.instance()
        } catch (e: Exception) {
            JobManager.create(context).addJobCreator(QuizJobCreator())
        }
    }

    fun cancelAll(context: Context) {
        try {
            JobManager.instance()
        } catch (e: Exception) {
            JobManager.create(context)
        }
        JobManager.instance().cancelAll()
    }

}
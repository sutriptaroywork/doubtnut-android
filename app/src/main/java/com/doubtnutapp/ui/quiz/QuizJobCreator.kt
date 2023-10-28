package com.doubtnutapp.ui.quiz

import com.evernote.android.job.Job
import com.evernote.android.job.JobCreator

/**
 * Created by akshaynandwana on
 * 28, November, 2018
 **/
class QuizJobCreator : JobCreator {

    override fun create(tag: String): Job? {
        return when (tag) {
            FetchQuizJob.JOB_TAG -> FetchQuizJob()
            else -> null
        }
    }
}

package com.doubtnutapp.workmanager.workers

import android.content.Context
import android.graphics.BitmapFactory
import androidx.annotation.Keep
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.doubtnutapp.Constants
import com.doubtnutapp.data.remote.DataHandler
import com.doubtnutapp.matchquestion.ui.activity.MatchQuestionActivity
import com.doubtnutapp.utils.BitmapUtils
import java.io.File
import javax.inject.Inject

@Keep
class MatchesByFileNameWorker @Inject constructor(
    private val context: Context,
    private val params: WorkerParameters
) : Worker(context, params) {

    override fun doWork(): Result {

        try {
            val imageFileName = inputData.getString(MatchQuestionActivity.IMAGE_FILE_NAME) ?: ""
            val askedQuestionImageUri =
                inputData.getString(MatchQuestionActivity.INTENT_EXTRA_ASKED_QUE_URI) ?: ""
            val source = inputData.getString(Constants.SOURCE) ?: ""

            val byteArray = getImageAsByteArray(askedQuestionImageUri) ?: return Result.failure()

            val quesBitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
                ?: return Result.failure()

            BitmapUtils.convertBitmapToString(quesBitmap) { imageString ->
                imageString?.let {
                    DataHandler.INSTANCE.matchesByFileRepository.getMatchesByFileName(
                        imageFileName,
                        askedQuestionImageUri,
                        source,
                        imageString
                    )
                }
            }

        } catch (throwable: Throwable) {
            throwable.printStackTrace()
        }

        return Result.success()
    }

    private fun getImageAsByteArray(askedQuestionImageUri: String): ByteArray? {
        val imageFile = File(askedQuestionImageUri)
        return if (imageFile.isFile) imageFile.readBytes() else null
    }
}
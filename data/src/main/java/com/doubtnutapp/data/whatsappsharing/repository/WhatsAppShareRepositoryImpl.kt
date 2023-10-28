package com.doubtnutapp.data.whatsappsharing.repository

import android.content.Context
import android.graphics.*
import androidx.core.content.FileProvider
import com.doubtnutapp.data.base.di.qualifier.ApplicationCachePath
import com.doubtnutapp.data.base.di.qualifier.ApplicationContext
import com.doubtnutapp.data.common.UserPreference
import com.doubtnutapp.data.toRequestBody
import com.doubtnutapp.data.whatsappsharing.service.WhatsAppSharingService
import com.doubtnutapp.domain.base.manager.FileManager
import com.doubtnutapp.domain.whatsappsharing.repository.WhatsAppShareRepository
import io.branch.indexing.BranchUniversalObject
import io.branch.referral.util.LinkProperties
import io.reactivex.Completable
import io.reactivex.Single
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.InputStream
import javax.inject.Inject

class WhatsAppShareRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val fileManager: FileManager,
    private val userPreference: UserPreference,
    @ApplicationCachePath private val appCachePath: String,
    private val whatsAppShareingService: WhatsAppSharingService

) : WhatsAppShareRepository {

    /**
     * This should be called on Analytics Thread.
     */
    override fun getDeepLink(
        channel: String,
        campaign: String,
        type: String?,
        controlParams: HashMap<String, String>?
    ): Single<String> {
        return Single.fromCallable {
            val linkProperties = LinkProperties().apply {
                this.channel = channel
                feature = type
                this.campaign = campaign
            }.also {
                controlParams?.let { params ->
                    params.forEach { (key, value) ->
                        it.addControlParameter(key, value)
                    }
                }

                it.addControlParameter(CONTROL_PARAM_SID, userPreference.getUserStudentId())
            }
            BranchUniversalObject().getShortUrl(context, linkProperties)
        }
    }

    override fun getShareableImageUrl(
        imageInputStream: InputStream,
        canvasBgColor: String,
        authority: String
    ): Single<String> {

        return Single.fromCallable {

            val imageAsBitmap = BitmapFactory.decodeStream(imageInputStream)

            val finalBitmap = Bitmap.createBitmap(
                imageAsBitmap.width,
                imageAsBitmap.height,
                Bitmap.Config.RGB_565
            )

            val canvas = Canvas()
            canvas.setBitmap(finalBitmap)
            canvas.drawColor(Color.parseColor(canvasBgColor))
            canvas.drawBitmap(imageAsBitmap, 0F, 0F, Paint())

            val stream = ByteArrayOutputStream()
            finalBitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
            val transformedInputStream = ByteArrayInputStream(stream.toByteArray())
            finalBitmap.recycle()

            val shareableImageFilePath = "$appCachePath${File.separator}ThumbnailTempImage.jpg"
            fileManager.saveFileToDirectory(transformedInputStream, shareableImageFilePath)
            FileProvider.getUriForFile(context, authority, File(shareableImageFilePath)).toString()
        }
    }

    override fun videoShared(questionId: String): Completable {

        val bodyParam = hashMapOf(
            "entity_type" to "video",
            "entity_id" to questionId
        ).toRequestBody()

        return whatsAppShareingService.videoShared(bodyParam)
    }

    companion object {
       private const val CONTROL_PARAM_SID = "sid"
    }
}

package com.doubtnutapp.ui.forum.comments

import android.graphics.Bitmap
import android.net.Uri
import android.provider.MediaStore
import android.util.Base64
import com.doubtnutapp.DoubtnutApp
import com.doubtnutapp.authToken
import com.doubtnutapp.base.BaseViewModel
import com.doubtnutapp.data.remote.DataHandler
import com.doubtnutapp.data.remote.RetrofitLiveData
import com.doubtnutapp.data.remote.models.ApiResponse
import com.doubtnutapp.data.remote.models.Comment
import com.doubtnutapp.doAsyncPost
import com.doubtnutapp.toRequestBody
import io.reactivex.disposables.CompositeDisposable
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.ByteArrayOutputStream
import java.io.File
import javax.inject.Inject

class CommentsViewModel @Inject constructor(
    compositeDisposable: CompositeDisposable
) : BaseViewModel(compositeDisposable) {

    private fun getApplication() = DoubtnutApp.INSTANCE

    fun getComments(
        entityType: String,
        entityId: String,
        page: String,
        batchId: String?
    ): RetrofitLiveData<ApiResponse<ArrayList<Comment>>> {
        return DataHandler.INSTANCE.commentRepository.getComments(
            authToken(getApplication()),
            entityType,
            entityId,
            page,
            null,
            batchId
        )
    }

    fun addComment(
        entityType: String, entityId: String, message: String,
        imageFile: File?, file: File?, batchId: String?,
    ): RetrofitLiveData<ApiResponse<Comment>> {
        return DataHandler.INSTANCE.commentRepository.addComment(
            authToken(getApplication()),
            entityType, entityId, "", message,
            getImageFileMultiPartRequestBody(imageFile),
            getAudioFileMultiPartRequestBody(file), null, null, batchId
        )
    }

    fun removeComment(commentId: String): RetrofitLiveData<ApiResponse<Any>> {
        return DataHandler.INSTANCE.commentRepository.removeComment(
            authToken(getApplication()),
            getRemoveRequestBody(commentId)
        )
    }

    fun reportComment(commentId: String): RetrofitLiveData<ApiResponse<Any>> {
        return DataHandler.INSTANCE.commentRepository.reportComment(
            authToken(getApplication()),
            getReportRequestBody(commentId)
        )
    }

    var imageString: String = ""
    fun getBitmapAsString(uri: Uri, callback: (imageAsString: String) -> Unit) {
        doAsyncPost(handler = {
            imageString = getEncodedImage(uri)
        }, postHandler = {
            callback(imageString)
        }).execute()
    }


    private fun getEncodedImage(imageUri: Uri): String {
        val bmp = MediaStore.Images.Media.getBitmap(
            getApplication().contentResolver,
            imageUri
        )
        val byteCount = bmp.byteCount.toFloat()
        val bitWidth = bmp.width.toFloat()
        val bitHeight = bmp.height.toFloat()
        val bitPer = byteCount * 8 / (bitWidth * bitHeight)

        val baos = ByteArrayOutputStream()
        if (bitPer >= 16.0) {
            bmp.compress(
                Bitmap.CompressFormat.JPEG,
                (100.0 * (16.0 / bitPer).toFloat()).toInt(),
                baos
            )
        } else {
            bmp.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        }

        val imageBytes = baos.toByteArray()
        return Base64.encodeToString(imageBytes, Base64.DEFAULT)
    }

    private fun getAudioFileMultiPartRequestBody(audioFile: File?): MultipartBody.Part? {
        val audioSend = audioFile?.asRequestBody("audio/*".toMediaTypeOrNull())
        return if (audioSend != null) MultipartBody.Part.createFormData(
            "audio",
            audioFile.name,
            audioSend
        ) else null
    }

    private fun getImageFileMultiPartRequestBody(imageFile: File?): MultipartBody.Part? {
        val audioSend = imageFile?.asRequestBody("image/*".toMediaTypeOrNull())
        return if (audioSend != null) MultipartBody.Part.createFormData(
            "image",
            imageFile.name,
            audioSend
        ) else null
    }


    private fun getRemoveRequestBody(commentId: String): RequestBody =
        getCommentIdRequestBody(commentId)

    private fun getReportRequestBody(commentId: String): RequestBody =
        getCommentIdRequestBody(commentId)

    private fun getCommentIdRequestBody(commentId: String): RequestBody = hashMapOf<String, Any>(
        "comment_id" to commentId
    ).toRequestBody()
}
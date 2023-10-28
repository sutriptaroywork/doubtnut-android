package com.doubtnutapp.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Looper
import android.util.Base64
import androidx.annotation.WorkerThread
import com.bumptech.glide.Glide
import com.bumptech.glide.load.Transformation
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.doubtnut.core.utils.ToastUtils
import com.doubtnutapp.Constants
import com.doubtnutapp.Log
import com.doubtnutapp.doAsyncPostWithResult
import com.doubtnutapp.toGrayscale
import java.io.ByteArrayOutputStream
import java.io.FileOutputStream
import java.io.OutputStream
import kotlin.math.sqrt

object BitmapUtils {

    fun convertBitmapToStringAfterScaling(
        context: Context?,
        uri: Uri?,
        performRecycle: Boolean = false,
        callback: (String?) -> Unit
    ) {
        if (context == null || uri == null) {
            callback(null)
            return
        }
        doAsyncPostWithResult(
            handler = {
                val bitmap = getBitmapFromUrl(context, uri.toString())
                if (bitmap == null) {
                    null
                } else {
                    scaleDownImage(
                        bmp = bitmap,
                        quality = Constants.SCALE_DOWN_IMAGE_QUALITY,
                        imageArea = Constants.SCALE_DOWN_IMAGE_AREA,
                        performRecycle = performRecycle
                    ).first
                }
            },
            postHandler = {
                callback(it)
            }
        ).execute()
    }

    fun convertBitmapToString(bitmap: Bitmap, callback: (String?) -> Unit) {
        doAsyncPostWithResult(handler = {
            getEncodedImage(bitmap)
        }, postHandler = {
            callback(it)
        }).execute()
    }

    /**
     * Perform recycle might cause a issue as Bitmap generated by Glide would be same for two URL
     * due to caching...
     */
    fun scaleDownBitmap(
        bitmap: Bitmap,
        quality: Int,
        imageArea: Double,
        performRecycle: Boolean = false,
        outputStream: OutputStream = ByteArrayOutputStream(),
        callback: (Pair<String, Bitmap>?) -> Unit
    ) {
        doAsyncPostWithResult(handler = {
            scaleDownImage(
                bmp = bitmap,
                quality = quality,
                imageArea = imageArea,
                performRecycle = performRecycle,
                outputStream = outputStream,
            )
        }, postHandler = {
            callback(it)
        }).execute()
    }

    fun greyScaleBitmap(bitmap: Bitmap, callback: (Bitmap?) -> Unit) {
        doAsyncPostWithResult(handler = {
            bitmap.toGrayscale()
        }, postHandler = {
            callback(it)
        }).execute()
    }

    fun getEncodedImage(bmp: Bitmap): String {
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

    @WorkerThread
    fun getBitmapFromUrl(context: Context, imageUrl: String?): Bitmap? {
        if (imageUrl.isNullOrEmpty()) {
            return null
        }

        if (Looper.myLooper() == Looper.getMainLooper()) {
            ToastUtils.makeTextInDev(context, "bhai kya kar raha hai tu")?.show()
            Log.e(IllegalStateException("getBitmapFromUrl() called on Main Thread."))
        }

        return try {
            Glide.with(context)
                .asBitmap()
                .load(imageUrl)
                .submit().get()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun convertBitmapToByteArray(bitmap: Bitmap, callback: (ByteArray?) -> Unit) {
        doAsyncPostWithResult(handler = {
            getImageByteArray(bitmap)
        }, postHandler = {
            callback(it)
        }).execute()
    }

    /**
     * Perform recycle might cause a issue as Bitmap generated by Glide would be same for two URL
     * due to caching...
     */
    private fun scaleDownImage(
        bmp: Bitmap,
        quality: Int,
        imageArea: Double,
        performRecycle: Boolean = false,
        outputStream: OutputStream = ByteArrayOutputStream()
    ): Pair<String, Bitmap> {

        val originalWidth = bmp.width.toDouble()
        val originalHeight = bmp.height.toDouble()

        val originalArea = (originalWidth * originalHeight)

        val finalBitmap = if (originalArea > imageArea) {
            val alpha = sqrt(imageArea / originalArea)
            val destWidth = (originalWidth * alpha).toInt()
            val destHeight = (originalHeight * alpha).toInt()
            val resizedImage = Bitmap.createScaledBitmap(bmp, destWidth, destHeight, true)
            resizedImage.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
            resizedImage
        } else {
            bmp.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
            bmp
        }
        if (outputStream is FileOutputStream) {
            outputStream.close()
        }

        val imageBytes = when (outputStream) {
            is ByteArrayOutputStream -> outputStream.toByteArray()
            else -> ByteArray(0)
        }
        return Pair(Base64.encodeToString(imageBytes, Base64.DEFAULT), finalBitmap)
    }

    fun getImageByteArray(bmp: Bitmap): ByteArray {
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
        return baos.toByteArray()
    }

    fun loadBitmap(
        context: Context,
        imageUrl: String,
        vararg transformations: Transformation<Bitmap>,
        onLoadSuccess: (result: Bitmap) -> Unit
    ) {
        Glide.with(context)
            .asBitmap()
            .load(imageUrl)
            .apply(RequestOptions().transform(*transformations))
            .into(object : CustomTarget<Bitmap>() {
                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                    onLoadSuccess(resource)
                }

                override fun onLoadCleared(placeholder: Drawable?) {
                    /**
                     * This is called when ImageView is cleared on lifecycle call or for
                     * some other reason.
                     * If you are referencing the bitmap somewhere else too other than this imageView
                     * clear it here as you can no longer have the bitmap
                     */
                }
            })
    }

    fun loadBitmap(
        context: Context,
        bitmap: Bitmap,
        vararg transformations: Transformation<Bitmap>,
        onLoadSuccess: (result: Bitmap) -> Unit
    ) {
        Glide.with(context)
            .asBitmap()
            .load(bitmap)
            .apply(RequestOptions().transform(*transformations))
            .into(object : CustomTarget<Bitmap>() {
                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                    onLoadSuccess(resource)
                }

                override fun onLoadCleared(placeholder: Drawable?) {
                    /**
                     * This is called when ImageView is cleared on lifecycle call or for
                     * some other reason.
                     * If you are referencing the bitmap somewhere else too other than this imageView
                     * clear it here as you can no longer have the bitmap
                     */
                }
            })
    }
}
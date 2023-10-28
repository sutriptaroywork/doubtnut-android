package com.doubtnut.referral.ui

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.text.Html
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import java.io.File
import java.io.IOException
import java.io.OutputStream
import androidx.core.content.FileProvider
import androidx.core.text.HtmlCompat
import java.io.FileOutputStream

object UiHelper {

    fun launchShareSheetWithImage(context: Context, messageStr: String, imageUrl: String?) {
        if (imageUrl != null) {
            Glide.with(context)
                .asBitmap()
                .load(imageUrl)
                .into(object : CustomTarget<Bitmap>() {
                    override fun onResourceReady(
                        bitmap: Bitmap,
                        transition: Transition<in Bitmap>?
                    ) {

                        val fileName ="DoubtnutAppReferralProgram.png"
                        val imageUri = getImageUri(context, bitmap,fileName)

                        val intent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_TEXT, messageStr)
                            type = if (imageUri != null) {
                                putExtra(Intent.EXTRA_STREAM, imageUri)
                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                                "image/*"
                            } else {
                                "text/plain"
                            }
                        }
                        val shareIntent = Intent.createChooser(intent, null)
                        context.startActivity(shareIntent)

                    }

                    override fun onLoadCleared(placeholder: Drawable?) {
                    }

                })
        } else {
            val intent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, messageStr)
                type = "text/plain"
            }
            val shareIntent = Intent.createChooser(intent, null)
            context.startActivity(shareIntent)

        }
    }

    fun launchShareSheetForText(context: Context, messageStr: String){
        val intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, HtmlCompat.fromHtml( messageStr,HtmlCompat.FROM_HTML_MODE_LEGACY).toString())
            type = "text/html"
        }
        val shareIntent = Intent.createChooser(intent, null)
        context.startActivity(shareIntent)

    }

     fun getImageUri(context: Context, bitmap: Bitmap,fileName:String?): Uri? {
        var fos: OutputStream? = null
        var imageUri: Uri? = null
        val folderName = "Doubtnut"

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val resolver = context.contentResolver
                val contentValues = ContentValues()
                contentValues.put(
                    MediaStore.MediaColumns.DISPLAY_NAME,
                    fileName + System.currentTimeMillis()
                )
                contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
                contentValues.put(
                    MediaStore.MediaColumns.RELATIVE_PATH,
                    Environment.DIRECTORY_PICTURES + File.separator + folderName
                )
                imageUri = resolver.insert(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    contentValues
                )
                if (imageUri == null) throw IndexOutOfBoundsException("Failed to create new MediaStore record.")
                fos = resolver.openOutputStream(imageUri)
                if (!bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos))
                    throw  IOException("Failed to save bitmap.");
                fos?.flush();
            } else {

                val file =
                    File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), fileName)
                val fileOutputStream = FileOutputStream(file)
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream)
                fileOutputStream.flush()
                imageUri = FileProvider.getUriForFile(
                    context, context.applicationContext
                        .packageName + ".provider", file
                );
            }
        } finally {
            fos?.close()
        }

        return imageUri
    }
}
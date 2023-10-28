package com.doubtnutapp.ui.browser

import android.Manifest
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.view.View
import android.webkit.*
import android.widget.ProgressBar
import androidx.core.app.ActivityCompat
import com.doubtnut.core.CoreApplication
import com.doubtnut.core.utils.PermissionUtils
import com.doubtnutapp.Log
import com.doubtnutapp.deeplink.DeeplinkAction
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

class ProgressWebViewClient(private val progressBar: ProgressBar) : WebViewClient() {

    init {
        progressBar.visibility = View.VISIBLE
    }

    override fun onPageCommitVisible(view: WebView?, url: String?) {
        super.onPageCommitVisible(view, url)
        progressBar.visibility = View.GONE
    }

    override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
        try {
            val url = request?.url.toString()
            if (url.startsWith("tg:") || url.startsWith("whatsapp:") || url.startsWith("doubtnutapp://")) {
                val intent = Intent(Intent.ACTION_VIEW)
                intent.data = Uri.parse(url)
                progressBar.context.startActivity(intent)
                return true
            }
        } catch (e: ActivityNotFoundException) {
            Log.e(e, TAG)
        }
        return super.shouldOverrideUrlLoading(view, request)
    }

    companion object {
        private const val TAG = "ProgressWebViewClient"
    }

}

class ProgressChromeClient : WebChromeClient() {

    @Inject
    lateinit var deeplinkAction: DeeplinkAction

    var permissionDeny: PermissionDeny? = null

    private var mFileChooserParams: FileChooserParams? = null
    var mFilePathCallback: ValueCallback<Array<Uri>>? = null
    var mCameraPhotoPath: String? = null

    init {
        CoreApplication.INSTANCE.androidInjector().inject(this)
    }

    override fun onPermissionRequest(request: PermissionRequest) {
        val perms = arrayOf(PermissionRequest.RESOURCE_PROTECTED_MEDIA_ID)
        request.grant(perms)
    }

    override fun onShowFileChooser(
        webView: WebView?,
        filePathCallback: ValueCallback<Array<Uri>>?,
        fileChooserParams: FileChooserParams?
    ): Boolean {
        val context = webView?.context ?: return false
        val activity = (context as? Activity) ?: return false
        mFilePathCallback?.onReceiveValue(null)

        mFilePathCallback = filePathCallback
        mFileChooserParams = fileChooserParams

        checkPermissionAndProceed(activity)
        return true
    }

    @Suppress("MemberVisibilityCanBePrivate")
    fun checkPermissionAndProceed(activity: Activity) {
        when {
            PermissionUtils.hasPermissions(
                activity,
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA)
            ) -> {
                pickFile(activity)
            }

            else -> {
                ActivityCompat.requestPermissions(
                    activity,
                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA),
                    REQUEST_CODE_PERM_INPUT_FILE
                )
            }
        }
    }

    @Suppress("MemberVisibilityCanBePrivate")
    fun showPermissionDenyAlert(activity: Activity) {
        permissionDeny?.let {
            val builder = androidx.appcompat.app.AlertDialog.Builder(activity)
                .setTitle(it.title)
                .setMessage(it.subtitle)
                .setCancelable(it.cancelable ?: false)
                .setPositiveButton(it.positiveAction) { dialog, _ ->
                    deeplinkAction.performAction(activity, it.positiveDeeplink)
                    dialog.dismiss()
                    activity.finish()
                }
                .setNegativeButton(it.negativeAction) { dialog, _ ->
                    deeplinkAction.performAction(activity, it.negativeDeeplink)
                    dialog.dismiss()
                    activity.finish()
                }
            val dialog = builder.create()
            dialog.show()
            dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE).isAllCaps =
                false
            dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_NEGATIVE).isAllCaps =
                false
        }
    }

    @Suppress("MemberVisibilityCanBePrivate")
    fun pickFile(activity: Activity) {
        mCameraPhotoPath = null
        var takePictureIntent: Intent? = null
        if (
            PermissionUtils.hasPermissions(activity, arrayOf(Manifest.permission.CAMERA))
            && mFileChooserParams?.acceptTypes?.any {
                it.contains(
                    "image",
                    true
                )
            } == true
        ) {
            takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            if (takePictureIntent.resolveActivity(activity.packageManager) != null) {
                // Create the File where the photo should go
                var photoFile: File? = null
                try {
                    photoFile = createImageFile()
                    takePictureIntent.putExtra("PhotoPath", mCameraPhotoPath)
                } catch (ex: IOException) {
                    // Error occurred while creating the File
                    Log.e(ex, TAG)
                }

                // Continue only if the File was successfully created
                if (photoFile != null) {
                    mCameraPhotoPath = "file:" + photoFile.absolutePath
                    takePictureIntent.putExtra(
                        MediaStore.EXTRA_OUTPUT,
                        Uri.fromFile(photoFile)
                    )
                } else {
                    takePictureIntent = null
                }
            }
        }

        val contentSelectionIntent = mFileChooserParams?.createIntent() ?: return

        val chooserIntent = Intent(Intent.ACTION_CHOOSER)
        chooserIntent.putExtra(Intent.EXTRA_INTENT, contentSelectionIntent)
        chooserIntent.putExtra(Intent.EXTRA_TITLE, mFileChooserParams?.title)
        takePictureIntent?.let {
            chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, arrayOf(takePictureIntent))
        }

        ActivityCompat.startActivityForResult(
            activity,
            chooserIntent,
            REQUEST_CODE_INPUT_FILE,
            null
        )
    }

    @Throws(IOException::class)
    private fun createImageFile(): File? {
        // Create an image file name
        val timeStamp: String =
            SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val imageFileName = "JPEG_" + timeStamp + "_"
        val storageDir =
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            imageFileName,  /* prefix */
            ".jpg",  /* suffix */
            storageDir /* directory */
        )
    }

    companion object {
        private const val TAG = "ProgressChromeClient"

        const val REQUEST_CODE_PERM_INPUT_FILE = 20041
        const val REQUEST_CODE_INPUT_FILE = 10041
    }

}

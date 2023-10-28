/**
 * Created by Sachin Saxena on 2020-10-14
 */

package com.theartofdev.edmodo.cropper

import android.Manifest
import android.animation.Animator
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.PorterDuff
import android.graphics.Rect
import android.graphics.RectF
import android.net.Uri
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.doubtnut.core.utils.ToastUtils
import com.theartofdev.edmodo.cropper.CropImageView.*
import kotlinx.android.synthetic.main.crop_image_activity.*
import java.io.File
import java.io.IOException

class CropImageFragment :
    DialogFragment(),
    OnSetImageUriCompleteListener,
    OnCropImageCompleteListener,
    View.OnClickListener,
    OnSetCropWindowChangeListener,
    OnSetCropOverlayMovedListener,
    OnSetCropOverlayReleasedListener,
    CropWindowInitListener {

    companion object {

        const val TAG = "CropImageFragment"

        fun newInstance(): CropImageFragment {
            return CropImageFragment()
        }
    }

    /**
     * Persist URI image to crop URI if specific permissions are required
     */
    private var mCropImageUri: Uri? = null
    private var eventStr = "CropPage"
    private var mOptions: CropImageOptions? = null
    private var cropWindowAdjusted = false
    private var getCropResultListener: GetCropResultListener? = null

    private var title: String? = null
    private var buttonTitle: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.dialog_theme)
        getIntentData()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is GetCropResultListener) {
            getCropResultListener = context
        } else {
            throw IllegalArgumentException("must implement GetCropResultListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        getCropResultListener = null
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.window?.requestFeature(Window.FEATURE_NO_TITLE)
        return dialog
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        updateUi()
        setCropImageOptions()
        setUpClickListeners()
        checkCameraPermission(savedInstanceState)
    }

    private fun updateUi() {
        title?.let { tvCropTitle.text = it }
        buttonTitle?.let { findSolutionButton.text = it }
        // Hide it here as it is added in the containing activity
        tvCropTitle?.visibility = View.GONE
    }

    private fun setCropImageOptions() {
        cropImageView.setOptions(mOptions)
        cropImageView.invalidate()
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        cropImageView.setOnSetImageUriCompleteListener(this)
        cropImageView.setOnCropImageCompleteListener(this)
        cropImageView.setOnCropWindowChangedListener(this)
        cropImageView.setOnSetCropOverlayMovedListener(this)
        cropImageView.setCropWindowInitListener(this)
    }

    override fun onStop() {
        super.onStop()
        cropImageView.setOnSetImageUriCompleteListener(null)
        cropImageView.setOnCropImageCompleteListener(null)
        cropImageView.setOnCropWindowChangedListener(null)
        cropImageView.setOnSetCropOverlayMovedListener(null)
        cropImageView.setCropWindowInitListener(null)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        // handle result of pick image chooser
        if (requestCode == CropImage.PICK_IMAGE_CHOOSER_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_CANCELED) {
                // User cancelled the picker. We don't have anything to crop
                setResultCancel()
            }
            if (resultCode == Activity.RESULT_OK) {
                mCropImageUri = CropImage.getPickImageResultUri(requireContext(), data)
                    ?: return setResultCancel()

                // For API >= 23 we need to check specifically that we have permissions to read external storage.
                if (CropImage.isReadExternalStoragePermissionsRequired(requireContext(), mCropImageUri!!)) {
                    // request permissions and handle the result in onRequestPermissionsResult()
                    requestPermissions(
                        arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                        CropImage.PICK_IMAGE_PERMISSIONS_REQUEST_CODE
                    )
                } else {
                    // no permissions required or already grunted, can start crop image activity
                    cropImageView.setImageUriAsync(mCropImageUri)
                }
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        if (requestCode == CropImage.PICK_IMAGE_PERMISSIONS_REQUEST_CODE) {
            if (mCropImageUri != null && grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // required permissions granted, start crop image activity
                cropImageView.setImageUriAsync(mCropImageUri)
            } else {
                ToastUtils.makeText(requireContext(), R.string.crop_image_activity_no_permissions, Toast.LENGTH_LONG).show()
                setResultCancel()
            }
        }
        if (requestCode == CropImage.CAMERA_CAPTURE_PERMISSIONS_REQUEST_CODE) {
            // Irrespective of whether camera permission was given or not, we show the picker
            // The picker will not add the camera intent if permission is not available
            CropImage.startPickImageActivity(requireActivity())
        }
    }

    private fun getIntentData() {
        mCropImageUri = arguments?.getParcelable(CropImage.CROP_IMAGE_EXTRA_SOURCE)
        mOptions = arguments?.getParcelable(CropImage.CROP_IMAGE_EXTRA_OPTIONS)
        title = arguments?.getString(CropConstant.INTENT_EXTRA_KEY_TITLE)
        buttonTitle = arguments?.getString(CropConstant.INTENT_EXTRA_KEY_FIND_SOLUTION_BUTTON_TEXT)
    }

    private fun checkCameraPermission(savedInstanceState: Bundle?) {
        if (savedInstanceState == null) {
            if (mCropImageUri == null || mCropImageUri == Uri.EMPTY) {
                if (CropImage.isExplicitCameraPermissionRequired(requireContext())) {
                    // request permissions and handle the result in onRequestPermissionsResult()
                    requestPermissions(
                        arrayOf(Manifest.permission.CAMERA),
                        CropImage.CAMERA_CAPTURE_PERMISSIONS_REQUEST_CODE
                    )
                } else {
                    CropImage.startPickImageActivity(requireActivity())
                }
            } else if (CropImage.isReadExternalStoragePermissionsRequired(requireContext(), mCropImageUri!!)) {
                // request permissions and handle the result in onRequestPermissionsResult()
                requestPermissions(
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                    CropImage.PICK_IMAGE_PERMISSIONS_REQUEST_CODE
                )
            } else {
                // no permissions required or already grunted, can start crop image activity
                cropImageView.setImageUriAsync(mCropImageUri)
            }
        }
    }

    private fun setUpClickListeners() {
        findSolutionButton.setOnClickListener(object : DebouncedOnClickListener(2000) {
            override fun onDebouncedClick(v: View?) {
                cropImage()
                eventStr = "$eventStr:SubmitCropButton"
            }
        })
        textViewRotate.setOnClickListener(this)
        textViewRetake.setOnClickListener(this)
    }

    private fun playCropAnimation() {
        val toShowAnimation = PreferenceManager.getDefaultSharedPreferences(requireContext()).getBoolean(CropConstant.PLAY_CROP_ANIMATION, true)
        if (toShowAnimation) {
            cropAnimation!!.visibility = View.VISIBLE
            cropAnimation!!.playAnimation()
            cropImageView.cropOverlayView.visibility = View.GONE
            addAnimationListener()
            PreferenceManager.getDefaultSharedPreferences(requireContext()).edit().putBoolean(CropConstant.PLAY_CROP_ANIMATION, false).apply()
        }
    }

    private fun addAnimationListener() {
        cropAnimation!!.setOnClickListener {
            cropAnimation!!.clearAnimation()
            cropAnimation!!.visibility = View.GONE
            cropImageView.cropOverlayView.visibility = View.VISIBLE
        }
        cropAnimation!!.addAnimatorListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animation: Animator) {}
            override fun onAnimationEnd(animation: Animator) {
                cropImageView.cropOverlayView.visibility = View.VISIBLE
                cropAnimation!!.clearAnimation()
                cropAnimation!!.visibility = View.GONE
            }

            override fun onAnimationCancel(animation: Animator) {}
            override fun onAnimationRepeat(animation: Animator) {}
        })
    }

    override fun onSetImageUriComplete(view: CropImageView, uri: Uri, error: Exception?) {
        if (error == null) {
            playCropAnimation()
            if (mOptions!!.initialCropWindowRectangle != null) {
                cropImageView.cropRect = mOptions!!.initialCropWindowRectangle
            }
            if (mOptions!!.initialRotation > -1) {
                cropImageView.rotatedDegrees = mOptions!!.initialRotation
            }
            onCropOverlayMoved(view.cropRect, view.cropWindowRect)
        } else {
            setResult(null, error, 1)
        }
    }

    override fun onCropImageComplete(view: CropImageView, result: CropResult) {
        setResult(result.uri, result.error, result.sampleSize)
    }

    override fun onClick(v: View) {
        if (v === textViewRetake) {
            setResultCancel()
            dismiss()
        } else if (v === textViewRotate) {
            rotateImage(mOptions!!.rotationDegrees)
        }
    }
    // region: Private methods
    /**
     * Execute crop image and save the result tou output uri.
     */
    private fun cropImage() {
        if (mOptions!!.noOutputImage) {
            setResult(null, null, 1)
        } else {
            cropImageView.saveCroppedImageAsync(
                outputUri,
                mOptions!!.outputCompressFormat,
                mOptions!!.outputCompressQuality,
                mOptions!!.outputRequestWidth,
                mOptions!!.outputRequestHeight,
                mOptions!!.outputRequestSizeOptions
            )
        }
    }

    /**
     * Rotate the image in the crop image view.
     */
    private fun rotateImage(degrees: Int) {
        cropImageView.rotateImage(degrees)
    }

    private val outputUri: Uri?
        get() {
            var outputUri = mOptions!!.outputUri
            if (outputUri == null || outputUri == Uri.EMPTY) {
                outputUri = try {
                    val ext = if (mOptions!!.outputCompressFormat == Bitmap.CompressFormat.JPEG) ".jpg" else if (mOptions!!.outputCompressFormat == Bitmap.CompressFormat.PNG) ".png" else ".webp"
                    Uri.fromFile(File.createTempFile("cropped", ext, requireActivity().cacheDir))
                } catch (e: IOException) {
                    throw RuntimeException("Failed to create temp file for output image", e)
                }
            }
            return outputUri
        }

    /**
     * Result with cropped image data or error if failed.
     */
    private fun setResult(uri: Uri?, error: Exception?, sampleSize: Int) {
        val resultCode = if (error == null) Activity.RESULT_OK else CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE
        if (uri != null) {
            getCropResultListener!!.setIntentForResult(getResultIntent(uri, error, sampleSize), resultCode)
        } else {
            ToastUtils.makeText(requireContext(), "Your Image size is 0 KB please choose another image", Toast.LENGTH_LONG).show()
            setResultCancel()
        }
    }

    /**
     * Cancel of cropping fragment.
     */
    private fun setResultCancel() {
        getCropResultListener!!.setIntentForResult(null, Activity.RESULT_CANCELED)
    }

    /**
     * Get intent instance to be used for the result of this activity.
     */
    private fun getResultIntent(uri: Uri?, error: Exception?, sampleSize: Int): Intent {
        val result = CropImage.ActivityResult(
            cropImageView.imageUri,
            uri,
            error,
            cropImageView.cropPoints,
            cropImageView.cropRect,
            cropImageView.rotatedDegrees,
            cropImageView.wholeImageRect,
            sampleSize
        )
        val intent = Intent()
        intent.putExtra(CropImage.CROP_IMAGE_EXTRA_RESULT, result)
        intent.putExtra("check_back", eventStr)
        intent.putExtra(CropConstant.RESULT_INTENT_EXTRA_KEY_CROP_WINDOW_ADJUSTED, cropWindowAdjusted)
        return intent
    }

    /**
     * Update the color of a specific menu item to the given color.
     */
    private fun updateMenuItemIconColor(menu: Menu, itemId: Int, color: Int) {
        val menuItem = menu.findItem(itemId)
        if (menuItem != null) {
            val menuItemIcon = menuItem.icon
            if (menuItemIcon != null) {
                try {
                    menuItemIcon.mutate()
                    menuItemIcon.setColorFilter(color, PorterDuff.Mode.SRC_ATOP)
                    menuItem.icon = menuItemIcon
                } catch (e: Exception) {
                    Log.w("AIC", "Failed to update menu item color", e)
                }
            }
        }
    }

    fun getCropImageView(): CropImageView = cropImageView

    override fun onCropWindowChanged() {
        cropWindowAdjusted = true
    }

    override fun onCropOverlayMoved(rect: Rect?, cropWindowRect: RectF?) {
        if (rect != null && cropWindowRect != null) {
            getCropResultListener?.onCropOverlayMoved(rect, cropWindowRect)
        }
    }

    override fun onCropOverlayReleased(rect: Rect?, cropWindowRect: RectF?) {
        if (rect != null && cropWindowRect != null) {
            getCropResultListener?.onCropOverlayReleased(rect, cropWindowRect)
        }
    }

    override fun onCropWindowInit(cropWidowRect: RectF) {
        getCropResultListener?.onCropWindowInit(cropWidowRect)
    }

    interface GetCropResultListener {
        fun setIntentForResult(intent: Intent?, resultCode: Int)
        fun onCropOverlayMoved(rect: Rect, cropWindowRect: RectF) {}
        fun onCropOverlayReleased(rect: Rect, cropWindowRect: RectF) {}
        fun onCropWindowInit(cropWindowRect: RectF) {}
    }
}

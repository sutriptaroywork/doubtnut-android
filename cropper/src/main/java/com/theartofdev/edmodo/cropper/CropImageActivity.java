// "Therefore those skilled at the unorthodox
// are infinite as heaven and earth,
// inexhaustible as the great rivers.
// When they come to an end,
// they begin again,
// like the days and months;
// they die and are reborn,
// like the four seasons."
//
// - Sun Tsu,
// "The Art of War"

package com.theartofdev.edmodo.cropper;

import android.Manifest;
import android.animation.Animator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.airbnb.lottie.LottieAnimationView;
import com.doubtnut.core.constant.CoreEventConstants;
import com.doubtnut.core.utils.ToastUtils;
import com.doubtnut.core.view.audiotooltipview.AudioTooltipView;
import com.doubtnut.core.view.audiotooltipview.AudioTooltipViewData;

import java.io.File;
import java.io.IOException;


public class CropImageActivity extends AppCompatActivity implements CropImageView.OnSetImageUriCompleteListener,
        CropImageView.OnCropImageCompleteListener, View.OnClickListener, CropImageView.OnSetCropWindowChangeListener {

    /**
     * The crop image view library widget used in the activity
     */
    private CropImageView mCropImageView;

    /**
     * Persist URI image to crop URI if specific permissions are required
     */

    private Uri mCropImageUri;

    private Button findSolutionButton;
    private TextView backButton;
    private LottieAnimationView cropAnimation;

    String eventStr = "CropPage";
    private CropImageOptions mOptions;
    private TextView rotateButton;

    boolean cropWindowAdjusted = false;

    private boolean isToastShown = false;
    private boolean isForStatus = false;
    private String mImageSource = "";
    private String source = "";

    private boolean isRotated = false;
    private boolean isCropped = false;

    private static final String SOURCE_STUDY_GROUP = "study_group";

    @Override
    @SuppressLint("NewApi")
    public void onCreate(Bundle savedInstanceState) {
        setStatusBarColor();
        super.onCreate(savedInstanceState);

        Bundle bundle = getIntent().getBundleExtra(CropImage.CROP_IMAGE_EXTRA_BUNDLE);
        isForStatus = bundle.getBoolean("isForStatus", false);
        mImageSource = bundle.getString("image_source", "");
        source = bundle.getString("source", "");

        String audioUrl = bundle.getString(CropConstant.AUDIO_URL, "");
        String tooltipText = bundle.getString(CropConstant.MUTE_TEXT, "");
        String muteImageUrl = bundle.getString(CropConstant.IMAGE_URL_MUTE, "");
        String unmuteImageUrl = bundle.getString(CropConstant.IMAGE_URL_UNMUTE, "");

        if (isForStatus && !source.equals(SOURCE_STUDY_GROUP)) {
            setContentView(R.layout.crop_status_image_activity);
        } else {
            setContentView(R.layout.crop_image_activity);
        }
        mCropImageView = findViewById(R.id.cropImageView);
        rotateButton = findViewById(R.id.textViewRotate);
        findSolutionButton = findViewById(R.id.findSolutionButton);
        backButton = findViewById(R.id.textViewRetake);
        cropAnimation = findViewById(R.id.cropAnimation);
        TextView titleText = findViewById(R.id.tvCropTitle);

        mCropImageUri = bundle.getParcelable(CropImage.CROP_IMAGE_EXTRA_SOURCE);
        mOptions = bundle.getParcelable(CropImage.CROP_IMAGE_EXTRA_OPTIONS);

        if (!isForStatus) {
            String title = bundle.getString(CropConstant.INTENT_EXTRA_KEY_TITLE);
            if (title != null) titleText.setText(title);
        } else {
            titleText.setVisibility(View.GONE);
        }

        AudioTooltipView audioPlayer = findViewById(R.id.audioPlayer);
        if (!audioUrl.isEmpty() && !isForStatus) {
            audioPlayer.setVisibility(View.VISIBLE);
            AudioTooltipViewData audioTooltipViewData = new AudioTooltipViewData.Builder()
                    .audioUrl(audioUrl)
                    .tooltipText(tooltipText)
                    .muteImageUrl(muteImageUrl)
                    .unMuteImageUrl(unmuteImageUrl)
                    .screenName(CoreEventConstants.SCREEN_CROP)
                    .build();
            audioPlayer.setData(audioTooltipViewData);
            audioPlayer.registerLifecycle(getLifecycle());
        }

        String findSolutionButtonText = bundle.getString(CropConstant.INTENT_EXTRA_KEY_FIND_SOLUTION_BUTTON_TEXT);
        if (findSolutionButtonText != null) findSolutionButton.setText(findSolutionButtonText);

        if (savedInstanceState == null) {
            if (mCropImageUri == null || mCropImageUri.equals(Uri.EMPTY)) {
                if (CropImage.isExplicitCameraPermissionRequired(this)) {
                    // request permissions and handle the result in onRequestPermissionsResult()
                    requestPermissions(
                            new String[]{Manifest.permission.CAMERA},
                            CropImage.CAMERA_CAPTURE_PERMISSIONS_REQUEST_CODE);
                } else {
                    CropImage.startPickImageActivity(this);
                }
            } else if (CropImage.isReadExternalStoragePermissionsRequired(this, mCropImageUri)) {
                // request permissions and handle the result in onRequestPermissionsResult()
                requestPermissions(
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        CropImage.PICK_IMAGE_PERMISSIONS_REQUEST_CODE);
            } else {
                // no permissions required or already grunted, can start crop image activity
                mCropImageView.setImageUriAsync(mCropImageUri);
            }
        }

        findSolutionButton.setOnClickListener(this);
        rotateButton.setOnClickListener(this);
        backButton.setOnClickListener(this);

    }

    @Override
    protected void onStart() {
        super.onStart();
        mCropImageView.setOnSetImageUriCompleteListener(this);
        mCropImageView.setOnCropImageCompleteListener(this);
        mCropImageView.setOnCropWindowChangedListener(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        mCropImageView.setOnSetImageUriCompleteListener(null);
        mCropImageView.setOnCropImageCompleteListener(null);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.crop_image_menu, menu);

        if (!mOptions.allowRotation) {
            menu.removeItem(R.id.crop_image_menu_rotate_left);
            menu.removeItem(R.id.crop_image_menu_rotate_right);
        } else if (mOptions.allowCounterRotation) {
            menu.findItem(R.id.crop_image_menu_rotate_left).setVisible(true);
        }

        if (mOptions.activityMenuIconColor != 0) {
            updateMenuItemIconColor(
                    menu, R.id.crop_image_menu_rotate_left, mOptions.activityMenuIconColor);
            updateMenuItemIconColor(
                    menu, R.id.crop_image_menu_rotate_right, mOptions.activityMenuIconColor);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == R.id.crop_image_menu_rotate_left) {
            rotateImage(-mOptions.rotationDegrees);
            eventStr = eventStr + ":" + "minusrotateButton";
            return true;
        }
        if (item.getItemId() == R.id.crop_image_menu_rotate_right) {
            rotateImage(mOptions.rotationDegrees);
            eventStr = eventStr + ":" + "plusrotateButton";
            return true;
        }

        if (item.getItemId() == android.R.id.home) {
            setResultCancel();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    @SuppressLint("NewApi")
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        // handle result of pick image chooser
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CropImage.PICK_IMAGE_CHOOSER_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_CANCELED) {
                // User cancelled the picker. We don't have anything to crop
                setResultCancel();
            }

            if (resultCode == Activity.RESULT_OK) {
                mCropImageUri = CropImage.getPickImageResultUri(this, data);

                // For API >= 23 we need to check specifically that we have permissions to read external
                // storage.
                if (CropImage.isReadExternalStoragePermissionsRequired(this, mCropImageUri)) {
                    // request permissions and handle the result in onRequestPermissionsResult()
                    requestPermissions(
                            new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                            CropImage.PICK_IMAGE_PERMISSIONS_REQUEST_CODE);
                } else {
                    // no permissions required or already grunted, can start crop image activity
                    mCropImageView.setImageUriAsync(mCropImageUri);

                }
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CropImage.PICK_IMAGE_PERMISSIONS_REQUEST_CODE) {
            if (mCropImageUri != null
                    && grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // required permissions granted, start crop image activity


                mCropImageView.setImageUriAsync(mCropImageUri);
            } else {
                ToastUtils.makeText(this, R.string.crop_image_activity_no_permissions, Toast.LENGTH_LONG).show();
                setResultCancel();
            }

        }

        if (requestCode == CropImage.CAMERA_CAPTURE_PERMISSIONS_REQUEST_CODE) {
            // Irrespective of whether camera permission was given or not, we show the picker
            // The picker will not add the camera intent if permission is not available
            CropImage.startPickImageActivity(this);
        }
    }

    @Override
    public void onSetImageUriComplete(CropImageView view, Uri uri, Exception error) {
        if (error == null) {

            playCropAnimation();

            if (mOptions.initialCropWindowRectangle != null) {
                mCropImageView.setCropRect(mOptions.initialCropWindowRectangle);
            }
            if (mOptions.initialRotation > -1) {
                mCropImageView.setRotatedDegrees(mOptions.initialRotation);
            }
        } else {
            setResult(null, error, 1);
        }
    }

    private void playCropAnimation() {
        if (isForStatus) {
            return;
        }

        boolean toShowAnimation = PreferenceManager.getDefaultSharedPreferences(this).getBoolean(CropConstant.PLAY_CROP_ANIMATION, true);

        if (toShowAnimation) {
            cropAnimation.setVisibility(View.VISIBLE);
            cropAnimation.playAnimation();
            mCropImageView.getCropOverlayView().setVisibility(View.GONE);
            addAnimationListener();

            PreferenceManager.getDefaultSharedPreferences(this).edit().putBoolean(CropConstant.PLAY_CROP_ANIMATION, false).apply();
        }
    }

    private void addAnimationListener() {

        cropAnimation.setOnClickListener(v -> {
            cropAnimation.clearAnimation();
            cropAnimation.setVisibility(View.GONE);
            mCropImageView.getCropOverlayView().setVisibility(View.VISIBLE);
        });

        cropAnimation.addAnimatorListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                mCropImageView.getCropOverlayView().setVisibility(View.VISIBLE);
                cropAnimation.clearAnimation();
                cropAnimation.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationCancel(Animator animation) {
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
            }
        });
    }

    @Override
    public void onCropImageComplete(CropImageView view, CropImageView.CropResult result) {
        setResult(result.getUri(), result.getError(), result.getSampleSize());
    }

    @Override
    public void onClick(View v) {

        if (v == findSolutionButton) {
            cropImage();
            eventStr = eventStr + ":" + "SubmitCropButton";
        } else if (v == backButton) {
            setResultCancel();
        } else if (v == rotateButton) {
            rotateImage(mOptions.rotationDegrees);
            isRotated = true;
        }
    }

    // region: Private methods

    /**
     * Execute crop image and save the result tou output uri.
     */
    protected void cropImage() {
        if (mOptions.noOutputImage) {
            setResult(null, null, 1);
        } else {
            isCropped = true;
            Uri outputUri = getOutputUri();
            mCropImageView.saveCroppedImageAsync(
                    outputUri,
                    mOptions.outputCompressFormat,
                    mOptions.outputCompressQuality,
                    mOptions.outputRequestWidth,
                    mOptions.outputRequestHeight,
                    mOptions.outputRequestSizeOptions);
        }
    }

    /**
     * Rotate the image in the crop image view.
     */
    protected void rotateImage(int degrees) {
        mCropImageView.rotateImage(degrees);
    }

    protected Uri getOutputUri() {
        Uri outputUri = mOptions.outputUri;
        if (outputUri == null || outputUri.equals(Uri.EMPTY)) {
            try {
                String ext =
                        mOptions.outputCompressFormat == Bitmap.CompressFormat.JPEG
                                ? ".jpg"
                                : mOptions.outputCompressFormat == Bitmap.CompressFormat.PNG ? ".png" : ".webp";
                outputUri = Uri.fromFile(File.createTempFile("cropped", ext, getCacheDir()));
            } catch (IOException e) {
                throw new RuntimeException("Failed to create temp file for output image", e);
            }
        }
        return outputUri;
    }

    /**
     * Result with cropped image data or error if failed.
     */
    protected void setResult(final Uri uri, final Exception error, final int sampleSize) {
        int resultCode = error == null ? RESULT_OK : CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE;
        if (uri != null) {
            setResult(resultCode, getResultIntent(uri, error, sampleSize));
            finish();
        } else {
            ToastUtils.makeText(CropImageActivity.this, "Your Image size is 0 KB please choose another image", Toast.LENGTH_LONG).show();
            setResultCancel();
        }
    }

    /**
     * Cancel of cropping activity.
     */
    protected void setResultCancel() {
        setResult(RESULT_CANCELED);
        finish();
    }


    /**
     * Get intent instance to be used for the result of this activity.
     */
    protected Intent getResultIntent(final Uri uri, final Exception error, final int sampleSize) {

        CropImage.ActivityResult result =
                new CropImage.ActivityResult(
                        mCropImageView.getImageUri(),
                        uri,
                        error,
                        mCropImageView.getCropPoints(),
                        mCropImageView.getCropRect(),
                        mCropImageView.getRotatedDegrees(),
                        mCropImageView.getWholeImageRect(),
                        sampleSize
                );
        Intent intent = new Intent();
        intent.putExtras(getIntent());
        intent.putExtra(CropImage.CROP_IMAGE_EXTRA_RESULT, result);
        intent.putExtra("check_back", eventStr);
        intent.putExtra("rotated", isRotated);
        intent.putExtra("cropped", isCropped);
        intent.putExtra("image_source", mImageSource);
        intent.putExtra(CropConstant.RESULT_INTENT_EXTRA_KEY_CROP_WINDOW_ADJUSTED, cropWindowAdjusted);
        if (isForStatus && !source.equals(SOURCE_STUDY_GROUP)) {
            EditText etCaption = findViewById(R.id.editTextCaption);
            intent.putExtra("caption", etCaption.getText().toString());
        }
        return intent;
    }

    /**
     * Update the color of a specific menu item to the given color.
     */
    private void updateMenuItemIconColor(Menu menu, int itemId, int color) {
        MenuItem menuItem = menu.findItem(itemId);
        if (menuItem != null) {
            Drawable menuItemIcon = menuItem.getIcon();
            if (menuItemIcon != null) {
                try {
                    menuItemIcon.mutate();
                    menuItemIcon.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
                    menuItem.setIcon(menuItemIcon);
                } catch (Exception e) {
                    Log.w("AIC", "Failed to update menu item color", e);
                }
            }
        }
    }

    // Remove inter-activity transition to avoid screen tossing on tapping bottom navigation items
    @Override
    public void onPause() {
        super.onPause();
        overridePendingTransition(0, 0);
    }

    @Override
    public void onBackPressed() {
        if (!isToastShown) {
            ToastUtils.makeText(this, "Tap again to Exit", Toast.LENGTH_LONG).show();
            isToastShown = true;
        } else {
            super.onBackPressed();
            setResultCancel();
        }
    }

    void setStatusBarColor() {
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.setStatusBarColor(ContextCompat.getColor(this, R.color.colorPrimary));
    }

    public void onCropWindowChanged() {
        cropWindowAdjusted = true;
    }
}
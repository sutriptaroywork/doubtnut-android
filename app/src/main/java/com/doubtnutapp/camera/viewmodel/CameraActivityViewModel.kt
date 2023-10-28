package com.doubtnutapp.camera.viewmodel

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.database.ContentObserver
import android.graphics.Bitmap
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.text.TextUtils
import android.util.Log
import androidx.core.content.edit
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.paging.Config
import androidx.paging.PagedList
import androidx.paging.toLiveData
import androidx.work.*
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnutapp.*
import com.doubtnutapp.R
import com.doubtnutapp.base.BaseViewModel
import com.doubtnutapp.base.extension.applyIoToMainSchedulerOnCompletable
import com.doubtnutapp.base.extension.applyIoToMainSchedulerOnSingle
import com.doubtnutapp.base.extension.subscribeToSingle
import com.doubtnutapp.bottomnavigation.model.BottomNavigationItemData
import com.doubtnutapp.camera.interactor.*
import com.doubtnutapp.data.Outcome
import com.doubtnutapp.data.common.UserPreference
import com.doubtnutapp.data.remote.DataHandler
import com.doubtnutapp.data.remote.RetrofitLiveData
import com.doubtnutapp.data.remote.models.ApiResponse
import com.doubtnutapp.data.remote.models.feed.TopOptionWidgetItem
import com.doubtnutapp.data.remote.repository.ProfileRepository
import com.doubtnutapp.data.remote.repository.TopOptionRepository
import com.doubtnutapp.data.remote.repository.UserActivityRepository
import com.doubtnutapp.db.DoubtnutDatabase
import com.doubtnutapp.db.entity.LocalOfflineOcr
import com.doubtnutapp.domain.camerascreen.entity.*
import com.doubtnutapp.downloadedVideos.DownloadedVideoRefresherWorker
import com.doubtnutapp.gallery.model.GalleryImageViewItem
import com.doubtnutapp.gallery.repository.GalleryImagesDataSource
import com.doubtnutapp.gallery.repository.GalleryImagesDataSourceFactory
import com.doubtnutapp.imagedirectory.model.ImageBucket
import com.doubtnutapp.ui.main.event.CameraEventManager
import com.doubtnutapp.ui.main.samplequestion.BackPressSampleQuestionFragmentV2
import com.doubtnutapp.utils.Event
import com.doubtnutapp.utils.UserUtil.getStudentClass
import com.doubtnutapp.utils.UserUtil.getStudentId
import com.doubtnutapp.utils.Utils
import com.doubtnutapp.workmanager.workers.OcrFromImageNotificationWorker
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import okhttp3.ResponseBody
import retrofit2.HttpException
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.net.HttpURLConnection
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlin.math.sqrt

class CameraActivityViewModel @Inject constructor(
    compositeDisposable: CompositeDisposable,
    private val getCameraConfigValue: GetCameraConfigValue,
    private val getCropConfigValue: GetCropScreenConfigValue,
    private val saveCropScreenConfigValue: SaveCropScreenConfigValue,
    private val checkTrickyQuestionButtonShown: CheckTrickyQuestionButtonShown,
    private val setIsCameraScreenShownFirstToTrue: SetIsCameraScreenShownFirstToTrue,
    private val setTrickyQuestionShownToTrue: SetTrickyQuestionShownToTrue,
    private val saveSelfieDetectedImageUseCase: SaveSelfieDetectedImageUseCase,
    private val getCameraSettingConfig: GetCameraSettingConfig,
    private val demoAnimationDetails: GetDemoAnimationDetails,
    private val cameraEventManager: CameraEventManager,
    private val getPackageStatusUseCase: GetPackageStatusUseCase,
    private val profileRepository: ProfileRepository,
    private val userActivityRepository: UserActivityRepository,
    private val database: DoubtnutDatabase,
    private val topOptionRepository: TopOptionRepository,
    private val userPreference: UserPreference
) : BaseViewModel(compositeDisposable) {

    var isForStatus = false

    val navigateAsk = MutableLiveData<Event<Uri>>()

    private val _cameraSettingConfig = MutableLiveData<Outcome<CameraSettingEntity>>()

    val cameraSettingConfig: LiveData<Outcome<CameraSettingEntity>>
        get() = _cameraSettingConfig

    private val _cameraConfig = MutableLiveData<CameraConfigEntity>()

    val cameraConfig: LiveData<CameraConfigEntity>
        get() = _cameraConfig

    private val _cropConfig = MutableLiveData<Event<Pair<CropScreenConfigEntity, Uri?>>>()

    val cropConfig: LiveData<Event<Pair<CropScreenConfigEntity, Uri?>>>
        get() = _cropConfig

    private val _showTrickyQuestionButtonLivedata = MutableLiveData<Boolean>()

    val showTrickyQuestionButtonLivedata: LiveData<Boolean>
        get() = _showTrickyQuestionButtonLivedata

    private val _demoAnimationList = MutableLiveData<Outcome<List<DemoAnimationEntity>>>()

    val demoAnimationList: LiveData<Outcome<List<DemoAnimationEntity>>>
        get() = _demoAnimationList

    private val _removeSampleQuestionFragment = MutableLiveData<Boolean>()

    val removeSampleQuestionFragment: LiveData<Boolean>
        get() = _removeSampleQuestionFragment

    private val _launchSampleQuestionFragment = MutableLiveData<Boolean>()

    val launchSampleQuestionFragment: LiveData<Boolean>
        get() = _launchSampleQuestionFragment

    private val _launchDemoAnimation = MutableLiveData<Pair<Boolean, Int>>()

    val launchDemoAnimation: LiveData<Pair<Boolean, Int>>
        get() = _launchDemoAnimation

    private val _removeDemoAnimation = MutableLiveData<Boolean>()

    val removeDemoAnimation: LiveData<Boolean>
        get() = _removeDemoAnimation

    private val _screenOrientationLiveData = MutableLiveData<Int>()

    val screenOrientationLiveData: LiveData<Int>
        get() = _screenOrientationLiveData

    val galleryImageListLiveData: LiveData<PagedList<GalleryImageViewItem>> by lazy {
        GalleryImagesDataSourceFactory(compositeDisposable).toLiveData(
            Config(GalleryImagesDataSource.PAGE_SIZE, enablePlaceholders = false)
        )
    }

    private val _imageDirectoryListLiveData = MutableLiveData<List<ImageBucket>>()

    val imageBucketListLiveData: LiveData<List<ImageBucket>>
        get() = _imageDirectoryListLiveData

    val storageReadPermissionReceivedLiveData = MutableLiveData<Boolean>()

    val loadAllImagesInGalleryLiveData = MutableLiveData<Boolean>()

    val refreshGalleryFragmentImagesLiveData = MutableLiveData<Boolean>()

    private val _isAnyImagePresentInStorageLiveData = MutableLiveData<Boolean>()

    val isAnyImagePresentInStorageLiveData: LiveData<Boolean>
        get() = _isAnyImagePresentInStorageLiveData

    private var _anyFaceExists: Boolean = false
        set(value) {
            field = value
            anyFaceExists = value
        }

    // Returns containing value only on first access, returns false otherwise, until set again
    // Like a one-time event
    // Value is set in the setter of backing property
    var anyFaceExists: Boolean = _anyFaceExists
        get() {
            val value = field
            field = false
            return value
        }
        private set

    private val _navigationBottomSheetIconItemsLiveData =
        MutableLiveData<List<TopOptionWidgetItem>>()

    val navigationBottomSheetIconItemsLiveData: LiveData<List<TopOptionWidgetItem>>
        get() = _navigationBottomSheetIconItemsLiveData

    var backPressWidgets: List<WidgetEntityModel<*, *>>? = null

    var deepLinkForBackPressDialog: String? = null

    var backPressWidgetType: String = ""

    // For D0 user - if press back, can't go to home screen
    var shouldD0UserExitAppIfPressBackButton: Boolean? = false
    val hideAskHistory = MutableLiveData<Boolean?>()
    val hideSearchIcon = MutableLiveData<Boolean?>()

    fun processCameraImage(data: ByteArray) {

        val destinationFileName = "CameraTempImage" + ".jpg"

        doAsyncPost(handler = {
            val file = File(getApplication().cacheDir, destinationFileName)
            var os: OutputStream? = null
            try {
                os = FileOutputStream(file)
                os.write(data)
                os.close()
            } catch (e: IOException) {
                Log.w("MainViewModel", "Cannot write to $file", e)
            } finally {
                if (os != null) {
                    try {
                        os.close()
                    } catch (e: IOException) {
                        // Ignore
                    }
                }
            }
        }, postHandler = {
            navigateAsk.value =
                Event(Uri.fromFile(File(getApplication().cacheDir, destinationFileName)))
        }).execute()
    }

    fun processCameraImage(file: File?) {
        navigateAsk.postValue(Event(Uri.fromFile(file)))
    }

    fun processGalleryImage(uri: Uri) {
        navigateAsk.value = Event(uri)
    }

    fun updateProfileClass(): RetrofitLiveData<ApiResponse<ResponseBody>> {
        return profileRepository.updateProfile(
            Utils.getUpdateProfileClassBody(getStudentClass(), Utils.getEmail(getApplication()))
                .toRequestBody()
        )
    }

    fun getCameraScreenConfig() {
        compositeDisposable + getCameraConfigValue.execute(Unit)
            .applyIoToMainSchedulerOnSingle()
            .subscribeToSingle(this::onCameraScreenConfigSuccess)
    }

    fun getCameraSetting(hasCameraPermission: Boolean) {
        _cameraSettingConfig.value = Outcome.loading(true)
        compositeDisposable + getCameraSettingConfig.execute(
            GetCameraSettingConfig.Param(
                hasCameraPermission
            )
        )
            .applyIoToMainSchedulerOnSingle()
            .map {
                backPressWidgetType = ""
                it.widgets?.mapIndexed { index, widget ->
                    if (widget != null) {
                        if (widget.extraParams == null) {
                            widget.extraParams = hashMapOf()
                        }
                        widget.extraParams?.put(
                            EventConstants.EVENT_SCREEN_PREFIX + EventConstants.NAME,
                            BackPressSampleQuestionFragmentV2.TAG
                        )
                        widget.extraParams?.put(
                            EventConstants.ITEM_PARENT_POSITION,
                            index
                        )
                        if (index < 5) {
                            backPressWidgetType += widget.type
                        }
                    }
                }
                shouldD0UserExitAppIfPressBackButton = it.d0UserData?.exitOnBackPress
                hideAskHistory.postValue(it.d0UserData?.hideAskHIstory)
                hideSearchIcon.postValue(it.d0UserData?.hideSearchIcon)

                defaultPrefs().edit {
                    putString(
                        Constants.DEMO_QUESTION_URL,
                        it.bottomOverlay?.subjectList?.getOrNull(0)?.imageUrl
                    )
                }
                it
            }
            .subscribeToSingle({
                backPressWidgets = it.widgets
                deepLinkForBackPressDialog = it.deepLink
                _cameraSettingConfig.value = Outcome.loading(false)
                _cameraSettingConfig.value = Outcome.success(it)
            }, {})
    }

    fun getDemoAnimation() {
        _demoAnimationList.value = Outcome.loading(true)
        compositeDisposable + demoAnimationDetails.execute(Unit)
            .applyIoToMainSchedulerOnSingle()
            .subscribeToSingle({
                _demoAnimationList.value = Outcome.loading(false)
                _demoAnimationList.value = Outcome.success(it)
            }, {
                print("")
            })
    }

    fun getCropScreenConfig(imageUri: Uri?) {
        compositeDisposable + getCropConfigValue.execute(Unit)
            .applyIoToMainSchedulerOnSingle()
            .subscribeToSingle({
                _cropConfig.value = Event(Pair(it, imageUri))
            })
    }

    fun saveCropScreenConfigValue() {
        compositeDisposable + saveCropScreenConfigValue.execute(Unit)
            .applyIoToMainSchedulerOnCompletable().subscribe()
    }

    fun detectFaceInImage(imageUri: Uri) {
        try {
            val image = InputImage.fromFilePath(getApplication(), imageUri)
            val minFacePercent = .1f
            val minFaceSize = sqrt(minFacePercent)
            val options = FaceDetectorOptions.Builder()
                .setMinFaceSize(minFaceSize)
                .build()

            FaceDetection.getClient(options)
                .process(image)
                .addOnSuccessListener { faceList ->
                    _anyFaceExists = faceList.any {
                        isFaceBigEnough(minFacePercent, it, image)
                    }
                    if (_anyFaceExists) {
                        image.bitmapInternal?.let { bitmap ->
                            compositeDisposable + convertBitmapToBase64(bitmap)
                                .subscribeOn(Schedulers.computation())
                                .doOnSuccess { base64String ->
                                    saveSelfieDetectedImage(base64String)
                                }.subscribe()
                        }
                    }
                }
        } catch (t: Throwable) {
            com.doubtnutapp.Log.e(t, "Selfie detection exception")
        }
    }

    private fun isFaceBigEnough(minFacePercent: Float, face: Face, image: InputImage): Boolean {
        val imageArea = image.width * image.height
        val faceArea = face.boundingBox.run { width() * height() }
        return (faceArea.toFloat() / imageArea) >= minFacePercent
    }

    private fun convertBitmapToBase64(bitmap: Bitmap): Single<String> = Single.create {
        it.onSuccess(bitmap.toBase64String())
    }

    private fun saveSelfieDetectedImage(imageInBase64: String) {
        compositeDisposable + saveSelfieDetectedImageUseCase.execute(
            SaveSelfieDetectedImageUseCase.Param(imageInBase64)
        ).applyIoToMainSchedulerOnCompletable().subscribe()
    }

    fun getGalleryImagesContentObserver(): ContentObserver =
        object : ContentObserver(Handler(Looper.getMainLooper())) {
            override fun onChange(selfChange: Boolean) {
                refreshGalleryImageItemsList()
            }
        }

    fun refreshGalleryImageItemsList() {
        galleryImageListLiveData.value?.dataSource?.invalidate()
        refreshGalleryFragmentImagesLiveData.value = true
    }

    fun checkTrickyQuestionButtonShown() {
        compositeDisposable + checkTrickyQuestionButtonShown.execute(Unit)
            .applyIoToMainSchedulerOnSingle().subscribeToSingle({
                _showTrickyQuestionButtonLivedata.value = it.not()
            })
    }

    fun setTrickQuestionShownToTrue() {
        compositeDisposable + setTrickyQuestionShownToTrue.execute(Unit)
            .applyIoToMainSchedulerOnCompletable().subscribe()
    }

    fun setCameraScreenShownToTrue() {
        compositeDisposable + setIsCameraScreenShownFirstToTrue.execute(Unit)
            .applyIoToMainSchedulerOnCompletable().subscribe()
    }

    fun setScreenOrientationLiveData(orientation: Int) {
        _screenOrientationLiveData.value = orientation
    }

    fun updateFCMRegId() {

        val applicationContext = getApplication().applicationContext

        try {
            val pInfo =
                applicationContext.packageManager.getPackageInfo(applicationContext.packageName, 0)
            val appVersion = pInfo.versionName
            if (isNewVersion(applicationContext, appVersion) || isFcmRegIdUpdated()) {
                updateAppVersion(appVersion)
                updateRegId()
            }
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }
    }

    private fun isNewVersion(context: Context, appVersion: String?) =
        if (appVersion.isNullOrBlank()) {
            false
        } else {
            !TextUtils.equals(
                defaultPrefs(context).getString(Constants.APP_VERSION, ""),
                appVersion
            )
        }

    @SuppressLint("CheckResult")
    private fun updateRegId() {
        val applicationContext = getApplication().applicationContext

        val params: HashMap<String, Any> = HashMap()
        params[Constants.KEY_GCM_REG_ID] =
            defaultPrefs(applicationContext).getString(Constants.GCM_REG_ID, "").orDefaultValue()
        params[Constants.KEY_STUDENT_ID] = getStudentId()
        params[Constants.APP_VERSION] =
            defaultPrefs(applicationContext).getString(Constants.APP_VERSION, "").orDefaultValue()

        compositeDisposable + DataHandler.INSTANCE.studentsRepositoryv2.updateUserProfileObservable(
            params.toRequestBody()
        )
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                if (DoubtnutApp.INSTANCE.fcmRegId.isEmpty()) return@subscribe
                defaultPrefs(applicationContext).edit {
                    putBoolean(Constants.KEY_FIREBASE_REG_ID_UPDATED_ON_SERVER, true)
                }
            }, {})
    }

    private fun updateAppVersion(appVersion: String) {
        val applicationContext = getApplication().applicationContext

        defaultPrefs(applicationContext).edit {
            putString(Constants.APP_VERSION, appVersion)
        }
    }

    private fun isFcmRegIdUpdated() = defaultPrefs(getApplication()).getBoolean(
        Constants.KEY_FIREBASE_REG_ID_UPDATED_ON_SERVER,
        false
    )

    private fun onCameraScreenConfigSuccess(cameraConfig: CameraConfigEntity) {
        _cameraConfig.value = cameraConfig
    }

    fun publishDemoVideoClickEvent(cameraVersion: String) {
        if (!isForStatus) {
            cameraEventManager.onDemoVideoClick(cameraVersion)
        }
    }

    fun publishOnDemoQuestionClickEvent(title: String, cameraVersion: String) {
        if (!isForStatus) {
            cameraEventManager.onDemoQuestionClick(title, cameraVersion)
        }
    }

    fun publishOnCameraPermission(allow: Boolean, cameraVersion: String) {
        if (!isForStatus) {
            cameraEventManager.onCameraPermission(allow, cameraVersion)
        }
    }

    fun publishEventWith(
        eventName: String,
        cameraVersion: String,
        ignoreSnowplow: Boolean = false
    ) {
        if (!isForStatus) {
            cameraEventManager.eventWith(eventName, cameraVersion, ignoreSnowplow)
        }
    }

    fun publishVipClickEvent(source: String) {
        if (!isForStatus) {
            cameraEventManager.onVipPlanViewClick(source)
        }
    }

    fun sendEvent(
        event: String,
        params: HashMap<String, Any> = hashMapOf(),
        ignoreSnowplow: Boolean = false
    ) {
        if (!isForStatus) {
            cameraEventManager.sendEvent(event, params, ignoreSnowplow)
        }
    }

    fun sendAskedQuestionEvent(event: String, ignoreSnowplow: Boolean = false) {
        if (!isForStatus) {
            cameraEventManager.sendEvent(event, hashMapOf(), ignoreSnowplow)
        }
    }

    fun closeToolTipFragment() {
        _launchSampleQuestionFragment.postValue(true)
    }

    fun launchDemoAnimation(position: Int) {
        _launchDemoAnimation.postValue(Pair(true, position))
    }

    fun removeSampleQuestionFragment() {
        _removeSampleQuestionFragment.postValue(true)
    }

    fun removeDemoAnimationFragment() {
        _removeDemoAnimation.postValue(true)
    }

    private val _packageStatus = MutableLiveData<Outcome<PackageStatusEntity>>()

    val packageStatus: LiveData<Outcome<PackageStatusEntity>>
        get() = _packageStatus

    fun getPackageStatus() {
        _packageStatus.value = Outcome.loading(true)
        compositeDisposable + getPackageStatusUseCase.execute(Unit)
            .applyIoToMainSchedulerOnSingle()
            .subscribeToSingle(this::onPackageSuccess, this::onPackageError)
    }

    private fun onPackageSuccess(result: PackageStatusEntity) {
        _packageStatus.value = Outcome.loading(false)
        _packageStatus.value = Outcome.success(result)
    }

    private fun onPackageError(error: Throwable) {
        _packageStatus.value = Outcome.loading(false)
        _packageStatus.value = if (error is HttpException) {
            when (error.response()?.code()) {
                HttpURLConnection.HTTP_UNAUTHORIZED -> Outcome.BadRequest(error.message ?: "")
                HttpURLConnection.HTTP_BAD_REQUEST -> Outcome.ApiError(error)
                else -> Outcome.Failure(error)
            }
        } else {
            Outcome.Failure(error)
        }
    }

    fun getAllDirectoriesWithImages() {
        compositeDisposable + Single.fromCallable {
            val bucketList = mutableListOf<ImageBucket>()
            var totalItemCount = 0

            val uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            val projection = arrayOf(
                MediaStore.Images.ImageColumns.DATA,
                MediaStore.Images.Media.BUCKET_DISPLAY_NAME,
                MediaStore.Images.Media.BUCKET_ID,
                MediaStore.Images.Media.DATE_TAKEN
            )
            val sortOrder = "${MediaStore.Images.ImageColumns.DATE_TAKEN} DESC"

            val cursor = getApplication().contentResolver.query(
                uri,
                projection, null,
                null,
                sortOrder
            )

            cursor?.use {
                val dataPathColumn = it.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA)
                val bucketNameColumn =
                    it.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME)
                val bucketIdColumn = it.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_ID)

                val bucketItemCounts = mutableMapOf<String?, Int>().withDefault { 0 }

                while (it.moveToNext()) {
                    val dataPath = it.getString(dataPathColumn)
                    val bucketName = it.getString(bucketNameColumn)
                    val bucketId = it.getString(bucketIdColumn)
                    val bucketPath = dataPath.substringBeforeLast('/')

                    if (bucketId !in bucketItemCounts) {
                        val imageBucketItem = ImageBucket(
                            name = bucketName,
                            bucketId = bucketId,
                            bucketPath = bucketPath,
                            iconPath = dataPath,
                            itemCount = 0
                        )
                        bucketList.add(imageBucketItem)
                    }
                    bucketItemCounts[bucketId] = bucketItemCounts.getValue(bucketId) + 1
                }
                bucketList.forEach { bucket ->
                    bucket.itemCount = bucketItemCounts.getValue(bucket.bucketId)
                    totalItemCount += bucket.itemCount
                }
            }
            bucketList.apply {
                // First item is the most recent image
                val mostRecentImageDataPath = bucketList.firstOrNull()?.iconPath.orEmpty()
                sortBy { it.name }
                add(
                    0,
                    ImageBucket(
                        name = getApplication().getString(R.string.all),
                        bucketId = null,
                        bucketPath = null,
                        iconPath = mostRecentImageDataPath,
                        itemCount = totalItemCount

                    )
                )
            }
        }.applyIoToMainSchedulerOnSingle().subscribeToSingle({
            _imageDirectoryListLiveData.value = it
        })
    }

    fun checkIfAnyImagePresentInStorage() {
        compositeDisposable + Single.fromCallable {
            val uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            val projection = arrayOf(MediaStore.MediaColumns.DATA)

            val cursor = DoubtnutApp.INSTANCE.contentResolver.query(
                uri,
                projection,
                null,
                null,
                "${MediaStore.Images.ImageColumns.DATE_TAKEN} DESC LIMIT 1"
            )
            val anyImagePresent = if (cursor == null) false else cursor.count > 0
            cursor?.close()
            anyImagePresent
        }.applyIoToMainSchedulerOnSingle().subscribeToSingle({
            _isAnyImagePresentInStorageLiveData.value = it
        })
    }

    fun storeQuestionAskCoreAction() {
        viewModelScope.launch {
            userActivityRepository.storeCoreActionDone(CoreActions.QUESTION_ASK).catch { }.collect()
        }
    }

    // FIXME: 20/3/21 Unused because of flagr removal. Code will be removed later.
    fun getNavigationBottomSheetIconItems(iconCount: Int) {
        viewModelScope.launch {
            if (userPreference.getCameraScreenNavigationDataFetchedInCurrentSession()) {
                database.topOptionWidgetItemDao().getTopIcons(iconCount)
            } else {
                userPreference.setCameraScreenNavigationDataFetchedInCurrentSession(true)
                topOptionRepository.getCameraNavigationTopIcons(iconCount)
                    .map { it.data.data.items }
            }.catch {
                // Some error occurred. Fetch data again in next method call
                userPreference.setCameraScreenNavigationDataFetchedInCurrentSession(false)
            }.collect {
                _navigationBottomSheetIconItemsLiveData.value = it
            }
        }
    }

    private val _navigationBottomIconItemsLiveData =
        MutableLiveData<List<BottomNavigationItemData>>()

    val navigationBottomIconItemsLiveData: LiveData<List<BottomNavigationItemData>>
        get() = _navigationBottomIconItemsLiveData

    fun getBottomNavigationItemsList(isDoubtFeedAvailable: Boolean) {
        viewModelScope.launch {
            topOptionRepository.getCameraBottomIcons(isDoubtFeedAvailable).map { it.data }
                .catch {
                    userPreference.setCameraScreenNavigationDataFetchedInCurrentSession(false)
                }
                .collect {
                    _navigationBottomIconItemsLiveData.value = it
                }
        }
    }

    fun runTextRecognition(imageUri: Uri) {
        try {
            val image = InputImage.fromFilePath(DoubtnutApp.INSTANCE, imageUri)

            val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

            recognizer.process(image)
                .addOnSuccessListener { result ->
                    if (result.text.isNotEmpty()) {
                        val localOfflineOcr = LocalOfflineOcr(
                            System.currentTimeMillis(),
                            result.text,
                            imageUri.toString()
                        )
                        DoubtnutApp.INSTANCE.getDatabase()?.offlineOcrDao()
                            ?.insertOcr(localOfflineOcr)
                        startWorkerToShowOcrNotification()
                    }
                }
                .addOnFailureListener { e ->
                    e.printStackTrace()
                }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    /**
     * This method starts worker only if network is connected which checks in database for offline ocr
     * if it exits, create notification else stop worker.
     * It is periodic worker, runs every 2 hours.
     */
    fun startWorkerToShowOcrNotification() {
        viewModelScope.launch(Dispatchers.IO) {
            val noOfRows =
                getApplication().getDatabase()?.offlineOcrDao()?.getNoOfRows() ?: return@launch
            if (noOfRows == 0) {
                WorkManager.getInstance(getApplication())
                    .cancelUniqueWork(OcrFromImageNotificationWorker.TAG)
            } else {
                val constraints = Constraints.Builder()
                    .setRequiresBatteryNotLow(true)
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()

                val req =
                    PeriodicWorkRequestBuilder<OcrFromImageNotificationWorker>(2, TimeUnit.HOURS)
                        .setConstraints(constraints)
                        .setInitialDelay(1, TimeUnit.MINUTES)
                        .addTag(OcrFromImageNotificationWorker.TAG)
                        .setBackoffCriteria(BackoffPolicy.LINEAR, 1, TimeUnit.HOURS)
                        .build()

                WorkManager.getInstance(getApplication())
                    .enqueueUniquePeriodicWork(
                        DownloadedVideoRefresherWorker.TAG,
                        ExistingPeriodicWorkPolicy.REPLACE,
                        req
                    )
            }
        }
    }

    private fun getApplication() = DoubtnutApp.INSTANCE
}

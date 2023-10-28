package com.doubtnutapp.di.module

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.content.res.Resources
import android.os.Bundle
import android.preference.PreferenceManager
import androidx.room.Room
import com.apxor.androidsdk.core.Attributes
import com.doubtnut.analytics.Tracker
import com.doubtnut.core.analytics.IAnalyticsPublisher
import com.doubtnut.core.data.*
import com.doubtnut.core.di.qualifier.ApiRetrofit
import com.doubtnut.core.di.qualifier.MicroApiRetrofit
import com.doubtnut.core.di.qualifier.ScreenWidth
import com.doubtnut.core.entitiy.CoreAnalyticsEvent
import com.doubtnut.core.navigation.IDeeplinkAction
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnutapp.*
import com.doubtnutapp.analytics.*
import com.doubtnutapp.analytics.di.qualifier.*
import com.doubtnutapp.analytics.model.StructuredEvent
import com.doubtnutapp.data.base.di.qualifier.*
import com.doubtnutapp.data.base.manager.DownloadManagerImp
import com.doubtnutapp.data.base.manager.FileManagerImpl
import com.doubtnutapp.data.base.service.DownloadService
import com.doubtnutapp.data.common.UserPreference
import com.doubtnutapp.data.common.UserPreferenceImpl
import com.doubtnutapp.data.common.service.ThirdPartyApisService
import com.doubtnutapp.data.course.repository.CourseRepositoryImpl
import com.doubtnutapp.data.course.service.CourseService
import com.doubtnutapp.data.newlibrary.service.LibraryTabService
import com.doubtnutapp.data.remote.*
import com.doubtnutapp.data.remote.api.services.ConfigService
import com.doubtnutapp.data.remote.api.services.MicroService
import com.doubtnutapp.data.remote.api.services.NetworkService
import com.doubtnutapp.db.DoubtnutDatabase
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnutapp.dnr.service.DnrMicroService
import com.doubtnutapp.dnr.service.RedeemDnrService
import com.doubtnutapp.domain.base.manager.DownloadManager
import com.doubtnutapp.domain.base.manager.FileManager
import com.doubtnutapp.domain.course.repository.CourseRepository
import com.doubtnutapp.gamification.popactivity.popupbuilder.PopupBuilder
import com.doubtnutapp.gamification.popactivity.popupbuilder.PopupBuilderImpl
import com.doubtnutapp.gamification.popactivity.ui.viewmodel.GamificationPopupViewModel
import com.doubtnutapp.liveclass.ui.practice_english.PracticeEnglishService
import com.doubtnutapp.matchquestion.service.UploadImageService
import com.doubtnutapp.networkstats.repository.NetworkStatsRepository
import com.doubtnutapp.newlibrary.service.PreviousPapersService
import com.doubtnutapp.packageInstallerCheck.CheckForPackageInstall
import com.doubtnutapp.packageInstallerCheck.CheckForPackageInstallImpl
import com.doubtnutapp.screennavigator.Navigator
import com.doubtnutapp.screennavigator.ScreenNavigatorImpl
import com.doubtnutapp.studygroup.service.StudyGroupMicroService
import com.doubtnutapp.utils.FakeInterceptor
import com.doubtnutapp.widgetmanager.WidgetResponse
import com.doubtnutapp.widgetmanager.WidgetResponseTypeAdapter
import com.doubtnutapp.widgetmanager.WidgetTypeAdapter
import com.google.android.exoplayer2.ext.ima.ImaAdsLoader
import com.google.gson.FieldNamingPolicy
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.moe.pushlibrary.PayloadBuilder
import com.naman14.spider.SpiderInterceptor
import dagger.Module
import dagger.Provides
import io.branch.referral.util.BranchEvent
import io.reactivex.disposables.CompositeDisposable
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
object AppModule {

    @Provides
    @Singleton
    @JvmStatic
    fun providePreference(application: Application) =
        PreferenceManager.getDefaultSharedPreferences(application)

    @Provides
    @Singleton
    @JvmStatic
    fun provideQuizNotificationDataStore(@ApplicationContext appContext: Context): QuizNotificationDataStore {
        return QuizNotificationDatastoreImpl(appContext)
    }

    @Provides
    @Singleton
    @JvmStatic
    fun provideDefaultDataStore(@ApplicationContext appContext: Context): DefaultDataStore {
        return DefaultDataStoreImpl(appContext)
    }

    @Provides
    @Singleton
    @JvmStatic
    fun provideLottieAnimDataStore(@ApplicationContext appContext: Context): LottieAnimDataStore {
        return LottieAnimDatastoreImpl(appContext)
    }

    @Provides
    @Singleton
    @JvmStatic
    @ApiRetrofit
    fun provideRetroFit(
        @DefaultOkHttpClient okHttpClient: OkHttpClient,
        gson: Gson,
        networkBuilderFactory: NetworkBuilderFactory
    ): Retrofit {
        return networkBuilderFactory.retrofitBuilder(gson)
            .baseUrl(DoubtnutApp.INSTANCE.getBaseApiUrl())
            .client(okHttpClient)
            .addCallAdapterFactory(RxErrorHandlingCallAdapterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    @JvmStatic
    @ExtendedApiRetrofit
    fun provideRetroFitExtended(
        @ExtendedOkHttpClient okHttpClient: OkHttpClient,
        gson: Gson,
        networkBuilderFactory: NetworkBuilderFactory
    ): Retrofit {
        return networkBuilderFactory.retrofitBuilder(gson)
            .baseUrl(DoubtnutApp.INSTANCE.getBaseApiUrl())
            .client(okHttpClient)
            .addCallAdapterFactory(RxErrorHandlingCallAdapterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    @JvmStatic
    @MicroApiRetrofit
    fun provideMicroRetroFit(
        @DefaultOkHttpClient okHttpClient: OkHttpClient,
        gson: Gson,
        networkBuilderFactory: NetworkBuilderFactory
    ): Retrofit {
        return networkBuilderFactory.retrofitBuilder(gson)
            .baseUrl(DoubtnutApp.INSTANCE.getBaseMicroApiUrl())
            .client(okHttpClient)
            .addCallAdapterFactory(RxErrorHandlingCallAdapterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    @JvmStatic
    fun provideNetworkBuilderFactory(): NetworkBuilderFactory = NetworkBuilderFactory()

    @Provides
    @JvmStatic
    fun provideCompositeDisposable() = CompositeDisposable()

    @Provides
    @Singleton
    @JvmStatic
    fun provideDeeplinkAction(): IDeeplinkAction = DeeplinkAction()

    @Provides
    @Singleton
    @JvmStatic
    fun provideRoomDb(@ApplicationContext appContext: Context): DoubtnutDatabase =
        Room.databaseBuilder(appContext, DoubtnutDatabase::class.java, "doubtnut.db")
            .allowMainThreadQueries()
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    @Singleton
    @JvmStatic
    fun provideGson(): Gson = GsonBuilder()
        .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
        .registerTypeAdapter(WidgetResponse::class.java, WidgetResponseTypeAdapter())
        .registerTypeAdapter(WidgetEntityModel::class.java, WidgetTypeAdapter())
        .create()

    @Provides
    @Singleton
    @JvmStatic
    @ApplicationContext
    fun provideContext(application: Application): Context = application.applicationContext

    @Provides
    @Singleton
    @JvmStatic
    fun provideScreenNavigator(): Navigator = ScreenNavigatorImpl()

    @Provides
    @Singleton
    @JvmStatic
    @DefaultOkHttpClient
    fun provideOkHttpClient(
        @ApplicationContext appContext: Context,
        httpClientBuilderFactory: NetworkBuilderFactory,
        gson: Gson,
        userPreference: UserPreference
    ): OkHttpClient =
        httpClientBuilderFactory.okhttpBuilder()
            .apply {
                if (FeaturesManager.isFeatureEnabled(DoubtnutApp.INSTANCE, Features.DNS_CACHING)) {
                    dns(DoubtnutDns())
                }
            }
            .connectTimeout(20000, TimeUnit.MILLISECONDS)
            .readTimeout(20000, TimeUnit.MILLISECONDS)
            .writeTimeout(20000, TimeUnit.MILLISECONDS)
            .addInterceptor(RefreshTokenInterceptor(appContext, userPreference, gson))
            .addInterceptor(GlobalErrorHandler.instance!!)
            .addInterceptor(ApiMetaInterceptor(appContext, gson))
            .apply {
                if (BuildConfig.DEBUG) {
                    addInterceptor(FakeInterceptor(appContext))
                    addInterceptor(
                        HttpLoggingInterceptor()
                            .also { it.level = HttpLoggingInterceptor.Level.BODY }
                    )
                }
                if (BuildConfig.DEBUG || BuildConfig.ENABLE_SPIDER) {
                    addInterceptor(SpiderInterceptor.getInstance(appContext)!!)
                }
                StethoUtils.addNetworkInterceptor(this)
            }
            .build()

    @Provides
    @Singleton
    @JvmStatic
    @ExtendedOkHttpClient
    fun provideExtendedOkHttpClient(
        httpClientBuilderFactory: NetworkBuilderFactory,
        userPreference: UserPreference,
        gson: Gson
    ): OkHttpClient =
        httpClientBuilderFactory.okhttpBuilder()
            .apply {
                if (FeaturesManager.isFeatureEnabled(DoubtnutApp.INSTANCE, Features.DNS_CACHING)) {
                    dns(DoubtnutDns())
                }
            }
            .connectTimeout(60000, TimeUnit.MILLISECONDS)
            .readTimeout(60000, TimeUnit.MILLISECONDS)
            .writeTimeout(60000, TimeUnit.MILLISECONDS)
            .addInterceptor(RefreshTokenInterceptor(DoubtnutApp.INSTANCE, userPreference, gson))
            .addInterceptor(GlobalErrorHandler.instance!!)
            .build()

    @Provides
    @JvmStatic
    @AppInternalDirPath
    fun provideAppInternalDirPath(@ApplicationContext appContext: Context) =
        appContext.getExternalFilesDir(null)?.path
            ?: ""

    @Provides
    @JvmStatic
    @ApplicationCachePath
    fun provideAppCachePath(@ApplicationContext appContext: Context) =
        appContext.cacheDir.path ?: ""

    @Provides
    @JvmStatic
    @CropDefaultImagePath
    fun provideDefaultCropImagePath() =
        "android.resource://com.doubtnutapp/drawable/default_sample_image"

    @Provides
    @JvmStatic
    @IconDirName
    fun provideTopIconInternalDirName() = "TopIcons"

    @Provides
    @JvmStatic
    @AppVersion
    fun provideAppVersion() = BuildConfig.VERSION_NAME

    @Provides
    @JvmStatic
    @AppVersionCode
    fun provideAppVersionCode() = BuildConfig.VERSION_CODE

    @Provides
    @JvmStatic
    @ResourceType
    fun provideResourceType() = "question_ask"

    @Provides
    @JvmStatic
    fun provideDownloadService(@ApiRetrofit retrofit: Retrofit): DownloadService =
        retrofit.create(DownloadService::class.java)

    @Provides
    @JvmStatic
    @Singleton
    fun provideDownloadManager(
        downloadService: DownloadService,
        fileManager: FileManager,
        @AppInternalDirPath internalDirPath: String,
        @IconDirName iconDirName: String,
        @ApplicationCachePath appCachePath: String
    ): DownloadManager =
        DownloadManagerImp(downloadService, fileManager, internalDirPath, appCachePath, iconDirName)

    @Provides
    @JvmStatic
    @Singleton
    fun provideFileManager(): FileManager = FileManagerImpl()

    @Provides
    @JvmStatic
    @Singleton
    fun provideUserPreference(
        sharedPreferences: SharedPreferences,
        defaultDataStore: DefaultDataStore
    ): UserPreference =
        UserPreferenceImpl(sharedPreferences, defaultDataStore)

    @Provides
    @JvmStatic
    @Singleton
    fun providePopupManager(userPreference: UserPreference, popupBuilder: PopupBuilder) =
        GamificationPopupViewModel(userPreference, popupBuilder)

    @Provides
    @JvmStatic
    @Singleton
    fun provideCheckForPackageInstall(@ApplicationContext appContext: Context): CheckForPackageInstall =
        CheckForPackageInstallImpl(appContext)

    @Provides
    @JvmStatic
    @Singleton
    fun providePopupBuilder(
        @ApplicationContext appContext: Context,
        @ScreenWidth screenWidth: Int,
        gson: Gson
    ): PopupBuilder = PopupBuilderImpl(appContext, screenWidth, gson)

    @Provides
    @JvmStatic
    @Singleton
    @ScreenWidth
    fun provideScreenWidth(): Int = Resources.getSystem().displayMetrics.widthPixels

    @Provides
    @JvmStatic
    @Singleton
    fun provideEventTracker(): Tracker = Tracker()

    @Singleton
    @Provides
    @FirebaseTrackerInfo
    @JvmStatic
    fun provideFirebaseTracker(
        @ApplicationContext appContext: Context,
        userPreference: UserPreference
    ): AnalyticsTracker<Pair<String, Bundle>> = FirebaseTracker(appContext, userPreference)

    @Singleton
    @Provides
    @FacebookTrackerInfo
    @JvmStatic
    fun provideFacebookTracker(
        @ApplicationContext appContext: Context,
        userPreference: UserPreference
    ): AnalyticsTracker<Pair<String, Bundle>> = FacebookTracker(appContext, userPreference)

    @Singleton
    @Provides
    @SnowPlowTrackerInfo
    @JvmStatic
    fun provideSnowPlowTracker(
        @ApplicationContext appContext: Context,
        userPreference: UserPreference,
        @AppVersionCode appVersionCode: Int,
        @AppVersion appVersionName: String
    ): AnalyticsTracker<StructuredEvent> =
        SnowPlowTracker(appContext, userPreference, appVersionCode, appVersionName)

    @Singleton
    @Provides
    @BranchIoTrackerInfo
    @JvmStatic
    fun provideBranchIoTracker(
        @ApplicationContext appContext: Context,
        userPreference: UserPreference,
        @Udid udid: String
    ): AnalyticsTracker<Pair<String, BranchEvent>> =
        BranchIoTracker(appContext, userPreference, udid)

    @Singleton
    @Provides
    @ApxorTrackerInfo
    @JvmStatic
    fun provideApxorTracker(
        @ApplicationContext appContext: Context,
        userPreference: UserPreference
    ): AnalyticsTracker<Pair<String, Attributes>> = ApxorTracker(appContext, userPreference)

    @Singleton
    @Provides
    @CleverTapTrackerInfo
    @JvmStatic
    fun provideCleverTapTracker(
        @ApplicationContext appContext: Context
    ): AnalyticsTracker<CoreAnalyticsEvent> = CleverTapTracker(appContext)

    @Singleton
    @Provides
    @MoEngageTrackerInfo
    @JvmStatic
    fun provideMoEngageTracker(
        @ApplicationContext appContext: Context,
        userPreference: UserPreference
    ): AnalyticsTracker<Pair<String, PayloadBuilder>> =
        MoEngageTracker(appContext, userPreference)

   /* @Singleton
    @Provides
    @ConvivaTrackerInfo
    @JvmStatic
    fun provideConvivaTracker(
        @ApplicationContext appContext: Context,
        userPreference: UserPreference,
        trackerController: TrackerController,
        gson: Gson
    ): AnalyticsTracker<ConvivaEvent> =
        ConvivaTracker(appContext, userPreference, trackerController, gson)*/

    @Provides
    @Singleton
    @JvmStatic
    fun provideAnalyticsTracker(
        @FirebaseTrackerInfo firebaseTracker: AnalyticsTracker<Pair<String, Bundle>>,
        @ApxorTrackerInfo apxorTracker: AnalyticsTracker<Pair<String, Attributes>>,
        @FacebookTrackerInfo facebookTracker: AnalyticsTracker<Pair<String, Bundle>>,
        @SnowPlowTrackerInfo snowPlowTracker: AnalyticsTracker<StructuredEvent>,
        @BranchIoTrackerInfo branchIoTracker: AnalyticsTracker<Pair<String, BranchEvent>>,
        @MoEngageTrackerInfo moEngageTracker: AnalyticsTracker<Pair<String, PayloadBuilder>>,
        /*@ConvivaTrackerInfo convivaTracker: AnalyticsTracker<ConvivaEvent>*/
    ): IAnalyticsPublisher {
        return AnalyticsPublisher(
            firebaseTracker = firebaseTracker,
            apxorTracker = apxorTracker,
            facebookTracker = facebookTracker,
            snowPlowTracker = snowPlowTracker,
            branchIoTracker = branchIoTracker,
            moEngageTracker = moEngageTracker,
            /*convivaTracker = convivaTracker*/
        )
    }

    @Singleton
    @Provides
    @Udid
    @JvmStatic
    fun provideUdid(@ApplicationContext appContext: Context): String =
        android.provider.Settings.Secure.getString(
            appContext.contentResolver,
            android.provider.Settings.Secure.ANDROID_ID
        )

    @Provides
    @JvmStatic
    fun provideUploadImageService(): UploadImageService = Retrofit.Builder()
        .baseUrl(DoubtnutApp.INSTANCE.getBaseApiUrl())
        .client(
            OkHttpClient.Builder()
                .apply {
                    if (FeaturesManager.isFeatureEnabled(
                            DoubtnutApp.INSTANCE,
                            Features.DNS_CACHING
                        )
                    ) {
                        dns(DoubtnutDns())
                    }
                }
                .connectTimeout(60000, TimeUnit.MILLISECONDS)
                .readTimeout(60000, TimeUnit.MILLISECONDS)
                .writeTimeout(60000, TimeUnit.MILLISECONDS)
                .apply {
                    if (BuildConfig.DEBUG || BuildConfig.ENABLE_SPIDER) {
                        addInterceptor(
                            HttpLoggingInterceptor()
                                .also { it.level = HttpLoggingInterceptor.Level.BODY }
                        )
                        addInterceptor(SpiderInterceptor.getInstance(DoubtnutApp.INSTANCE.applicationContext)!!)
                    }
                    StethoUtils.addNetworkInterceptor(this)
                }
                .build()
        )
        .addConverterFactory(GsonConverterFactory.create())
        .addCallAdapterFactory(RxErrorHandlingCallAdapterFactory.create())
        .build().create(UploadImageService::class.java)

    @Provides
    @JvmStatic
    fun provideThirdPartiesApiService(): ThirdPartyApisService = Retrofit.Builder()
        .baseUrl(DoubtnutApp.INSTANCE.getBaseApiUrl())
        .client(
            OkHttpClient.Builder()
                .apply {
                    if (FeaturesManager.isFeatureEnabled(
                            DoubtnutApp.INSTANCE,
                            Features.DNS_CACHING
                        )
                    ) {
                        dns(DoubtnutDns())
                    }
                }
                .connectTimeout(60000, TimeUnit.MILLISECONDS)
                .readTimeout(60000, TimeUnit.MILLISECONDS)
                .writeTimeout(60000, TimeUnit.MILLISECONDS)
                .addInterceptor(GlobalErrorHandler.instance!!)
                .apply {
                    if (BuildConfig.DEBUG || BuildConfig.ENABLE_SPIDER) {
                        addInterceptor(
                            HttpLoggingInterceptor()
                                .also { it.level = HttpLoggingInterceptor.Level.BODY }
                        )
                        addInterceptor(SpiderInterceptor.getInstance(DoubtnutApp.INSTANCE.applicationContext)!!)
                    }
                    StethoUtils.addNetworkInterceptor(this)
                }
                .build()
        )
        .addConverterFactory(GsonConverterFactory.create())
        .addCallAdapterFactory(RxErrorHandlingCallAdapterFactory.create())
        .build()
        .create(ThirdPartyApisService::class.java)

    @Provides
    @JvmStatic
    fun provideRedeemDnrApiService(
        @ApplicationContext appContext: Context,
        userPreference: UserPreference,
        gson: Gson
    ): RedeemDnrService {
        val defaultTimeout = 40000L
        val timeout: Long = when (
            FeaturesManager.isFeatureEnabled(
                appContext,
                Features.REDEEM_DNR_TIME_OUT
            )
        ) {
            true -> {
                val payload = FeaturesManager.getFeaturePayload(
                    appContext,
                    Features.REDEEM_DNR_TIME_OUT
                )
                payload?.let {
                    (it["timeout"] as? Double)?.toLong() ?: defaultTimeout
                } ?: defaultTimeout
            }
            else -> {
                defaultTimeout
            }
        }
        return Retrofit.Builder()
            .baseUrl(DoubtnutApp.INSTANCE.getBaseMicroApiUrl())
            .client(
                OkHttpClient.Builder()
                    .apply {
                        if (FeaturesManager.isFeatureEnabled(
                                appContext,
                                Features.DNS_CACHING
                            )
                        ) {
                            dns(DoubtnutDns())
                        }
                    }
                    .connectTimeout(timeout, TimeUnit.MILLISECONDS)
                    .readTimeout(timeout, TimeUnit.MILLISECONDS)
                    .writeTimeout(timeout, TimeUnit.MILLISECONDS)
                    .addInterceptor(RefreshTokenInterceptor(appContext, userPreference, gson))
                    .addInterceptor(GlobalErrorHandler.instance!!)
                    .apply {
                        if (BuildConfig.DEBUG || BuildConfig.ENABLE_SPIDER) {
                            addInterceptor(
                                HttpLoggingInterceptor()
                                    .also { it.level = HttpLoggingInterceptor.Level.BODY }
                            )
                            addInterceptor(SpiderInterceptor.getInstance(appContext)!!)
                        }
                        StethoUtils.addNetworkInterceptor(this)
                    }
                    .build()
            )
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(RxErrorHandlingCallAdapterFactory.create())
            .build()
            .create(RedeemDnrService::class.java)
    }

    @Provides
    @JvmStatic
    @Singleton
    fun provideNetworkService(@ApiRetrofit retrofit: Retrofit): NetworkService =
        retrofit.create(NetworkService::class.java)

    @Provides
    @JvmStatic
    @Singleton
    fun provideMicroService(@MicroApiRetrofit retrofit: Retrofit): MicroService =
        retrofit.create(MicroService::class.java)

    @Provides
    @JvmStatic
    @Singleton
    fun provideStudyGroupMicroService(@MicroApiRetrofit retrofit: Retrofit): StudyGroupMicroService =
        retrofit.create(StudyGroupMicroService::class.java)

    @Provides
    @JvmStatic
    @Singleton
    fun provideDnrMicroService(@MicroApiRetrofit retrofit: Retrofit): DnrMicroService =
        retrofit.create(DnrMicroService::class.java)

    @Provides
    @JvmStatic
    @Singleton
    fun provideLibraryTabService(@ApiRetrofit retrofit: Retrofit): LibraryTabService =
        retrofit.create(LibraryTabService::class.java)

    @Provides
    @JvmStatic
    @Singleton
    fun provideConfigService(@ApiRetrofit retrofit: Retrofit): ConfigService =
        retrofit.create(ConfigService::class.java)

    /*@Provides
    @Singleton
    @JvmStatic
    fun provideConvivaVideoAnalytics(@ApplicationContext appContext: Context): ConvivaVideoAnalytics {
        ConvivaHelper.init(appContext)
        return ConvivaAnalytics.buildVideoAnalytics(appContext).apply {
            setContentInfo(ConvivaHelper.getUserProperties())
        }
    }*/

/*    @Provides
    @Singleton
    @JvmStatic
    fun provideConvivaAppAnalytics(@ApplicationContext appContext: Context): TrackerController {
        return ConvivaHelper.initTracker(appContext)
    }*/

    @Provides
    @JvmStatic
    @Singleton
    fun providePreviousPapersService(@ApiRetrofit retrofit: Retrofit): PreviousPapersService =
        retrofit.create(PreviousPapersService::class.java)

    @Provides
    @JvmStatic
    @Singleton
    fun provideCourseService(@ApiRetrofit retrofit: Retrofit): CourseService =
        retrofit.create(CourseService::class.java)

    @Provides
    @JvmStatic
    @Singleton
    fun provideCourseRepository(
        service: CourseService
    ): CourseRepository {
        return CourseRepositoryImpl(service)
    }

    @Provides
    @JvmStatic
    @Singleton
    fun providesPracticeEnglishService(@ApiRetrofit retrofit: Retrofit): PracticeEnglishService {
        return retrofit.create(PracticeEnglishService::class.java)
    }

    @Provides
    @JvmStatic
    @Singleton
    fun provideNetworkStatsRepository(): NetworkStatsRepository = NetworkStatsRepository()

    @Provides
    @Singleton
    @JvmStatic
    fun providesBottomNavIconsNotificationsDataStore(@ApplicationContext appContext: Context): BottomNavIconsNotificationDataStore {
        return BottomNavIconsNotificationsDataStoreImpl(appContext)
    }

    @Provides
    @Singleton
    @JvmStatic
    fun providesImaAdsLoader(@ApplicationContext appContext: Context): ImaAdsLoader.Builder {
        return ImaAdsLoader.Builder(appContext)
    }
}

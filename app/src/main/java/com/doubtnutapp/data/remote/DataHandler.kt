package com.doubtnutapp.data.remote

import com.doubtnut.core.data.DefaultDataStore
import com.doubtnut.core.di.qualifier.ApiRetrofit
import com.doubtnut.core.di.qualifier.MicroApiRetrofit
import com.doubtnutapp.DoubtnutApp
import com.doubtnutapp.data.base.di.qualifier.ExtendedApiRetrofit
import com.doubtnutapp.data.homefeed.StudyDostApiService
import com.doubtnutapp.data.remote.api.services.*
import com.doubtnutapp.data.remote.repository.*
import com.doubtnutapp.data.similarVideo.service.NcertSimilarVideoService
import com.doubtnutapp.db.DoubtnutDatabase
import com.doubtnutapp.dnr.service.DnrMicroService
import com.doubtnutapp.dnr.service.RedeemDnrService
import com.doubtnutapp.doubtpecharcha.service.DoubtPeCharchaApiService
import com.doubtnutapp.liveclass.viewmodel.ReferralRepository
import com.doubtnutapp.liveclass.viewmodel.ReferralService
import com.doubtnutapp.matchquestion.service.MatchQuestionService
import com.doubtnutapp.studygroup.service.DnrRepository
import com.doubtnutapp.ui.likeuserlist.LikedUserRepository
import com.doubtnutapp.ui.likeuserlist.LikedUserService
import dagger.Lazy
import retrofit2.Retrofit
import javax.inject.Inject

class DataHandler private constructor() {

    companion object Singleton {
        val INSTANCE: DataHandler by lazy { DataHandler() }
    }

    @ApiRetrofit
    @Inject
    lateinit var retrofit: Lazy<Retrofit>

    @MicroApiRetrofit
    @Inject
    lateinit var retrofitMicro:Lazy<Retrofit>

    @ExtendedApiRetrofit
    @Inject
    lateinit var retrofitWithExtendedTimeOut: Lazy<Retrofit>

    @Inject
    lateinit var microService: Lazy<MicroService>

    @Inject
    lateinit var networkService: Lazy<NetworkService>

    @Inject
    lateinit var configService: Lazy<ConfigService>

    @Inject
    lateinit var doubtnutDatabase: Lazy<DoubtnutDatabase>

    @Inject
    lateinit var defaultDataStore: Lazy<DefaultDataStore>

    val questionsRepository: QuestionsRepository by lazy { QuestionsRepository(networkService.get()) }
    val studentsRepository: StudentsRepository by lazy {
        StudentsRepository(
            retrofit.get().create(
                StudentsService::class.java
            )
        )
    }
    val studentsRepositoryv2: StudentsRepository by lazy {
        StudentsRepository(
            retrofit.get().create(
                StudentsService::class.java
            )
        )
    }
    val phoneVerificationRepository: PhoneVerificationRepository by lazy {
        PhoneVerificationRepository(
            retrofit.get().create(PhoneVerificationService::class.java)
        )
    }
    val languageRepository: LanguageRepository by lazy {
        LanguageRepository(
            retrofit.get().create(
                LanguageService::class.java
            )
        )
    }
    val classRepository: ClassRepository by lazy {
        ClassRepository(
            retrofit.get().create(ClassService::class.java)
        )
    }
    val courseRepository: CourseRepository by lazy {
        CourseRepository(
            retrofit.get().create(CourseService::class.java),
            microService.get()
        )
    }
    val chatRepository: LiveClassChatRepository by lazy { LiveClassChatRepository(microService.get()) }
    val notificationCenterRepository: NotificationCenterRepository by lazy {
        NotificationCenterRepository(
            microService.get()
        )
    }
    val libraryRepository: LibraryRepository by lazy {
        LibraryRepository(
            retrofit.get().create(
                LibraryService::class.java
            )
        )
    }
    val playlistRepository: PlaylistRepository by lazy {
        PlaylistRepository(
            retrofit.get().create(
                PlaylistService::class.java
            )
        )
    }
    val settingsRepository: SettingsRepository by lazy {
        SettingsRepository(
            retrofit.get().create(
                SettingsService::class.java
            )
        )
    }
    val notificationRepository: NotificationRepository by lazy {
        NotificationRepository(
            retrofit.get().create(
                NotificationService::class.java
            )
        )
    }
    val feedbackRepository: FeedbackRepository by lazy {
        FeedbackRepository(
            retrofit.get().create(
                FeedbackService::class.java
            )
        )
    }
    val dailyPrizeRepository: DailyPrizeRepository by lazy {
        DailyPrizeRepository(
            retrofit.get().create(
                DailyPrizeService::class.java
            )
        )
    }
    val commentRepository: CommentsRepository by lazy {
        CommentsRepository(
            retrofit.get().create(
                CommentService::class.java
            )
        )
    }
    val likedUserRepository: LikedUserRepository by lazy {
        LikedUserRepository(
            retrofit.get().create(
                LikedUserService::class.java
            )
        )
    }
    val pdfRepository: PdfRepository by lazy {
        PdfRepository(
            retrofit.get().create(PdfService::class.java),
            microService.get()
        )
    }
    val testRepository: TestRepository by lazy {
        TestRepository(
            retrofit.get().create(TestService::class.java)
        )
    }
    val teslaRepository: TeslaRepository by lazy {
        TeslaRepository(
            retrofit.get().create(TeslaService::class.java)
        )
    }
    val teacherChannelRepository: TeacherChannelRepository by lazy {
        TeacherChannelRepository(
            retrofit.get().create(TeacherChannelService::class.java)
        )
    }
    val appConfigRepository: AppConfigRepository by lazy {
        AppConfigRepository(
            microService.get(),
            configService.get()
        )
    }
    val matchesByFileRepository: MatchesByFileRepository by lazy {
        MatchesByFileRepository(
            retrofitWithExtendedTimeOut.get().create(MatchQuestionService::class.java),
            doubtnutDatabase.get(),
            defaultDataStore.get()
        )
    }
    val searchRepository: SearchRepository by lazy {
        SearchRepository(
            retrofit.get().create(SearchService::class.java)
        )
    }
    val questionAskedHistoryRepository: QuestionAskedHistoryRepository by lazy {
        QuestionAskedHistoryRepository(
            retrofit.get().create(QuestionAskedHistoryService::class.java)
        )
    }
    val socialRepository: SocialRepository by lazy {
        SocialRepository(
            retrofit.get().create(SocialService::class.java)
        )
    }
    val videoDownloadRepository: VideoDownloadRepository by lazy {
        VideoDownloadRepository(
            microService.get()
        )
    }
    val userStatusRepository: UserStatusRepository by lazy { UserStatusRepository(microService.get()) }
    val apbRepository: ApbRepository by lazy { ApbRepository(microService.get()) }
    val referralRepository: ReferralRepository by lazy {
        ReferralRepository(
            retrofit.get().create(
                ReferralService::class.java
            )
        )
    }

    val ncertSimilarVideoRepository: NcertSimilarVideoRepository by lazy {
        NcertSimilarVideoRepository(
            retrofit.get().create(NcertSimilarVideoService::class.java)
        )
    }

    val studyDostRepository: StudyDostRepository by lazy {
        StudyDostRepository(
            retrofit.get().create(
                StudyDostApiService::class.java
            )
        )
    }

    val doubtPeCharchaRepository: DoubtPeCharchaRepository by lazy {
        DoubtPeCharchaRepository(
            retrofit.get().create(DoubtPeCharchaApiService::class.java)
        )
    }

    val whatsAppAdminRepository: WhatsappAdminRepository by lazy {
        WhatsappAdminRepository(
            retrofit.get().create(
                WhatsAppAdminService::class.java
            )
        )
    }

    val exploreMoreWidgetRepository: ExploreMoreWidgetRepository by lazy {
        ExploreMoreWidgetRepository(
            retrofit.get().create(
                ExploreMoreWidgetService::class.java
            )
        )
    }

    val dnrRepository : DnrRepository by lazy {
        DnrRepository(retrofitMicro.get().create(DnrMicroService::class.java),
        retrofitMicro.get().create(RedeemDnrService::class.java))
    }

    init {
        DoubtnutApp.INSTANCE.daggerAppComponent?.inject(this)
    }
}

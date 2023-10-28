package com.doubtnutapp.home.mapper

import com.doubtnutapp.base.*
import com.doubtnutapp.data.base.Mapper
import com.doubtnutapp.domain.base.SolutionResourceType.SOLUTION_RESOURCE_TYPE_VIDEO
import com.doubtnutapp.screennavigator.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ActionToScreenMapper @Inject constructor() : Mapper<Any, Screen> {

    @Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE")
    override fun map(action: Any) = when (action) {
        is OpenNCERTChapter -> NcertChapterScreen
        is OpenPDFPage -> PDFScreen
        is OpenContestPage -> ContestDetailScreen
        is OpenTopicPage -> if (action.isLast == 0) LibraryPlayListScreen else LibraryVideoPlayListScreen
        is OpenQuizDetail -> QuizDetailScreen
        is PlayVideo -> if (action.resourceType == SOLUTION_RESOURCE_TYPE_VIDEO) VideoScreen else TextSolutionScreen
        is OpenLearnMoreVideo -> LearnMoreScreen
        is OpenWhatsapp -> ExternalUrlScreen
        is OpenWebView -> WebViewScreen
        is LaunchGame -> DnGamesScreen
        else -> NoScreen
    }


}
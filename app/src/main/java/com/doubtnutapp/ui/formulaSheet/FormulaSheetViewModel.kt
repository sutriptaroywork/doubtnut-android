package com.doubtnutapp.ui.formulaSheet

import com.doubtnutapp.base.BaseViewModel
import com.doubtnutapp.data.remote.RetrofitLiveData
import com.doubtnutapp.data.remote.models.*
import com.doubtnutapp.data.remote.repository.FormulaSheetRepository
import com.doubtnutapp.ui.formulaSheet.event.FormulaSheetEventManager
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import javax.inject.Inject


class FormulaSheetViewModel @Inject constructor(compositeDisposable: CompositeDisposable,
                                                private val formulaSheetEventManager: FormulaSheetEventManager,
                                                private val formulaSheetRepository: FormulaSheetRepository)
    : BaseViewModel(compositeDisposable) {

    fun getFormulaHome(): RetrofitLiveData<ApiResponse<ArrayList<FormulaSheetSubjects>>> {
        return formulaSheetRepository.getFormulaHome()
    }

    fun getFormulasTopics(subjectId: String): RetrofitLiveData<ApiResponse<ArrayList<FormulaSheetSuperChapter>>> {
        return formulaSheetRepository.getFormulasTopics(subjectId)
    }

    fun getFormulas(chapterId: String): RetrofitLiveData<ApiResponse<ArrayList<FormulaSheetFormulas>>> {
        return formulaSheetRepository.getFormulas(chapterId)
    }

    fun getFormulasBySearch(chapterId: String, searchType: String): RetrofitLiveData<ApiResponse<ArrayList<FormulaSheetFormulas.FormulasList>>> {
        return formulaSheetRepository.getFormulasBySearch(chapterId, searchType)
    }

    fun getFormulasTopicsSearch(subjectId: String, searchText: String): Observable<ApiResponse<ArrayList<FormulaSheetSuperChapter>>> {
        return formulaSheetRepository.getFormulasTopicsSearch(subjectId, searchText)
    }


    fun getGlobalSearch(searchText: String): Observable<ApiResponse<ArrayList<FormulaSheetGlobalSearch>>> {
        return formulaSheetRepository.getGlobalSearch(searchText)
    }

    fun getFormulasSearch(chapterId: String, searchText: String): Observable<ApiResponse<ArrayList<FormulaSheetFormulas>>> {
        return formulaSheetRepository.getFormulasSearch(chapterId, searchText)
    }

    fun getCheatSheet(): RetrofitLiveData<ApiResponse<ArrayList<CheatSheetData>>> {
        return formulaSheetRepository.getCheatSheet()
    }

    fun getFormulasByCheatSheet(cheatSheetId: String): RetrofitLiveData<ApiResponse<ArrayList<FormulaSheetFormulas.FormulasList>>> {
        return formulaSheetRepository.getFormulasByCheatSheet(cheatSheetId)
    }

    fun publishOnFormulaSubjectSelectedEvent(subject: String) {
        formulaSheetEventManager.onFormulaSubjectSelected(subject)
    }

    fun eventWith(eventName: String, ignoreSnowplow: Boolean = false) {
        formulaSheetEventManager.eventWith(eventName, ignoreSnowplow = ignoreSnowplow)
    }

    fun publishOnFormulaTopicSelectedEvent(topic: String) {
        formulaSheetEventManager.onFormulaTopicSelected(topic)
    }

    fun publishOnAddToCheatSheetEvent(cheatSheetName: String, type: String) {
        formulaSheetEventManager.onAddToCheatSheetClick(cheatSheetName, type)
    }

    fun publishOnAddToCheatSheetSuccessFullEvent() {
        formulaSheetEventManager.onAddToCheatSheetSuccessFull()
    }

}

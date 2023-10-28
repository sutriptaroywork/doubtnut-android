package com.doubtnutapp.data.remote.repository

import com.doubtnutapp.data.remote.RetrofitLiveData
import com.doubtnutapp.data.remote.api.services.NetworkService
import com.doubtnutapp.data.remote.models.*
import io.reactivex.Observable
import okhttp3.RequestBody
import javax.inject.Inject

class FormulaSheetRepository @Inject constructor(private val networkService: NetworkService) {

    fun getFormulaHome(): RetrofitLiveData<ApiResponse<ArrayList<FormulaSheetSubjects>>> = networkService.getFormulaHome()

    fun getFormulasTopics(subjectId: String): RetrofitLiveData<ApiResponse<ArrayList<FormulaSheetSuperChapter>>> = networkService.getTopics(subjectId)

    fun getFormulas(chapterId: String): RetrofitLiveData<ApiResponse<ArrayList<FormulaSheetFormulas>>> = networkService.getFormulas(chapterId)

    fun getFormulasTopicsSearch(subjectId: String, searchText: String): Observable<ApiResponse<ArrayList<FormulaSheetSuperChapter>>> = networkService.getTopicsSearch(subjectId, searchText)

    fun getFormulasSearch(chapterId: String, searchText: String): Observable<ApiResponse<ArrayList<FormulaSheetFormulas>>> = networkService.getFormulasSearch(chapterId, searchText)

    fun getGlobalSearch(searchText: String): Observable<ApiResponse<ArrayList<FormulaSheetGlobalSearch>>> = networkService.getGlobalSearch(searchText)

    fun getFormulasBySearch(chapterId: String, searchType: String): RetrofitLiveData<ApiResponse<ArrayList<FormulaSheetFormulas.FormulasList>>> = networkService.getFormulasBySearch(searchType, chapterId)

    fun getCheatSheet(): RetrofitLiveData<ApiResponse<ArrayList<CheatSheetData>>> = networkService.getCheatSheet()

    fun addCheatsheet(requestBody: RequestBody): RetrofitLiveData<ApiResponse<Any>> = networkService.addCheatsheet(requestBody)

    fun getFormulasByCheatSheet(cheatSheetId: String): RetrofitLiveData<ApiResponse<ArrayList<FormulaSheetFormulas.FormulasList>>> = networkService.getFormulasByCheatSheet(cheatSheetId)
}

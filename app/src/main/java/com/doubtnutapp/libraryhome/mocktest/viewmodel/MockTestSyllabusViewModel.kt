package com.doubtnutapp.libraryhome.mocktest.viewmodel

import androidx.lifecycle.MutableLiveData
import com.doubtnutapp.base.BaseViewModel
import com.doubtnutapp.data.remote.models.MockTestSyllabusData
import io.reactivex.disposables.CompositeDisposable
import javax.inject.Inject

class MockTestSyllabusViewModel @Inject constructor(compositeDisposable: CompositeDisposable) :
    BaseViewModel(compositeDisposable) {

    companion object {
        private const val LINE_SEPARATOR = "###"
        private const val MARKS_SEPARATOR = "#$#"
        private const val TOPIC_SEPARATOR = "#!#"
    }

    private val syllabusLiveData: MutableLiveData<MockTestSyllabusData> = MutableLiveData()

    fun getSyllabusLiveData(description: String): MutableLiveData<MockTestSyllabusData> {
        parseDescription(description)
        return syllabusLiveData
    }
    /**
     * Uses input as "###Physics#$#50#!#Topic1#!#Topic2#!#Topic3
     * ###Chemistry#$#30#!#CHEM01#!#CHEM02#!#CHEM03#!#CHEM04###Maths#$#50#!#MAT01#!#MAT02#!#MAT03"
     * to give output as a list of marks,subjects and a map of (position,topicList)
     *
     */

    private fun parseDescription(description: String) {

        val subjectList: ArrayList<String> = ArrayList()
        val topicList: ArrayList<String> = ArrayList()
        val marksList: ArrayList<String> = ArrayList()
        val map = HashMap<Int, List<String>>()
        if (description.startsWith(LINE_SEPARATOR)) {
            if (description.startsWith(LINE_SEPARATOR)) {
                val substr = description.substring(3, description.length)
                val list = substr.split(LINE_SEPARATOR)
                for (k in list.indices) {
                    subjectList.add(list[k].split(MARKS_SEPARATOR)[0])
                    val tempList2 = list[k].split(MARKS_SEPARATOR)[1].split(TOPIC_SEPARATOR)
                    map[k] = tempList2
                    marksList.add(list[k].split(MARKS_SEPARATOR)[1].split(TOPIC_SEPARATOR)[0])
                    val tempList = list[k].split(MARKS_SEPARATOR)[1].split(TOPIC_SEPARATOR)
                    for (i in tempList.indices) {
                        if (i != 0) {
                            topicList.add(tempList[i])
                        }
                    }
                }
            }
        }
        val syllabusData = MockTestSyllabusData(subjectList, marksList, map)
        syllabusLiveData.postValue(syllabusData)
    }

}
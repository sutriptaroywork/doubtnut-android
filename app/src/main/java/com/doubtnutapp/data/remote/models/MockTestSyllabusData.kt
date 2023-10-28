package com.doubtnutapp.data.remote.models

data class MockTestSyllabusData(
    val subjectList: ArrayList<String>,
    val marksList: ArrayList<String>,
    val map: HashMap<Int, List<String>>
)

data class StartTestData(
    val msg: String?
)

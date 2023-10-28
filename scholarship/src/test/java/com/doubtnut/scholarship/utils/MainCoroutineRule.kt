package com.doubtnut.scholarship.utils

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.rules.TestWatcher
import org.junit.runner.Description

// this class is not currently being used as AndroidX's AndroidJUnit4 runner is also
// used in local tests which provides the android components such as Main Looper

// To use this class remove the annotation @RunWith(AndroidJUnit4::class) from ScholarshipActivityVMTest
// and add
// @get:Rule
// var mainCoroutineRule = MainCoroutineRule()

/**
 * ViewModelScope which uses the Main Dispatcher has been used inside many methods,
 * which in turn uses Main Looper and this available only for real app environments
 * so we can use this class whenever the above said case arises
 */
@ExperimentalCoroutinesApi
class MainCoroutineRule(
    private val dispatcher: TestDispatcher = UnconfinedTestDispatcher()
) : TestWatcher() {

    override fun starting(description: Description) {
        super.starting(description)
        Dispatchers.setMain(dispatcher)
    }

    override fun finished(description: Description) {
        super.finished(description)
        Dispatchers.resetMain()
    }
}

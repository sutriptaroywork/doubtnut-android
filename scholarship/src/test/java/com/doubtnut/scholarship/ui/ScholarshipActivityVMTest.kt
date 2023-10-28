package com.doubtnut.scholarship.ui

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.doubtnut.scholarship.utils.MainCoroutineRule
import com.doubtnut.core.data.remote.Outcome
import com.doubtnut.scholarship.utils.FakeScholarshipRepository
import com.doubtnut.scholarship.data.entity.ScholarshipData
import com.doubtnut.scholarship.utils.TestData
import com.google.common.truth.Truth.assertThat
import com.doubtnut.scholarship.utils.getOrAwaitValueTest
import io.reactivex.disposables.CompositeDisposable
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.robolectric.annotation.Config

/**
 * Class for testing ScholarshipActivityVM.kt
 */

@ExperimentalCoroutinesApi
@Config(manifest = Config.NONE)
class ScholarshipActivityVMTest {

    /**
     * makes sure that all the background tasks related to
     * architectural components happen in the same thread important if observing live data
     */
    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    // to set the Dispatcher as Dispatcher.setMain in the viewModelScope
    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    private lateinit var viewModel: ScholarshipActivityVM

    private val compositeDisposable by lazy { CompositeDisposable() }
    private val fakeScholarshipRepository = FakeScholarshipRepository()

    @Before
    fun createViewModel() {
        viewModel = ScholarshipActivityVM(compositeDisposable, fakeScholarshipRepository)
    }

    @Test
    fun getScholarshipData_returnScholarshipLiveData() = runTest(UnconfinedTestDispatcher()) {
        viewModel.getScholarshipData("1", false)
        val value = viewModel.widgetsLiveData.getOrAwaitValueTest()
        assertThat(value).isEqualTo(Outcome.success(TestData.scholarshipData))
    }
}

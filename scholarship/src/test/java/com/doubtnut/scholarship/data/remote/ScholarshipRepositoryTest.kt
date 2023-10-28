package com.doubtnut.scholarship.data.remote

import com.doubtnut.scholarship.utils.FakeScholarshipService
import com.doubtnut.scholarship.utils.TestData
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

/**
 * Class for testing ScholarshipRepository.kt
 */
@ExperimentalCoroutinesApi
class ScholarshipRepositoryTest {

    // Class under test
    private lateinit var repository: ScholarshipRepository

    private val fakeScholarshipService = FakeScholarshipService()

    @Before
    fun createRepository() {
        repository = ScholarshipRepositoryImpl(fakeScholarshipService)
    }

    @Test
    fun getScholarshipData_returnsScholarshipData() = runTest(UnconfinedTestDispatcher()) {
        val data = repository.getScholarshipData("1", false).first()
        assertThat(data).isEqualTo(TestData.scholarshipData)
    }

    @Test
    fun registerScholarshipTest_registerAndReturnSuccess() = runTest(UnconfinedTestDispatcher()) {
        val data = repository.registerScholarshipTest("id").first()
        assertThat(data.message).isEqualTo("Registration Successful")
    }
}

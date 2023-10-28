package com.doubtnutapp.libraryhome.mocktest.mapper

import com.doubtnutapp.data.base.Mapper
import com.doubtnutapp.domain.mockTestLibrary.entities.MockTestDetailsEntity
import com.doubtnutapp.domain.mockTestLibrary.entities.MockTestEntity
import com.doubtnutapp.libraryhome.mocktest.model.MockTestCourse
import com.doubtnutapp.libraryhome.mocktest.model.MockTestDetailsDataModel
import javax.inject.Inject

class MockTestCourseMapper @Inject constructor(
    private val mockTestDetailsDataModelMapper: MockTestDetailsDataModelMapper
) : Mapper<MockTestEntity, MockTestCourse> {

    override fun map(mockTestEntity: MockTestEntity) = with(mockTestEntity) {
        MockTestCourse(
            course,
            getMockTestDeatils(mockTestList)

        )
    }

    private fun getMockTestDeatils(mockTestList: List<MockTestDetailsEntity>): List<MockTestDetailsDataModel> =
        mockTestList.map {
            mockTestDetailsDataModelMapper.map(it)
        }

}
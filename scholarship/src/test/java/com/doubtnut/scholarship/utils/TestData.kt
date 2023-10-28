package com.doubtnut.scholarship.utils

import com.doubtnut.core.data.remote.CoreResponse
import com.doubtnut.core.data.remote.ResponseMeta
import com.doubtnut.core.entitiy.BaseResponse
import com.doubtnut.scholarship.data.entity.ScholarshipData

object TestData {

    val scholarshipData = ScholarshipData(
        scholarshipTestId = "1",
        testId = null,
        progress = null,
        bgColor = null,
        title = null,
        titleTextSize = null,
        titleTextColor = null,
        startTimeInMillis = null,
        stickyWidgets = null,
        widgets = null,
        footerWidgets = null,
        bottomData = null,
        extraParams = null
    )

    val response1 = CoreResponse(
        ResponseMeta(1, "abc", null),
        ScholarshipData(
            scholarshipTestId = "1",
            testId = null,
            progress = null,
            bgColor = null,
            title = null,
            titleTextSize = null,
            titleTextColor = null,
            startTimeInMillis = null,
            stickyWidgets = null,
            widgets = null,
            footerWidgets = null,
            bottomData = null,
            extraParams = null
        )
    )
    val response2 = CoreResponse(
        ResponseMeta(
            code = 200,
            message = "Success",
            success = null
        ),
        BaseResponse(
            message = "Registration Successful",
            deeplink = null,
            errorMessage = null,
            toastMessage = null,
            extraParams = null,
            success = true,
            data = null
        )
    )
}
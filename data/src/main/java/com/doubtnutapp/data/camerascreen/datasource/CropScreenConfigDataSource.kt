package com.doubtnutapp.data.camerascreen.datasource

import com.doubtnutapp.domain.camerascreen.entity.CropScreenConfigEntity

interface CropScreenConfigDataSource {

    fun saveCropScreenConfig(configHashMap: Map<String, String>)

    fun getCropScreenConfig(): CropScreenConfigEntity
}

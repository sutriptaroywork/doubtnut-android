package com.doubtnutapp.data.camerascreen.datasource

import com.doubtnutapp.domain.camerascreen.entity.CameraConfigEntity
import java.util.HashMap

interface ConfigDataSources {

    fun updateCameraScreenConfig(configHashMap: Map<String, Any>)

    fun getCameraScreenConfig(): CameraConfigEntity

    fun updateEnabledFeatures(configHashMap: Map<String, Any>)

    fun setBaseCdnUrl(configHashMap: Map<String, Any>)

    fun setForceUpdate(data: HashMap<String, Any>)

    fun setCampaignLandingDeeplink(configHashMap: Map<String, Any>)
}

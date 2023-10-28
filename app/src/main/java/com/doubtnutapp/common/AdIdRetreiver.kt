package com.doubtnutapp.common

import android.content.Context
import com.doubtnutapp.data.base.di.qualifier.ApplicationContext
import com.google.android.gms.ads.identifier.AdvertisingIdClient
import com.google.android.gms.common.GooglePlayServicesNotAvailableException
import java.io.IOException
import javax.inject.Inject

/**
 *  Created by Pradip Awasthi on 2019-10-01.
 */
class AdIdRetreiver @Inject constructor(
    @ApplicationContext private val context: Context
) {
    fun getId(): String? {
        var adInfo: AdvertisingIdClient.Info? = null
        try {
            adInfo = AdvertisingIdClient.getAdvertisingIdInfo(context)
        } catch (e: IOException) {
            // Unrecoverable error connecting to Google Play services (e.g.,
            // the old version of the service doesn't support getting AdvertisingId).
        } catch (e: Exception) {
            // Encountered a recoverable error connecting to Google Play services.
        } catch (e: GooglePlayServicesNotAvailableException) {
            // Google Play services is not available entirely.
        }
        return adInfo?.id
//  final boolean isLAT = adInfo.isLimitAdTrackingEnabled();
    }
}

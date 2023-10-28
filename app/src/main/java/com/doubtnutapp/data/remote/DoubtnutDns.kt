package com.doubtnutapp.data.remote

import androidx.core.content.edit
import com.doubtnutapp.defaultPrefs
import okhttp3.Dns
import java.net.InetAddress
import javax.inject.Singleton
import kotlin.math.abs

@Singleton
class DoubtnutDns : Dns {

    companion object {
        const val UPDATED_TS = "updated_ts"
        const val TIMEOUT = 10 * 60 * 1000 // 10 Minutes
    }

    private val hostAddressMapping = linkedMapOf<String, List<InetAddress>>()

    private fun put(hostName: String, addressList: List<InetAddress>) {
        hostAddressMapping[hostName] = addressList
    }

    override fun lookup(hostname: String): List<InetAddress> {

        if (hasDnsCacheTimeOut()) {
            hostAddressMapping.clear()

            // Set current Time stamp
            defaultPrefs().edit {
                putLong(UPDATED_TS, System.currentTimeMillis())
            }
        }

        val addressList = hostAddressMapping[hostname]

        return if (addressList == null) {
            val allHostAddress = listOf(InetAddress.getByName(hostname))
            put(hostname, allHostAddress)
            allHostAddress
        } else {
            addressList
        }
    }

    private fun hasDnsCacheTimeOut(): Boolean {
        val lastUpdatedTs = defaultPrefs().getLong(UPDATED_TS, 0)
        if (lastUpdatedTs == 0L) return true
        val currentTs = System.currentTimeMillis()
        return abs(currentTs - lastUpdatedTs) > TIMEOUT
    }
}

package com.doubtnut.analytics.constant

enum class EventPlatforms(private val value: String) {
    APXOR("apxor"),
    BRANCH("branch"),
    CLEVERTAP("clevertap"),
    MOENGAGE("moengage"),
    FIREBASE("firebase"),
    FACEBOOK("facebook"),
    SNOWPLOW("snowplow");

    override fun toString(): String {
        return value
    }

    companion object {
        fun fromString(str: String?): EventPlatforms? {
            if (str != null) {
                for (enum in values()) {
                    if (str.equals(enum.value, ignoreCase = true)) {
                        return enum
                    }
                }
            }
            return null
        }
    }
}
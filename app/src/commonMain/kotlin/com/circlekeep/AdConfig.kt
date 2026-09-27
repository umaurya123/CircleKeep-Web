package com.circlekeep

object AdConfig {
    val IOS_BANNER_AD_UNIT_ID: String
        get() = if (getPlatform().buildVariant.lowercase() == "release") {
            "ca-app-pub-9985549354765338/7939680639"
        } else {
            "ca-app-pub-3940256099942544/2934735716" // Test ID
        }

    val IOS_INTERSTITIAL_AD_UNIT_ID: String
        get() = if (getPlatform().buildVariant.lowercase() == "release") {
            "ca-app-pub-9985549354765338/1775306543"
        } else {
            "ca-app-pub-3940256099942544/4411468910" // Test ID
        }
}

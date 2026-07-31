package com.circlekeep

object AdConfig {
    val IOS_BANNER_AD_UNIT_ID: String
        get() = if (getPlatform().buildVariant == "Release") {
            "ca-app-pub-3940256099942544/2934735716" // TODO: Replace with your iOS Production Banner ID
        } else {
            "ca-app-pub-3940256099942544/2934735716" // Test ID
        }

    val IOS_INTERSTITIAL_AD_UNIT_ID: String
        get() = if (getPlatform().buildVariant == "Release") {
            "ca-app-pub-3940256099942544/4411468910" // TODO: Replace with your iOS Production Interstitial ID
        } else {
            "ca-app-pub-3940256099942544/4411468910" // Test ID
        }
}

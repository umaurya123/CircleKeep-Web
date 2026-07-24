package com.circlekeep

import platform.UIKit.UIDevice

class IOSPlatform: Platform {
    override val name: String = UIDevice.currentDevice.systemName() + " " + UIDevice.currentDevice.systemVersion
    
    override fun uriToBase64(uri: String): String? {
        // Implement using iOS native APIs (NSData, etc)
        return null 
    }

    override fun base64ToUri(base64: String, fileNamePrefix: String): String? {
        // Implement using iOS native APIs
        return null
    }

    override fun formatDisplayDate(dateString: String?): String {
        return dateString ?: ""
    }

    override fun formatPartialDate(day: String, month: String): String {
        return "$month $day"
    }

    override fun calculateAge(dobString: String?): Int? {
        return null
    }

    override fun parseDateToDayMonth(dateString: String): Pair<String, String>? {
        return null
    }

    override fun isDayValidForMonth(day: String, month: String): Boolean {
        return true
    }

    override fun currentTimeMillis(): Long = (platform.Foundation.NSDate().timeIntervalSince1970 * 1000).toLong()
}

actual fun getPlatform(): Platform = IOSPlatform()

package com.circlekeep

interface Platform {
    val name: String
    fun uriToBase64(uri: String): String?
    fun base64ToUri(base64: String, fileNamePrefix: String): String?
    fun formatDisplayDate(dateString: String?): String
    fun formatPartialDate(day: String, month: String): String
    fun calculateAge(dobString: String?): Int?
    fun parseDateToDayMonth(dateString: String): Pair<String, String>?
    fun isDayValidForMonth(day: String, month: String): Boolean
    fun currentTimeMillis(): Long
    val buildVariant: String
    val appVersion: String
}

expect fun getPlatform(): Platform

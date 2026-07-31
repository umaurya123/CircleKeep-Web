package com.circlekeep

import platform.UIKit.UIDevice
import platform.Foundation.*
import kotlinx.cinterop.ExperimentalForeignApi

@OptIn(ExperimentalForeignApi::class, kotlinx.cinterop.BetaInteropApi::class)
class IOSPlatform: Platform {
    override val name: String = UIDevice.currentDevice.systemName() + " " + UIDevice.currentDevice.systemVersion
    
    override fun uriToBase64(uri: String): String? {
        if (uri.isBlank()) return null
        val url = if (uri.startsWith("/")) {
            NSURL.fileURLWithPath(uri)
        } else {
            NSURL.URLWithString(uri) ?: return null
        }
        val data = NSData.dataWithContentsOfURL(url) ?: return null
        return data.base64EncodedStringWithOptions(0UL)
    }

    override fun base64ToUri(base64: String, fileNamePrefix: String): String? {
        if (base64.isBlank()) return null
        val data = NSData.create(base64EncodedString = base64, options = NSDataBase64DecodingIgnoreUnknownCharacters) ?: return null
        val fileName = "${fileNamePrefix}_${NSDate().timeIntervalSince1970}.jpg"
        val paths = NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, true)
        val documentDirectory = paths.firstOrNull() as? String ?: return null
        val filePath = "$documentDirectory/$fileName"
        data.writeToFile(filePath, true)
        return filePath
    }

    override fun formatDisplayDate(dateString: String?): String {
        val date = parseDate(dateString) ?: return dateString ?: ""
        val formatter = NSDateFormatter()
        formatter.dateStyle = NSDateFormatterMediumStyle
        return formatter.stringFromDate(date)
    }

    override fun formatPartialDate(day: String, month: String): String {
        if (day.isBlank() || month.isBlank()) return ""
        val months = listOf("", "Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
        val monthName = month.toIntOrNull()?.let { if (it in 1..12) months[it] else month } ?: month
        return "$monthName $day"
    }

    override fun calculateAge(dobString: String?): Int? {
        val date = parseDate(dobString) ?: return null
        val calendar = NSCalendar.currentCalendar
        val now = NSDate()
        val components = calendar.components(NSCalendarUnitYear, fromDate = date, toDate = now, options = 0UL)
        return components.year.toInt()
    }

    override fun parseDateToDayMonth(dateString: String): Pair<String, String>? {
        val date = parseDate(dateString) ?: return null
        val calendar = NSCalendar.currentCalendar
        val components = calendar.components(NSCalendarUnitDay or NSCalendarUnitMonth, fromDate = date)
        return components.day.toString() to components.month.toString()
    }

    override fun isDayValidForMonth(day: String, month: String): Boolean {
        if (day.isBlank()) return true
        val d = day.toIntOrNull() ?: return false
        if (d !in 1..31) return false
        
        val m = month.toIntOrNull() ?: return true 
        
        if (m !in 1..12) return false
        
        val maxDays = when (m) {
            1, 3, 5, 7, 8, 10, 12 -> 31
            4, 6, 9, 11 -> 30
            2 -> 29 
            else -> 0
        }
        return d in 1..maxDays
    }

    override fun currentTimeMillis(): Long = (NSDate().timeIntervalSince1970 * 1000).toLong()

    override val buildVariant: String by lazy {
        val variant = NSBundle.mainBundle.objectForInfoDictionaryKey("CKBuildVariant") as? String
        if (variant.isNullOrBlank()) "Debug" else variant
    }

    override val appVersion: String by lazy {
        NSBundle.mainBundle.objectForInfoDictionaryKey("CFBundleShortVersionString") as? String ?: "Unknown"
    }

    private fun parseDate(dateString: String?): NSDate? {
        if (dateString.isNullOrBlank()) return null
        val formats = listOf(
            "dd/MM/yyyy", "MM/dd/yyyy", "yyyy-MM-dd", "dd-MM-yyyy",
            "MM/dd/yy", "dd/MM/yy", "M/d/yy", "d/M/yy",
            "MMM dd, yyyy", "MMM d, yyyy", "MMMM dd, yyyy", "MMMM d, yyyy",
            "MMM dd, yy", "MMM d, yy", "MMMM dd, yy", "MMMM d, yy"
        )
        
        val formatter = NSDateFormatter()
        for (format in formats) {
            formatter.dateFormat = format
            val date = formatter.dateFromString(dateString)
            if (date != null) return date
        }
        return null
    }
}

actual fun getPlatform(): Platform = IOSPlatform()

package com.circlekeep

import android.content.Context
import android.net.Uri
import android.util.Base64
import java.io.File
import java.io.FileOutputStream

class AndroidPlatform(private val context: Context) : Platform {
    override val name: String = "Android ${android.os.Build.VERSION.SDK_INT}"

    override fun uriToBase64(uri: String): String? {
        if (uri.isBlank()) return null
        return try {
            val inputStream = if (uri.startsWith("/")) {
                File(uri).inputStream()
            } else {
                context.contentResolver.openInputStream(Uri.parse(uri))
            }
            inputStream?.use { input ->
                Base64.encodeToString(input.readBytes(), Base64.NO_WRAP)
            }
        } catch (e: Exception) {
            null
        }
    }

    override fun base64ToUri(base64: String, fileNamePrefix: String): String? {
        if (base64.isBlank()) return null
        return try {
            val bytes = Base64.decode(base64, Base64.DEFAULT)
            val fileName = "${fileNamePrefix}_${System.nanoTime()}.jpg"
            val file = File(context.filesDir, fileName)
            FileOutputStream(file).use { it.write(bytes) }
            file.absolutePath
        } catch (e: Exception) {
            null
        }
    }

    override fun formatDisplayDate(dateString: String?): String {
        val date = parseDate(dateString) ?: return dateString ?: ""
        return java.text.DateFormat.getDateInstance(java.text.DateFormat.MEDIUM).format(date)
    }

    override fun formatPartialDate(day: String, month: String): String {
        if (day.isBlank() || month.isBlank()) return ""
        val months = listOf("", "Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
        val monthName = month.toIntOrNull()?.let { if (it in 1..12) months[it] else month } ?: month
        return "$monthName $day"
    }

    override fun formatTimestamp(timestamp: Long): String {
        if (timestamp == 0L) return ""
        val date = java.util.Date(timestamp)
        return java.text.DateFormat.getDateTimeInstance(java.text.DateFormat.MEDIUM, java.text.DateFormat.SHORT).format(date)
    }

    override fun getDayOfMonth(): Int = java.util.Calendar.getInstance().get(java.util.Calendar.DAY_OF_MONTH)
    
    override fun getMonth(): Int = java.util.Calendar.getInstance().get(java.util.Calendar.MONTH) + 1

    override fun calculateAge(dobString: String?): Int? {
        val date = parseDate(dobString) ?: return null
        val today = java.util.Calendar.getInstance()
        val birth = java.util.Calendar.getInstance().apply { time = date }
        var age = today.get(java.util.Calendar.YEAR) - birth.get(java.util.Calendar.YEAR)
        if (today.get(java.util.Calendar.DAY_OF_YEAR) < birth.get(java.util.Calendar.DAY_OF_YEAR)) age--
        return age
    }

    override fun parseDateToDayMonth(dateString: String): Pair<String, String>? {
        val date = parseDate(dateString) ?: return null
        val cal = java.util.Calendar.getInstance()
        cal.time = date
        val day = cal.get(java.util.Calendar.DAY_OF_MONTH).toString()
        val month = (cal.get(java.util.Calendar.MONTH) + 1).toString()
        return day to month
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

    override fun currentTimeMillis(): Long = System.currentTimeMillis()

    override val buildVariant: String = BuildConfig.BUILD_TYPE

    override val appVersion: String by lazy {
        try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            packageInfo.versionName ?: "Unknown"
        } catch (e: Exception) {
            "Unknown"
        }
    }

    private fun parseDate(dateString: String?): java.util.Date? {
        if (dateString.isNullOrBlank()) return null
        val formats = listOf(
            "dd/MM/yyyy", "MM/dd/yyyy", "yyyy-MM-dd", "dd-MM-yyyy",
            "MM/dd/yy", "dd/MM/yy", "M/d/yy", "d/M/yy",
            "MMM dd, yyyy", "MMM d, yyyy", "MMMM dd, yyyy", "MMMM d, yyyy",
            "MMM dd, yy", "MMM d, yy", "MMMM dd, yy", "MMMM d, yy"
        )
        
        val currentYear = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)
        
        for (format in formats) {
            try {
                val sdf = java.text.SimpleDateFormat(format, java.util.Locale.getDefault())
                sdf.isLenient = false
                var date = sdf.parse(dateString)
                if (date != null) {
                    val cal = java.util.Calendar.getInstance().apply { time = date }
                    var year = cal.get(java.util.Calendar.YEAR)
                    
                    if (year < 100) {
                        val pivot = (currentYear % 100) + 10
                        year += if (year > pivot) 1900 else 2000
                        cal.set(java.util.Calendar.YEAR, year)
                        date = cal.time
                    } else if (year > currentYear && year < currentYear + 100) {
                        cal.set(java.util.Calendar.YEAR, year - 100)
                        date = cal.time
                    }
                    return date
                }
            } catch (_: Exception) { }
        }
        val styles = listOf(java.text.DateFormat.SHORT, java.text.DateFormat.MEDIUM, java.text.DateFormat.LONG)
        for (style in styles) {
            try {
                val df = java.text.DateFormat.getDateInstance(style, java.util.Locale.getDefault())
                df.isLenient = false
                var date = df.parse(dateString)
                if (date != null) {
                    val cal = java.util.Calendar.getInstance().apply { time = date }
                    var year = cal.get(java.util.Calendar.YEAR)
                    if (year < 100) {
                        val pivot = (currentYear % 100) + 10
                        year += if (year > pivot) 1900 else 2000
                        cal.set(java.util.Calendar.YEAR, year)
                        date = cal.time
                    } else if (year > currentYear && year < currentYear + 100) {
                        cal.set(java.util.Calendar.YEAR, year - 100)
                        date = cal.time
                    }
                    return date
                }
            } catch (_: Exception) { }
        }
        return null
    }
}

private var androidPlatform: Platform? = null

fun initPlatform(context: Context) {
    androidPlatform = AndroidPlatform(context)
}

actual fun getPlatform(): Platform = androidPlatform ?: object : Platform {
    override val name: String = "Android (Fallback)"
    override fun uriToBase64(uri: String): String? = null
    override fun base64ToUri(base64: String, fileNamePrefix: String): String? = null
    override fun formatDisplayDate(dateString: String?): String = dateString ?: ""
    override fun formatPartialDate(day: String, month: String): String = ""
    override fun formatTimestamp(timestamp: Long): String = ""
    override fun getDayOfMonth(): Int = 1
    override fun getMonth(): Int = 1
    override fun calculateAge(dobString: String?): Int? = null
    override fun parseDateToDayMonth(dateString: String): Pair<String, String>? = null
    override fun isDayValidForMonth(day: String, month: String): Boolean = true
    override fun currentTimeMillis(): Long = System.currentTimeMillis()
    override val buildVariant: String = "Unknown"
    override val appVersion: String = "Unknown"
}

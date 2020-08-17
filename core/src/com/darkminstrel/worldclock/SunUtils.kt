package com.darkminstrel.worldclock

import java.util.*
import kotlin.math.atan
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

object SunUtils {
    private const val K = Math.PI / 180.0
    private val timeZone = TimeZone.getTimeZone("UTC")
    private val calendar = Calendar.getInstance(timeZone)

    fun computeSunLocation(dst: LatLng, millis:Long){
        calendar.timeInMillis = millis
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)+1
        val date = calendar.get(Calendar.DAY_OF_MONTH)
        val hours = calendar.get(Calendar.HOUR_OF_DAY)
        val minutes = calendar.get(Calendar.MINUTE)
        val seconds = calendar.get(Calendar.SECOND)
        val std = hours + minutes / 60.0 + seconds / 3600.0
        var days:Long = 365 * year + date + 31 * month - 46.toLong()
        days = if (month < 3) days + ((year - 1) / 4) else days - (0.4 * month + 2.3).toInt() + (year / 4.0).toInt()

        var x:Double = (days - 693960) / 1461.0
        x = (x - x.toInt()) * 1440.02509 + x.toInt() * 0.0307572
        x += std / 24.0 * 0.9856645 + 356.6498973
        x += 1.91233 * sin(0.9999825 * x * K)
        x = (x + sin(1.999965 * x * K) / 50.0 + 282.55462) / 360.0
        x = (x - x.toInt()) * 360.0
        val j2000:Double = (year - 2000) / 100.0
        val ecliptic:Double = 23.43929111 - (46.8150 + (0.00059 - 0.001813 * j2000) * j2000) * j2000 / 3600.0
        x = sin(x * K) * sin(K * ecliptic)
        val declination = atan(x / sqrt(1.0 - x * x)) / K + 0.00075

        val p:Double = std / 24.0
        var x2:Double = (p + days - 7.22449E5) * 0.98564734 + 279.306
        x2 *= K
        var g:Double = -104.55 * sin(x2) - 429.266 * cos(x2) + 595.63 * sin(2.0 * x2) - 2.283 * cos(2.0 * x2)
        g += 4.6 * sin(3.0 * x2) + 18.7333 * cos(3.0 * x2)
        g = g - 13.2 * sin(4.0 * x2) - cos(5.0 * x2) - sin(5.0 * x2) / 3.0 + 0.5 * sin(6.0 * x2) + 0.231
        g = g / 240.0 + 360.0 * (p + 0.5)
        if (g > 360) g -= 360.0
        val gha = g

        dst.set(declination.toFloat(), -gha.toFloat())
    }

}
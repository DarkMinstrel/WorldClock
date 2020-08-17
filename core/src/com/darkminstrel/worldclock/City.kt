package com.darkminstrel.worldclock

import java.util.*

enum class City(private val cityName:String, val lat:Float, val long:Float, private val timezone:String) {
    KYIV("Kyiv", 50.45f, 30.523333f, "Europe/Kiev"),
    NEW_YORK("New York", 40.72833f,-73.99417f, "America/New_York"),
    LA("Los Angeles", 34.05f, -118.25f, "America/Los_Angeles"),
    TOKYO("Tokyo", 35.7f,139.6f, "Asia/Tokyo");

    private val calendar = Calendar.getInstance(TimeZone.getTimeZone(this.timezone))
    private val sb = StringBuilder()
    private val formatter = Formatter(sb)

    fun formatLabelText(millis:Long, dst:StringBuilder){
        this.sb.clear()
        this.calendar.timeInMillis = millis
        this.formatter.format("%02d:%02d:%02d", calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), calendar.get(Calendar.SECOND))

        dst.clear()
        dst.append("  ").append(this.cityName).append("\n  ").append(sb)
    }
}
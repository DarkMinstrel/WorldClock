package com.darkminstrel.worldclock

import java.util.*

enum class City(val cityName:String, val lat:Float, val long:Float, private val timezone:TimeZone) {
    KYIV("Kyiv", 50.45f, 30.523333f, TimeZone.getTimeZone("Europe/Kiev")),
    NEW_YORK("New York", 40.72833f,-73.99417f, TimeZone.getTimeZone("America/New_York")),
    LA("Los Angeles", 34.05f, -118.25f, TimeZone.getTimeZone("America/Los_Angeles")),
    TOKYO("Tokyo", 35.7f,139.6f, TimeZone.getTimeZone("Asia/Tokyo"));

    fun getTime():String{
        val c = Calendar.getInstance(this.timezone)
        return String.format("%02d:%02d:%02d", c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), c.get(Calendar.SECOND))
    }

    fun getLabelText():String{
        return "  "+this.cityName+"\n  "+getTime()
    }
}
package com.darkminstrel.worldclock

import java.util.*

enum class City(private val cityName:String, val lat:Float, val long:Float, private val timezone:String) {
    KYIV("Kyiv", 50.45f, 30.523333f, "Europe/Kiev"),
    //LONDON("London", 51.507222f, -0.1275f, "Europe/London"),
    MADRID("Madrid",40.383333f, -3.716667f, "Europe/Madrid"),
    NEW_YORK("New York", 40.72833f,-73.99417f, "America/New_York"),
    LOS_ANGELES("Los Angeles", 34.05f, -118.25f, "America/Los_Angeles"),
    TOKYO("Tokyo", 35.7f,139.6f, "Asia/Tokyo"),
    MUMBAI("Mumbai", 18.975f, 72.825833f, "Asia/Calcutta"),
    SYDNEY("Sydney",-33.86944f,151.20833f,"Australia/Sydney"),
    BUENOS_AIRES("Buenos Aires", -34.603333f, -58.381667f, "America/Argentina/Buenos_Aires"),
    //SAO_PAULO("Sao Paulo", -23.55f, -46.633333f, "America/Sao_Paulo"),
    JOHANNESBURG("Johannesburg", -26.145000f, 28.050280f, "Africa/Johannesburg"),
    NOVOSIBIRSK("Novosibirsk", 55.05f, 82.95f, "Asia/Novosibirsk")
    ;

    private val calendar = Calendar.getInstance(TimeZone.getTimeZone(this.timezone))
    private val sb = StringBuilder()
    private val formatter = Formatter(sb)

    fun formatLabelText(millis:Long, dst:StringBuilder){
        this.sb.clear()
        this.calendar.timeInMillis = millis
        this.formatter.format("%02d:%02d:%02d", calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), calendar.get(Calendar.SECOND))

        dst.clear()
        dst.append(this.cityName).append("\n").append(sb)
    }
}
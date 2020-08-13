package com.darkminstrel.worldclock

import kotlin.math.sqrt

object Config {
    const val MAX_FOV = 67f //degrees
    const val MIN_FOV = 32f //degrees

    const val LIGHT_AMBIENT = 0.5f
    const val LIGHT_DIRECTIONAL = 1.0f

    const val EARTH_RADIUS = 50f
    const val EARTH_DETALIZATION = 30
    const val CITY_RADIUS = 1f
    const val CITY_DETALIZATION = 10
    const val CAMERA_DISTANCE = 180f

    val MAX_LABEL_DISTANCE = sqrt(CAMERA_DISTANCE * CAMERA_DISTANCE - EARTH_RADIUS * EARTH_RADIUS)
    val MIN_LABEL_DISTANCE = CAMERA_DISTANCE - EARTH_RADIUS


}
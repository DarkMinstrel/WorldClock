package com.darkminstrel.worldclock.data

import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight
import com.badlogic.gdx.math.Vector3
import com.darkminstrel.worldclock.*

class Sun:ISun {

    private val directionalLight = DirectionalLight().set(Config.LIGHT_DIRECTIONAL, Config.LIGHT_DIRECTIONAL, Config.LIGHT_DIRECTIONAL, Config.MAX_CAMERA_DISTANCE, 0f, 0f)
    private val sunLocation = LatLng(0f,0f)
    private val vectorSun = Vector3()

    init {
        setTime(System.currentTimeMillis())
    }

    override fun setTime(millis: Long) {
        SunUtils.computeSunLocation(sunLocation, millis)
        //DBG("SUN: $sunLocation")
        geoToVector(vectorSun, sunLocation.lat, sunLocation.lng, Config.MAX_CAMERA_DISTANCE)
        directionalLight.direction.set(0f,0f,0f).sub(vectorSun).nor()
    }

    override fun getDirectionalLight(): DirectionalLight = directionalLight

}
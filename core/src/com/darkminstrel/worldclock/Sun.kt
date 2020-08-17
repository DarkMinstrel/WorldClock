package com.darkminstrel.worldclock

import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight
import com.badlogic.gdx.math.Vector3
import com.darkminstrel.worldclock.utils.LatLng
import com.darkminstrel.worldclock.utils.SunUtils
import com.darkminstrel.worldclock.utils.geoToVector

interface ISun {
    fun setTime(millis:Long)
    fun getDirectionalLight():DirectionalLight
}

class Sun: ISun {

    private val directionalLight = DirectionalLight().set(_Config.LIGHT_DIRECTIONAL, _Config.LIGHT_DIRECTIONAL, _Config.LIGHT_DIRECTIONAL, _Config.MAX_CAMERA_DISTANCE, 0f, 0f)
    private val sunLocation = LatLng(0f, 0f)
    private val vectorSun = Vector3()

    init {
        setTime(System.currentTimeMillis())
    }

    override fun setTime(millis: Long) {
        SunUtils.computeSunLocation(sunLocation, millis)
        //DBG("SUN: $sunLocation")
        geoToVector(vectorSun, sunLocation.lat, sunLocation.lng, _Config.MAX_CAMERA_DISTANCE)
        directionalLight.direction.set(0f,0f,0f).sub(vectorSun).nor()
    }

    override fun getDirectionalLight(): DirectionalLight = directionalLight

}
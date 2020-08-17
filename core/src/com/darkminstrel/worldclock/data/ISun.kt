package com.darkminstrel.worldclock.data

import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight

interface ISun {
    fun setTime(millis:Long)
    fun getDirectionalLight():DirectionalLight
}
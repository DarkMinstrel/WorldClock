package com.darkminstrel.worldclock.data

import com.badlogic.gdx.graphics.PerspectiveCamera

interface IBeholder {
    fun move(dx:Float, dy:Float)
    fun fling(dx:Float, dy:Float)
    fun setDistance(distance:Float)
    fun getDistance():Float
    fun tryZoom()
    fun getCamera():PerspectiveCamera
    fun updateCamera()
}
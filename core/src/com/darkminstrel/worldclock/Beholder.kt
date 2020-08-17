package com.darkminstrel.worldclock

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.PerspectiveCamera
import com.badlogic.gdx.math.Vector3
import com.darkminstrel.worldclock.utils.radiansToVector
import com.darkminstrel.worldclock.utils.toRadians
import kotlin.math.max
import kotlin.math.min

interface IBeholder {
    fun move(dx:Float, dy:Float)
    fun fling(dx:Float, dy:Float)
    fun setDistance(distance:Float)
    fun getDistance():Float
    fun tryZoom()
    fun getCamera():PerspectiveCamera
    fun updateCamera(elapsed:Float)
}

class Beholder: IBeholder {
    private var phi:Float = toRadians(0f)
    private var theta:Float = toRadians(30f)
    private var distance = _Config.MAX_CAMERA_DISTANCE
    private val position = Vector3()
    private var speedD = 0.0f
    private var speedPhi = 0.5f

    private var camera = PerspectiveCamera(_Config.CAMERA_FOV, Gdx.graphics.width.toFloat(), Gdx.graphics.height.toFloat()).apply {
        near = 1f
        far = _Config.MAX_CAMERA_DISTANCE
    }

    init {
        updateCamera(0f)
    }

    override fun move(dx:Float, dy:Float){
        speedPhi = 0f
        val c = distance / _Config.MAX_CAMERA_DISTANCE
        phi -= toRadians(dx) * c
        theta += toRadians(dy) * c
        theta = min(toRadians(80f), max(toRadians(-80f), theta))
    }

    override fun fling(dx: Float, dy: Float) {
        val c = distance / _Config.MAX_CAMERA_DISTANCE / 4f
        speedPhi = -toRadians(dx) * c
    }

    override fun setDistance(distance:Float){
        speedPhi = 0.0f
        speedD = 0.0f
        this.distance = max(_Config.MIN_CAMERA_DISTANCE, min(_Config.MAX_CAMERA_DISTANCE, distance))
    }

    override fun tryZoom(){
        speedD = if(this.distance==_Config.MAX_CAMERA_DISTANCE) -300f else 300f
    }

    override fun getDistance(): Float = this.distance

    override fun getCamera(): PerspectiveCamera = this.camera

    override fun updateCamera(elapsed:Float){
        if(speedD != 0f) distance = min(_Config.MAX_CAMERA_DISTANCE, max(_Config.MIN_CAMERA_DISTANCE, distance + speedD * elapsed))
        phi += speedPhi * elapsed

        radiansToVector(position, theta, phi, distance)
        camera.apply {
            position.set(this@Beholder.position)
            direction.set(0f, 0f, 0f).sub(this@Beholder.position).nor()
            up.set(0f,1f,0f)
            update()
        }
    }
}
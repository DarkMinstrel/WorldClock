package com.darkminstrel.worldclock.data

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.PerspectiveCamera
import com.badlogic.gdx.math.Vector3
import com.darkminstrel.worldclock.Config
import com.darkminstrel.worldclock.radiansToVector
import com.darkminstrel.worldclock.toRadians
import kotlin.math.max
import kotlin.math.min

class Beholder:IBeholder {
    private var phi:Float = toRadians(0f)
    private var theta:Float = toRadians(30f)
    private var distance = Config.MAX_CAMERA_DISTANCE
    private val position = Vector3()
    private var speedD = 0.0f
    private var speedPhi = 0.5f

    private val camera = PerspectiveCamera(Config.CAMERA_FOV, Gdx.graphics.width.toFloat(), Gdx.graphics.height.toFloat()).apply {
        near = 1f
        far = Config.MAX_CAMERA_DISTANCE
    }

    init {
        updateCamera()
    }

    override fun move(dx:Float, dy:Float){
        speedPhi = 0f
        val c = distance / Config.MAX_CAMERA_DISTANCE
        phi -= toRadians(dx) * c
        theta += toRadians(dy) * c
        theta = min(toRadians(80f), max(toRadians(-80f), theta))
    }

    override fun fling(dx: Float, dy: Float) {
        val c = distance / Config.MAX_CAMERA_DISTANCE / 4f
        speedPhi = -toRadians(dx) * c
    }

    override fun setDistance(distance:Float){
        speedPhi = 0.0f
        speedD = 0.0f
        this.distance = max(Config.MIN_CAMERA_DISTANCE, min(Config.MAX_CAMERA_DISTANCE, distance))
    }

    override fun tryZoom(){
        speedD = if(this.distance==Config.MAX_CAMERA_DISTANCE) -300f else 300f
    }

    override fun getDistance(): Float = this.distance

    override fun getCamera(): PerspectiveCamera = this.camera

    override fun updateCamera(){
        val elapsed = Gdx.graphics.deltaTime
        if(speedD != 0f) distance = min(Config.MAX_CAMERA_DISTANCE, max(Config.MIN_CAMERA_DISTANCE, distance + speedD * elapsed))
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
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

    private val camera = PerspectiveCamera(Config.CAMERA_FOV, Gdx.graphics.width.toFloat(), Gdx.graphics.height.toFloat()).apply {
        near = 1f
        far = Config.MAX_CAMERA_DISTANCE
    }

    init {
        updateCamera()
    }

    override fun move(dx:Float, dy:Float){
        val coeff = distance / Config.MAX_CAMERA_DISTANCE
        phi -= toRadians(dx) * coeff
        theta += toRadians(dy) * coeff
        theta = min(toRadians(80f), max(toRadians(-80f), theta))
        updateCamera()
    }

    override fun setDistance(distance:Float){
        this.distance = max(Config.MIN_CAMERA_DISTANCE, min(Config.MAX_CAMERA_DISTANCE, distance))
        updateCamera()
    }

    override fun getDistance(): Float = this.distance

    override fun getCamera(): PerspectiveCamera = this.camera

    private fun updateCamera(){
        radiansToVector(position, theta, phi, distance)
        camera.apply {
            position.set(this@Beholder.position)
            direction.set(0f, 0f, 0f).sub(this@Beholder.position).nor()
            up.set(0f,1f,0f)
            update()
        }
    }
}
package com.darkminstrel.worldclock

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.input.GestureDetector
import com.badlogic.gdx.math.Vector2
import com.darkminstrel.worldclock.data.IBeholder

class GestureListener(private val beholder: IBeholder): GestureDetector.GestureListener {
    private val density = Gdx.graphics.density

    override fun pan(x: Float, y: Float, deltaX: Float, deltaY: Float): Boolean {
        beholder.move(deltaX/density, deltaY/density)
        return true
    }

    private var initialDistance:Float? = null
    override fun zoom(initialDistance: Float, distance: Float): Boolean {
        if(this.initialDistance==null) this.initialDistance = beholder.getDistance()
        beholder.setDistance((initialDistance/distance) * (this.initialDistance!!))
        return true
    }

    override fun pinchStop() {
        initialDistance = null
    }

    override fun tap(x: Float, y: Float, count: Int, button: Int): Boolean {
        return if(count==2) {
            beholder.tryZoom()
            true
        }else if(count==1){
            beholder.fling(0f,0f)
            true
        }else{
            false
        }
    }

    override fun fling(velocityX: Float, velocityY: Float, button: Int): Boolean {
        beholder.fling(velocityX/density, velocityY/density)
        return true
    }

    override fun panStop(x: Float, y: Float, pointer: Int, button: Int): Boolean {
        return false
    }

    override fun longPress(x: Float, y: Float): Boolean {
        return false
    }

    override fun touchDown(x: Float, y: Float, pointer: Int, button: Int): Boolean {
        return false
    }

    override fun pinch(initialPointer1: Vector2?, initialPointer2: Vector2?, pointer1: Vector2?, pointer2: Vector2?): Boolean {
        return false
    }
}
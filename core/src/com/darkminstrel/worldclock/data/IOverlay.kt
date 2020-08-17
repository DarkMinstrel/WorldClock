package com.darkminstrel.worldclock.data

interface IOverlay {
    fun update(now:Long, beholder:IBeholder)
    fun dispose()
}
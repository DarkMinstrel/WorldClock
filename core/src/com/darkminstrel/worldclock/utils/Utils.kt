package com.darkminstrel.worldclock.utils

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.math.Vector3
import kotlin.math.cos
import kotlin.math.sin

fun DBG(s:String?) = Gdx.app.log("GDXDBG", s)

const val PI = 3.141592653589793f

fun toRadians(deg:Float):Float = deg * PI / 180.0f
fun toDegrees(rad:Float):Float = rad / PI * 180.0f

fun radiansToVector(dst:Vector3, theta:Float, phi:Float, radius: Float){
    val x = radius * cos(theta) * cos(PI -phi)
    val y = radius * sin(theta)
    val z = radius * cos(theta) * sin(PI -phi)
    dst.set(x,y,z)
}

fun geoToVector(dst:Vector3, lat:Float, lng:Float, radius: Float) = radiansToVector(dst, toRadians(lat), toRadians(lng), radius)

fun geoToVector(lat:Float, lng:Float, radius: Float) = Vector3().also { geoToVector(it, lat, lng, radius) }
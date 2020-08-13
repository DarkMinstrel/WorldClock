package com.darkminstrel.worldclock

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.Vector3

object World {
    interface Fill

    enum class FillTexture(val filename:String):Fill {
        EARTH("earth2.png")
    }
    enum class FillColor(val color:Color):Fill {
        YELLOW(Color.YELLOW)
    }
    val allFills = ArrayList<Fill>().apply {
        addAll(FillTexture.values())
        addAll(FillColor.values())
    }

    open class WorldModel(val fill:Fill)
    class SphereModel(fill:Fill, val radius:Float, val detalization:Int):WorldModel(fill)
    private val earthModel = SphereModel(FillTexture.EARTH, Config.EARTH_RADIUS, Config.EARTH_DETALIZATION)
    private val cityModel = SphereModel(FillColor.YELLOW, Config.CITY_RADIUS, Config.CITY_DETALIZATION)
    val allModels = listOf<WorldModel>(earthModel, cityModel)

    open class WorldObject(val position: Vector3, val model: WorldModel)
    val earth = WorldObject(Vector3(0f,0f,0f), earthModel)
    class CityObject(val city: City): WorldObject(geoToVector(city.lat, city.long, Config.EARTH_RADIUS), cityModel)

    val cities = City.values().map { CityObject(it) }

    val allObjects = ArrayList<WorldObject>().apply {
        add(earth)
        addAll(cities)
    }
}
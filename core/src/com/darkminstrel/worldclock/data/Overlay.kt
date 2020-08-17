package com.darkminstrel.worldclock.data

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.darkminstrel.worldclock.City
import com.darkminstrel.worldclock.Config
import com.darkminstrel.worldclock.geoToVector
import java.util.*
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt

class Overlay:IOverlay {

    private val stage = Stage()
    private val font: BitmapFont
    private val labels = IdentityHashMap<City, Label>()

    init {
        FreeTypeFontGenerator(Gdx.files.internal(Config.FONT)).apply {
            font = generateFont(FreeTypeFontGenerator.FreeTypeFontParameter().also { it.size = (Config.FONT_SIZE * Gdx.graphics.density).toInt() })
            dispose()
        }
        for(city in City.values()){
            Label(city.name, Label.LabelStyle(font, Color.WHITE.cpy())).also {
                stage.addActor(it)
                labels[city] = it
            }
        }
    }

    private val tempVector = Vector3()
    private val tempString = StringBuilder()

    override fun update(now: Long, beholder: IBeholder) {
        val maxDistance = sqrt(beholder.getDistance() * beholder.getDistance() - Config.EARTH_RADIUS * Config.EARTH_RADIUS)
        val minDistance = beholder.getDistance() - Config.EARTH_RADIUS

        for ((city, label) in labels) {
            geoToVector(tempVector, city.lat, city.long, Config.EARTH_RADIUS)
            val distance = tempVector.dst(beholder.getCamera().position)
            val alpha = 1f - min(1f, max(0f, (distance - minDistance) / (maxDistance - minDistance)))

            beholder.getCamera().project(tempVector)
            label.setPosition(tempVector.x, tempVector.y)
            label.style.fontColor.a = alpha
            city.formatLabelText(now, tempString)
            label.setText(tempString)
        }

        stage.draw()
    }

    override fun dispose() {
        stage.dispose()
        font.dispose()
    }
}
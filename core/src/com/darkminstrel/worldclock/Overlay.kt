package com.darkminstrel.worldclock

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.darkminstrel.worldclock.utils.Cities
import com.darkminstrel.worldclock.utils.geoToVector
import java.util.*
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt

interface IOverlay {
    fun update(now:Long, beholder: IBeholder)
    fun dispose()
}

class Overlay: IOverlay {

    private val stage = Stage()
    private val font: BitmapFont
    private val labels = IdentityHashMap<Cities, Label>()
    private val paddingX = Gdx.graphics.density * 8f
    private val paddingY = 0f

    init {
        FreeTypeFontGenerator(Gdx.files.internal(_Config.FONT)).apply {
            font = generateFont(FreeTypeFontGenerator.FreeTypeFontParameter().apply {
                size = (_Config.FONT_SIZE * Gdx.graphics.density).toInt()
                spaceY = (2f * Gdx.graphics.density).toInt()
            })
            dispose()
        }
        for(city in Cities.values()){
            Label(city.name, Label.LabelStyle(font, Color.WHITE.cpy())).also {
                stage.addActor(it)
                labels[city] = it
            }
        }
    }

    private val tempVector = Vector3()
    private val tempString = StringBuilder()

    override fun update(now: Long, beholder: IBeholder) {
        val maxDistance = sqrt(beholder.getDistance() * beholder.getDistance() - _Config.EARTH_RADIUS * _Config.EARTH_RADIUS)
        val minDistance = beholder.getDistance() - _Config.EARTH_RADIUS
        val coeff = beholder.getDistance() / _Config.MAX_CAMERA_DISTANCE

        for ((city, label) in labels) {
            geoToVector(tempVector, city.lat, city.long, _Config.EARTH_RADIUS)
            val distance = tempVector.dst(beholder.getCamera().position)
            val alpha = 1f - min(1f, max(0f, (distance - minDistance) / (maxDistance - minDistance)))

            beholder.getCamera().project(tempVector)
            label.setPosition(tempVector.x + paddingX/coeff, tempVector.y + paddingY)
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
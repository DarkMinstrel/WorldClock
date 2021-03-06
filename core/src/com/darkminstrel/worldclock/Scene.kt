package com.darkminstrel.worldclock

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.VertexAttributes
import com.badlogic.gdx.graphics.g3d.Environment
import com.badlogic.gdx.graphics.g3d.Material
import com.badlogic.gdx.graphics.g3d.ModelBatch
import com.badlogic.gdx.graphics.g3d.ModelInstance
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder
import com.badlogic.gdx.input.GestureDetector
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.utils.Disposable
import com.darkminstrel.worldclock.utils.Cities
import com.darkminstrel.worldclock.utils.geoToVector

interface IScene {
    fun render()
    fun dispose()
}

class Scene: IScene {

    private val beholder: IBeholder = Beholder()
    private val sun: ISun = Sun()
    private val environment = Environment().apply {
        set(ColorAttribute(ColorAttribute.AmbientLight, _Config.LIGHT_AMBIENT, _Config.LIGHT_AMBIENT, _Config.LIGHT_AMBIENT, 1f))
        add(sun.getDirectionalLight())
    }
    private val instances = ArrayList<ModelInstance>()
    //disposables
    private val assetManager = AssetManager()
    private val modelBatch = ModelBatch()
    private val overlay: IOverlay = Overlay()
    private val disposables = ArrayList<Disposable>()

    init{
        assetManager.apply {
            load(_Config.TEXTURE_EARTH, Texture::class.java)
            finishLoading()
        }
        val modelBuilder = ModelBuilder()

        //earth
        val earthTexture = assetManager.get(_Config.TEXTURE_EARTH, Texture::class.java).also { disposables.add(it) }
        val earthMaterial = Material(TextureAttribute.createDiffuse(earthTexture))
        val earthModel = modelBuilder.createSphere(_Config.EARTH_RADIUS*2, _Config.EARTH_RADIUS*2, _Config.EARTH_RADIUS*2,
                _Config.EARTH_DETALIZATION, _Config.EARTH_DETALIZATION, earthMaterial, (VertexAttributes.Usage.Position or VertexAttributes.Usage.Normal or VertexAttributes.Usage.TextureCoordinates).toLong())
                .also { disposables.add(it) }
        ModelInstance(earthModel).also { instances.add(it) }

        //cities
        val tempVector = Vector3()
        val cityMaterial = Material(ColorAttribute.createDiffuse(Color.GREEN))
        val cityModel = modelBuilder.createSphere(_Config.CITY_RADIUS*2, _Config.CITY_RADIUS*2, _Config.CITY_RADIUS*2,
                _Config.CITY_DETALIZATION, _Config.CITY_DETALIZATION, cityMaterial, (VertexAttributes.Usage.Position or VertexAttributes.Usage.Normal).toLong())
                .also { disposables.add(it) }
        for(city in Cities.values()){
            ModelInstance(cityModel).apply {
                geoToVector(tempVector, city.lat, city.long, _Config.EARTH_RADIUS - _Config.CITY_RADIUS / 2)
                transform.setToTranslation(tempVector)
            }.also { instances.add(it) }
        }

        Gdx.input.inputProcessor = GestureDetector(GestureListener(beholder))
    }

    override fun render() {
        val now = System.currentTimeMillis()
        val elapsed = Gdx.graphics.deltaTime

        sun.setTime(now)
        beholder.updateCamera(elapsed)

        with(modelBatch) {
            begin(beholder.getCamera())
            instances.forEach { render(it, environment) }
            end()
        }

        overlay.update(now, beholder)
    }

    override fun dispose() {
        assetManager.dispose()
        modelBatch.dispose()
        overlay.dispose()
        disposables.forEach { it.dispose() }
    }

}
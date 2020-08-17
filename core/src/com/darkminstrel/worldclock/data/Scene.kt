package com.darkminstrel.worldclock.data

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
import com.darkminstrel.worldclock.*

class Scene:IScene {

    private val beholder:IBeholder = Beholder()
    private val sun: ISun = Sun()
    private val environment = Environment().apply {
        set(ColorAttribute(ColorAttribute.AmbientLight, Config.LIGHT_AMBIENT, Config.LIGHT_AMBIENT, Config.LIGHT_AMBIENT, 1f))
        add(sun.getDirectionalLight())
    }
    private val instances = ArrayList<ModelInstance>()
    //disposables
    private val assetManager = AssetManager()
    private val modelBatch = ModelBatch()
    private val overlay:IOverlay = Overlay()
    private val disposables = ArrayList<Disposable>()

    init{
        val modelBuilder = ModelBuilder()

        assetManager.apply {
            load(Config.TEXTURE_EARTH, Texture::class.java)
            finishLoading()
        }

        //earth
        val earthTexture = assetManager.get(Config.TEXTURE_EARTH, Texture::class.java).also { disposables.add(it) }
        val earthMaterial = Material(TextureAttribute.createDiffuse(earthTexture))
        val earthModel = modelBuilder.createSphere(Config.EARTH_RADIUS*2, Config.EARTH_RADIUS*2, Config.EARTH_RADIUS*2, Config.EARTH_DETALIZATION, Config.EARTH_DETALIZATION, earthMaterial, (VertexAttributes.Usage.Position or VertexAttributes.Usage.Normal or VertexAttributes.Usage.TextureCoordinates).toLong()).also { disposables.add(it) }
        ModelInstance(earthModel).also { instances.add(it) }

        //cities
        val tempVector = Vector3()
        val cityMaterial = Material(ColorAttribute.createDiffuse(Color.GREEN))
        val cityModel = modelBuilder.createSphere(Config.CITY_RADIUS*2, Config.CITY_RADIUS*2, Config.CITY_RADIUS*2, Config.CITY_DETALIZATION, Config.CITY_DETALIZATION, cityMaterial, (VertexAttributes.Usage.Position or VertexAttributes.Usage.Normal).toLong()).also { disposables.add(it) }
        for(city in City.values()){
            ModelInstance(cityModel).apply {
                geoToVector(tempVector, city.lat, city.long, Config.EARTH_RADIUS-Config.CITY_RADIUS/2)
                transform.setToTranslation(tempVector)
            }.also { instances.add(it) }
        }

        Gdx.input.inputProcessor = GestureDetector(GestureListener(beholder))
    }

    override fun render() {
        val now = System.currentTimeMillis()
        sun.setTime(now)
        beholder.updateCamera()

        with(modelBatch) {
            begin(beholder.getCamera())
            for(instance in instances) render(instance, environment)
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
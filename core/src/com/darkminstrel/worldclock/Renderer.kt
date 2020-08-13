package com.darkminstrel.worldclock

import com.badlogic.gdx.ApplicationAdapter
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.graphics.*
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter
import com.badlogic.gdx.graphics.g3d.*
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder
import com.badlogic.gdx.input.GestureDetector
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.utils.Align
import java.util.*
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt


class Renderer : ApplicationAdapter() {

    private lateinit var environment: Environment
    private lateinit var cam:PerspectiveCamera
    private lateinit var assetManager:AssetManager
    private lateinit var directionalLight: DirectionalLight
    private lateinit var modelBatch: ModelBatch
    private lateinit var stage: Stage

    private val mapTextures = IdentityHashMap<World.FillTexture, Texture>()
    private val mapMaterials = IdentityHashMap<World.Fill, Material>()
    private val mapModels = IdentityHashMap<World.WorldModel, Model>()
    private val instances = ArrayList<Pair<World.WorldObject, ModelInstance>>()
    private val mapLabels = IdentityHashMap<City, Label>()

    private val sunLocation = Vector2()
    private val vectorSun = Vector3()

    private var ready = false

    override fun create() {
        environment = Environment().apply {
            set(ColorAttribute(ColorAttribute.AmbientLight, Config.LIGHT_AMBIENT, Config.LIGHT_AMBIENT, Config.LIGHT_AMBIENT, 1f))
            directionalLight = DirectionalLight().set(Config.LIGHT_DIRECTIONAL, Config.LIGHT_DIRECTIONAL, Config.LIGHT_DIRECTIONAL, Config.MAX_CAMERA_DISTANCE, 0f, 0f)
            add(directionalLight)
            updateSun() //TODO periodically
        }

        stage = Stage()

        cam = PerspectiveCamera(Config.FOV, Gdx.graphics.width.toFloat(), Gdx.graphics.height.toFloat()).apply {
            near = 1f
            far = Config.MAX_CAMERA_DISTANCE
        }
        updateCamera()

        assetManager = AssetManager().apply {
            for(texture in World.FillTexture.values()) load(texture.filename, Texture::class.java)
        }

        Gdx.input.inputProcessor = GestureDetector(object: SimpleGestureListener() {
            override fun pan(x: Float, y: Float, deltaX: Float, deltaY: Float): Boolean {
                val density = Gdx.graphics.density
                World.Camera.move(deltaX/density, deltaY/density)
                updateCamera()
                return true
            }

            private var initialRadius:Float? = null
            override fun zoom(initialDistance: Float, distance: Float): Boolean {
                if(initialRadius==null) initialRadius = World.Camera.distance
                World.Camera.zoom((initialDistance/distance) * (initialRadius!!))
                updateCamera()
                return true
            }
            override fun pinchStop() {
                super.pinchStop()
                initialRadius = null
            }
            override fun tap(x: Float, y: Float, count: Int, button: Int): Boolean {
                return if(count==2){
                    //TODO
                    true
                }else{
                    false
                }
            }
        })
    }

    private fun onTexturesLoaded(){
        for(texture in World.FillTexture.values()) mapTextures[texture] = assetManager.get(texture.filename, Texture::class.java)
        World.allFills.forEach {
            mapMaterials[it] = when(it){
                is World.FillTexture -> Material(TextureAttribute.createDiffuse(mapTextures[it]))
                is World.FillColor -> Material(ColorAttribute.createDiffuse(it.color))
                else -> null
            }
        }

        val modelBuilder = ModelBuilder()
        for(model in World.allModels){
            mapModels[model] = when(model){
                is World.SphereModel -> modelBuilder.createSphere(model.radius*2, model.radius*2, model.radius*2, model.detalization, model.detalization, mapMaterials[model.fill],
                        (VertexAttributes.Usage.Position or VertexAttributes.Usage.Normal or VertexAttributes.Usage.TextureCoordinates).toLong())
                else -> null
            }
        }
        for(obj in World.allObjects) {
            val instance = ModelInstance(mapModels[obj.model])
            instance.transform.translate(obj.position)
            instances += Pair(obj, instance)
        }

        val generator = FreeTypeFontGenerator(Gdx.files.internal("golden.ttf"))
        val font: BitmapFont = generator.generateFont(FreeTypeFontParameter().also { it.size = (12f * Gdx.graphics.density).toInt() })
        generator.dispose()

        for(city in City.values()){
            val label = Label(city.getLabelText(), Label.LabelStyle(font, Color.WHITE.cpy()))
            stage.addActor(label)
            mapLabels[city] = label
        }

        modelBatch = ModelBatch()
    }

    override fun render() {
        Gdx.gl.glViewport(0, 0, Gdx.graphics.width, Gdx.graphics.height)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT or GL20.GL_DEPTH_BUFFER_BIT)

        if(!assetManager.update()) return

        if(!ready){
            ready = true
            onTexturesLoaded()
        }

        //draw models
        with(modelBatch) {
            begin(cam)
            for (instance in instances) render(instance.second, environment)
            end()
        }

        //draw stage
        drawLabels()
    }

    override fun resize(width: Int, height: Int) {
        stage.viewport.update(width, height, true)
    }

    private fun updateSun(){
        SunUtils.compute(sunLocation)
        DBG("SUN: $sunLocation")
        geoToVector(vectorSun, sunLocation.x, sunLocation.y, Config.MAX_CAMERA_DISTANCE)
        directionalLight.direction.set(0f,0f,0f).sub(vectorSun).nor()
    }

    private fun updateCamera(){
        cam.apply {
            position.set(World.Camera.vector)
            direction.set(0f, 0f, 0f).sub(World.Camera.vector).nor()
            up.set(0f,1f,0f)
            update()
        }
    }

    private val tempVector = Vector3()
    private fun drawLabels(){
        val maxDistance = sqrt(World.Camera.distance * World.Camera.distance - Config.EARTH_RADIUS * Config.EARTH_RADIUS)
        val minDistance = World.Camera.distance - Config.EARTH_RADIUS

        for((city, label) in mapLabels){
            geoToVector(tempVector, city.lat, city.long, Config.EARTH_RADIUS)
            val distance = Vector3.dst(tempVector.x, tempVector.y, tempVector.z, cam.position.x, cam.position.y, cam.position.z)
            val alpha = 1f - min(1f,max(0f, (distance-minDistance)/(maxDistance-minDistance)))

            cam.project(tempVector)
            label.setPosition(tempVector.x, tempVector.y)
            label.style.fontColor.a = alpha
            label.setText(city.getLabelText())
        }
        stage.draw()
    }

    override fun dispose() {
        assetManager.dispose()
        for(texture in mapTextures.values) texture.dispose()
        for(model in mapModels.values) model.dispose()
        modelBatch.dispose()
        stage.dispose()
    }
}
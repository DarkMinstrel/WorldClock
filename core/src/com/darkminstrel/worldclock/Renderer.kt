package com.darkminstrel.worldclock

import com.badlogic.gdx.ApplicationAdapter
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.InputAdapter
import com.badlogic.gdx.InputMultiplexer
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
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Label
import java.util.*
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt


class Renderer : ApplicationAdapter() {

    private lateinit var cam:PerspectiveCamera
    private var phi:Float = toRadians(0f)
    private var theta:Float = toRadians(30f)

    private lateinit var environment: Environment

    private lateinit var assetManager:AssetManager
    private var ready = false

    private lateinit var directionalLight: DirectionalLight
    private lateinit var modelBatch: ModelBatch
    private val mapTextures = IdentityHashMap<World.FillTexture, Texture>()
    private val mapMaterials = IdentityHashMap<World.Fill, Material>()
    private val mapModels = IdentityHashMap<World.WorldModel, Model>()
    private val instances = ArrayList<Pair<World.WorldObject, ModelInstance>>()
    private val mapLabels = IdentityHashMap<City, Label>()

    private lateinit var stage: Stage
    private lateinit var label: Label

    override fun create() {
        environment = Environment().apply {
            set(ColorAttribute(ColorAttribute.AmbientLight, Config.LIGHT_AMBIENT, Config.LIGHT_AMBIENT, Config.LIGHT_AMBIENT, 1f))
            directionalLight = DirectionalLight().set(Config.LIGHT_DIRECTIONAL, Config.LIGHT_DIRECTIONAL, Config.LIGHT_DIRECTIONAL, Config.CAMERA_DISTANCE, 0f, 0f)
            add(directionalLight)
        }

        stage = Stage()

        cam = PerspectiveCamera(Config.FOV, Gdx.graphics.width.toFloat(), Gdx.graphics.height.toFloat()).apply {
            near = 1f
            far = Config.CAMERA_DISTANCE * 2
        }
        updateCamera()

        assetManager = AssetManager().apply {
            for(texture in World.FillTexture.values()) load(texture.filename, Texture::class.java)
        }

        Gdx.input.inputProcessor = InputMultiplexer(inputAdapter)
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

        val generator = FreeTypeFontGenerator(Gdx.files.internal("tahoma.ttf"))
        val font: BitmapFont = generator.generateFont(FreeTypeFontParameter().also { it.size = 28 })
        generator.dispose()

        for(city in City.values()){
            val label = Label("  "+city.cityName, Label.LabelStyle(font, Color.WHITE.cpy())).apply {
                //setAlignment(Align.left)
            }
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

        with(modelBatch) {
            begin(cam)
            for (instance in instances) render(instance.second, environment)
            end()
        }

        drawLabels()
    }

    private val inputAdapter = object: InputAdapter() {
        private var startX = 0
        private var startY = 0
        override fun touchDown(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
            startX = screenX
            startY = screenY
            return true
        }
        override fun touchDragged(screenX: Int, screenY: Int, pointer: Int): Boolean {
            val dx = (screenX - startX).toFloat(); val dy = (screenY - startY).toFloat()
            phi -= toRadians(dx) / 3
            theta += toRadians(dy) / 3
            theta = min(toRadians(80f), max(toRadians(-80f), theta))
            startX = screenX; startY = screenY
            updateCamera()
            return false
        }
    }

    override fun resize(width: Int, height: Int) {
        stage.viewport.update(width, height, true)
    }

    private val cameraVector = Vector3()
    private fun updateCamera(){
        radiansToVector(cameraVector, theta, phi, Config.CAMERA_DISTANCE)

        directionalLight.apply {
            setDirection(-cameraVector.x,-cameraVector.y,-cameraVector.z)
        }

        cam.apply {
            position.set(cameraVector)
            direction.set(0f, 0f, 0f).sub(cam.position).nor()
            up.set(0f,1f,0f)
            update()
        }
    }

    private val tempVector = Vector3()
    private fun drawLabels(){
        val maxD = sqrt(Config.CAMERA_DISTANCE*Config.CAMERA_DISTANCE - Config.EARTH_RADIUS*Config.EARTH_RADIUS)
        val minD = Config.CAMERA_DISTANCE - Config.EARTH_RADIUS
        for((city, label) in mapLabels){
            geoToVector(tempVector, city.lat, city.long, Config.EARTH_RADIUS)
            val distance = Vector3.dst(tempVector.x, tempVector.y, tempVector.z, cam.position.x, cam.position.y, cam.position.z)
            val alpha = 1f - min(1f,max(0f, (distance-minD)/(maxD-minD)))

            cam.project(tempVector)
            label.setPosition(tempVector.x, tempVector.y)
            label.style.fontColor.a = alpha
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
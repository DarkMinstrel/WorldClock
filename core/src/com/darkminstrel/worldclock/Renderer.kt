package com.darkminstrel.worldclock

import com.badlogic.gdx.ApplicationAdapter
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.GL20

class Renderer : ApplicationAdapter() {

    private lateinit var scene: IScene

    override fun create() {
        scene = Scene()
    }

    override fun render() {
        Gdx.gl.glViewport(0, 0, Gdx.graphics.width, Gdx.graphics.height)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT or GL20.GL_DEPTH_BUFFER_BIT)

        scene.render()
    }

    override fun dispose() {
        scene.dispose()
    }

}
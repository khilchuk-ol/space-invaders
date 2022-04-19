package com.example.space_invaders.models

import android.graphics.RectF

class Bullet(screenY: Int,
             private val speed: Float = 350f,
             heightModifier: Float = 20f) {

    val position = RectF()

    val up = 0
    val down = 1

    private var heading = -1

    private val width = 2
    private var height = screenY / heightModifier

    var isActive = false

    fun shoot(startX: Float, startY: Float, direction: Int): Boolean {
        if (!isActive) {
            position.left = startX
            position.top = startY
            position.right = position.left + width
            position.bottom = position.top + height
            heading = direction
            isActive = true
            return true
        }

        return false
    }

    fun update(fps: Long) {

        if (heading == up) {
            position.top -= speed / fps
        } else {
            position.top += speed / fps
        }

        position.bottom = position.top + height
    }
}
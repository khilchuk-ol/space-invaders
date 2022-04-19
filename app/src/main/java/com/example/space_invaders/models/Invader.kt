package com.example.space_invaders.models

import android.content.Context
import android.graphics.Bitmap
import android.graphics.RectF
import java.util.*
import android.graphics.BitmapFactory

class Invader(context: Context, row: Int, column: Int, screenX: Int, screenY: Int) {
    var width = screenX / 35f
    private var height = screenY / 35f
    private val padding = screenX / 45

    var position = RectF(
        column * (width + padding),
        100 + row * (width + padding / 4),
        column * (width + padding) + width,
        100 + row * (width + padding / 4) + height
    )

    private var speed = 40f

    private val left = 1
    private val right = 2

    private var shipMoving = right

    var isVisible = true

    companion object {
        lateinit var bitmap1: Bitmap
        lateinit var bitmap2: Bitmap

        var numberOfInvaders = 0
    }

    init {
        bitmap1 = BitmapFactory.decodeResource(
            context.resources,
            R.drawable.invader1
        )

        bitmap2 = BitmapFactory.decodeResource(
            context.resources,
            R.drawable.invader2
        )

        bitmap1 = Bitmap.createScaledBitmap(
            bitmap1,
            (width.toInt()),
            (height.toInt()),
            false
        )

        bitmap2 = Bitmap.createScaledBitmap(
            bitmap2,
            (width.toInt()),
            (height.toInt()),
            false
        )

        numberOfInvaders++
    }

    fun update(fps: Long) {
        if (shipMoving == left) {
            position.left -= speed / fps
        }

        if (shipMoving == right) {
            position.left += speed / fps
        }

        position.right = position.left + width
    }

    fun dropDownAndReverse(waveNumber: Int) {
        shipMoving = if (shipMoving == left) {
            right
        } else {
            left
        }

        position.top += height
        position.bottom += height

        speed *= (1.1f + (waveNumber.toFloat() / 20))
    }

    fun takeAim(
        playerShipX: Float,
        playerShipLength: Float,
        waves: Int
    )
            : Boolean {

        val generator = Random()
        var randomNumber: Int

        if (playerShipX + playerShipLength > position.left &&
            playerShipX + playerShipLength < position.left + width ||
            playerShipX > position.left && playerShipX < position.left + width
        ) {

            randomNumber = generator.nextInt((100 * numberOfInvaders) / waves)
            if (randomNumber == 0) {
                return true
            }

        }

        randomNumber = generator.nextInt(150 * numberOfInvaders)
        return randomNumber == 0

    }
}

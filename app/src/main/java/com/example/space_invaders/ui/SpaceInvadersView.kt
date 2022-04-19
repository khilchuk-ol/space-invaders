package com.example.space_invaders.ui

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.graphics.*
import android.util.Log
import android.view.MotionEvent
import android.view.SurfaceView
import com.example.space_invaders.models.*

@SuppressLint("ViewConstructor")
class SpaceInvadersView(
    context: Context,
    private val size: Point
) : SurfaceView(context),
    Runnable {

    private val soundPlayer = SoundPlayer(context)

    private val gameThread = Thread(this)

    private var playing = false

    private var paused = true

    private var canvas: Canvas = Canvas()
    private val paint: Paint = Paint()

    private var playerShip: PlayerShip = PlayerShip(context, size.x, size.y)

    private val invaders = ArrayList<Invader>()
    private var numInvaders = 0

    private val bricks = ArrayList<DefenceBrick>()
    private var numBricks: Int = 0

    private var playerBullet = Bullet(size.y, 1200f, 40f)

    private val invadersBullets = ArrayList<Bullet>()
    private var nextBullet = 0
    private val maxInvaderBullets = 10

    private var score = 0

    private var waves = 1

    private var lives = 3

    private val prefs: SharedPreferences = context.getSharedPreferences(
        "Space Invaders",
        Context.MODE_PRIVATE
    )

    private var highScore = prefs.getInt("highScore", 0)

    private var menaceInterval: Long = 1000

    private var uhOrOh: Boolean = false

    private var lastMenaceTime = System.currentTimeMillis()

    private fun prepareLevel() {
        Invader.numberOfInvaders = 0
        numInvaders = 0
        for (column in 0..10) {
            for (row in 0..5) {
                invaders.add(
                    Invader(
                        context,
                        row,
                        column,
                        size.x,
                        size.y
                    )
                )

                numInvaders++
            }
        }

        numBricks = 0
        for (shelterNumber in 0..4) {
            for (column in 0..18) {
                for (row in 0..8) {
                    bricks.add(
                        DefenceBrick(
                            row,
                            column,
                            shelterNumber,
                            size.x,
                            size.y
                        )
                    )

                    numBricks++
                }
            }
        }

        for (i in 0 until maxInvaderBullets) {
            invadersBullets.add(Bullet(size.y))
        }
    }

    override fun run() {
        var fps: Long = 0

        while (playing) {

            val startFrameTime = System.currentTimeMillis()

            if (!paused) {
                update(fps)
            }

            draw()

            val timeThisFrame = System.currentTimeMillis() - startFrameTime
            if (timeThisFrame >= 1) {
                fps = 1000 / timeThisFrame
            }

            if (!paused && ((startFrameTime - lastMenaceTime) > menaceInterval))
                menacePlayer()
        }
    }

    private fun menacePlayer() {
        if (uhOrOh) {
            soundPlayer.playSound(SoundPlayer.uhID)

        } else {
            soundPlayer.playSound(SoundPlayer.ohID)
        }

        lastMenaceTime = System.currentTimeMillis()
        uhOrOh = !uhOrOh

    }

    private fun update(fps: Long) {
        playerShip.update(fps)

        var bumped = false

        var lost = false

        for (invader in invaders) {

            if (invader.isVisible) {
                invader.update(fps)

                if (invader.takeAim(
                        playerShip.position.left,
                        playerShip.width,
                        waves
                    )
                ) {

                    if (invadersBullets[nextBullet].shoot(
                            invader.position.left
                                    + invader.width / 2,
                            invader.position.top, playerBullet.down
                        )
                    ) {

                        nextBullet++

                        if (nextBullet == maxInvaderBullets) {
                            nextBullet = 0
                        }
                    }
                }

                if (invader.position.left > size.x - invader.width
                    || invader.position.left < 0
                ) {
                    bumped = true
                }
            }
        }

        if (playerBullet.isActive) {
            playerBullet.update(fps)
        }

        for (bullet in invadersBullets) {
            if (bullet.isActive) {
                bullet.update(fps)
            }
        }

        if (bumped) {

            for (invader in invaders) {
                invader.dropDownAndReverse(waves)
                if (invader.position.bottom >= size.y && invader.isVisible) {
                    lost = true
                }
            }
        }

        if (playerBullet.position.bottom < 0) {
            playerBullet.isActive = false
        }

        for (bullet in invadersBullets) {
            if (bullet.position.top > size.y) {
                bullet.isActive = false
            }
        }

        if (playerBullet.isActive) {
            for (invader in invaders) {
                if (invader.isVisible) {
                    if (RectF.intersects(playerBullet.position, invader.position)) {
                        invader.isVisible = false

                        soundPlayer.playSound(SoundPlayer.invaderExplodeID)
                        playerBullet.isActive = false
                        Invader.numberOfInvaders--
                        score += 10
                        if (score > highScore) {
                            highScore = score
                        }

                        if (Invader.numberOfInvaders == 0) {
                            paused = true
                            lives++
                            invaders.clear()
                            bricks.clear()
                            invadersBullets.clear()
                            prepareLevel()
                            waves++
                            break
                        }

                        break
                    }
                }
            }
        }

        for (bullet in invadersBullets) {
            if (bullet.isActive) {
                for (brick in bricks) {
                    if (brick.isVisible) {
                        if (RectF.intersects(bullet.position, brick.position)) {
                            bullet.isActive = false
                            brick.isVisible = false
                            soundPlayer.playSound(SoundPlayer.damageShelterID)
                        }
                    }
                }
            }

        }

        if (playerBullet.isActive) {
            for (brick in bricks) {
                if (brick.isVisible) {
                    if (RectF.intersects(playerBullet.position, brick.position)) {
                        playerBullet.isActive = false
                        brick.isVisible = false
                        soundPlayer.playSound(SoundPlayer.damageShelterID)
                    }
                }
            }
        }

        for (bullet in invadersBullets) {
            if (bullet.isActive) {
                if (RectF.intersects(playerShip.position, bullet.position)) {
                    bullet.isActive = false
                    lives--
                    soundPlayer.playSound(SoundPlayer.playerExplodeID)

                    if (lives == 0) {
                        lost = true
                        break
                    }
                }
            }
        }

        if (lost) {
            paused = true
            lives = 3
            score = 0
            waves = 1
            invaders.clear()
            bricks.clear()
            invadersBullets.clear()
            prepareLevel()
        }
    }

    private fun draw() {
        if (holder.surface.isValid) {
            canvas = holder.lockCanvas()

            canvas.drawColor(Color.argb(255, 0, 0, 0))

            paint.color = Color.argb(255, 0, 255, 0)

            canvas.drawBitmap(
                playerShip.bitmap, playerShip.position.left,
                playerShip.position.top, paint
            )

            for (invader in invaders) {
                if (invader.isVisible) {
                    if (uhOrOh) {
                        canvas.drawBitmap(
                            Invader.bitmap1,
                            invader.position.left,
                            invader.position.top,
                            paint
                        )
                    } else {
                        canvas.drawBitmap(
                            Invader.bitmap2,
                            invader.position.left,
                            invader.position.top,
                            paint
                        )
                    }
                }
            }

            for (brick in bricks) {
                if (brick.isVisible) {
                    canvas.drawRect(brick.position, paint)
                }
            }

            if (playerBullet.isActive) {
                canvas.drawRect(playerBullet.position, paint)
            }

            for (bullet in invadersBullets) {
                if (bullet.isActive) {
                    canvas.drawRect(bullet.position, paint)
                }
            }

            paint.color = Color.argb(255, 255, 255, 255)
            paint.textSize = 70f
            canvas.drawText(
                "Score: $score   Lives: $lives Wave: " +
                        "$waves HI: $highScore", 20f, 75f, paint
            )

            holder.unlockCanvasAndPost(canvas)
        }
    }

    fun pause() {
        playing = false
        try {
            gameThread.join()
        } catch (e: InterruptedException) {
            Log.e("Error:", "joining thread")
        }

        val prefs = context.getSharedPreferences(
            "Space Invaders",
            Context.MODE_PRIVATE
        )

        val oldHighScore = prefs.getInt("highScore", 0)

        if (highScore > oldHighScore) {
            val editor = prefs.edit()

            editor.putInt(
                "highScore", highScore
            )

            editor.apply()
        }
    }

    fun resume() {
        playing = true
        prepareLevel()
        gameThread.start()
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(motionEvent: MotionEvent): Boolean {
        val motionArea = size.y - (size.y / 8)
        when (motionEvent.action and MotionEvent.ACTION_MASK) {

            MotionEvent.ACTION_POINTER_DOWN,
            MotionEvent.ACTION_DOWN,
            MotionEvent.ACTION_MOVE -> {
                paused = false

                if (motionEvent.y > motionArea) {
                    if (motionEvent.x > size.x / 2) {
                        playerShip.moving = PlayerShip.right
                    } else {
                        playerShip.moving = PlayerShip.left
                    }

                }

                if (motionEvent.y < motionArea) {
                    // Shots fired
                    if (playerBullet.shoot(
                            playerShip.position.left + playerShip.width / 2f,
                            playerShip.position.top,
                            playerBullet.up
                        )
                    ) {

                        soundPlayer.playSound(SoundPlayer.shootID)
                    }
                }
            }

            // Player has removed finger from screen
            MotionEvent.ACTION_POINTER_UP,
            MotionEvent.ACTION_UP -> {
                if (motionEvent.y > motionArea) {
                    playerShip.moving = PlayerShip.stopped
                }
            }

        }
        return true
    }

}

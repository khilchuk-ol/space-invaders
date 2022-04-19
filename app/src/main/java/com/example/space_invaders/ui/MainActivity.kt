package com.example.space_invaders.ui

import android.app.Activity
import android.graphics.Point
import android.os.Bundle
import androidx.window.layout.WindowMetricsCalculator

class MainActivity : Activity() {

    private var invadersView: SpaceInvadersView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val windowMetrics = WindowMetricsCalculator.getOrCreate().computeCurrentWindowMetrics(this)
        val currentBounds = windowMetrics.bounds
        val width = currentBounds.width()
        val height = currentBounds.height()

        val size = Point(width, height)

        invadersView = SpaceInvadersView(this, size)
        setContentView(invadersView)
    }

    override fun onResume() {
        super.onResume()

        invadersView?.resume()
    }

    override fun onPause() {
        super.onPause()

        invadersView?.pause()
    }
}

package com.example.cargame

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import kotlin.random.Random

class MainActivity : AppCompatActivity() {

    private lateinit var btnLeft: ImageButton
    private lateinit var btnRight: ImageButton

    private lateinit var life1: ImageView
    private lateinit var life2: ImageView
    private lateinit var life3: ImageView

    private lateinit var car0: ImageView
    private lateinit var car1: ImageView
    private lateinit var car2: ImageView
    private lateinit var cars: Array<ImageView>

    private lateinit var viewsArray: Array<Array<ImageView>>
    private val lMatrix = Array(5) { IntArray(3) { 0 } }

    private var lane = 0        // -1 left, 0 mid, 1 right
    private var lives = 3
    private var counter = 0

    private val rockHandler = Handler(Looper.getMainLooper())
    private val rockDelay = 700L
    private var timerOn = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        btnLeft = findViewById(R.id.btnLeft)
        btnRight = findViewById(R.id.btnRight)

        life1 = findViewById(R.id.life1)
        life2 = findViewById(R.id.life2)
        life3 = findViewById(R.id.life3)

        car0 = findViewById(R.id.car0)
        car1 = findViewById(R.id.car1)
        car2 = findViewById(R.id.car2)
        cars = arrayOf(car0, car1, car2)

        viewsArray = arrayOf(
            arrayOf(findViewById(R.id.rock00), findViewById(R.id.rock01), findViewById(R.id.rock02)),
            arrayOf(findViewById(R.id.rock10), findViewById(R.id.rock11), findViewById(R.id.rock12)),
            arrayOf(findViewById(R.id.rock20), findViewById(R.id.rock21), findViewById(R.id.rock22)),
            arrayOf(findViewById(R.id.rock30), findViewById(R.id.rock31), findViewById(R.id.rock32)),
            arrayOf(findViewById(R.id.rock40), findViewById(R.id.rock41), findViewById(R.id.rock42))
        )

        resetGame()

        btnLeft.setOnClickListener {
            lane = moveCar(-1, lane)
            showCar()
            checkCollision()
        }

        btnRight.setOnClickListener {
            lane = moveCar(1, lane)
            showCar()
            checkCollision()
        }

        startRocks()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    override fun onStop() {
        super.onStop()
        stopRocks()
    }

    private fun startRocks() {
        if (timerOn) return
        timerOn = true
        rockHandler.postDelayed(rockRunnable, rockDelay)
    }

    private fun stopRocks() {
        timerOn = false
        rockHandler.removeCallbacks(rockRunnable)
    }

    private val rockRunnable = object : Runnable {
        override fun run() {
            if (!timerOn) return
            moveRocks()
            rockHandler.postDelayed(this, rockDelay)
        }
    }

    private fun resetGame() {
        lives = 3
        counter = 0
        lane = 0

        for (i in 0..4) {
            for (j in 0..2) {
                lMatrix[i][j] = 0
            }
        }

        lives()
        showCar()
        drawRocks()
    }

    private fun moveRocks() {
        for (i in 4 downTo 1) {
            for (j in 0..2) {
                lMatrix[i][j] = lMatrix[i - 1][j]
            }
        }

        for (j in 0..2) {
            lMatrix[0][j] = 0
        }

        if (counter % 2 == 0) {
            SpawnRock()
        }
        counter++

        drawRocks()
        checkCollision()
    }

    private fun SpawnRock() {
        val r = Random.nextInt(3)
        lMatrix[0][r] = 1
    }

    private fun drawRocks() {
        for (i in 0..4) {
            for (j in 0..2) {
                viewsArray[i][j].visibility =
                    if (lMatrix[i][j] == 1) View.VISIBLE else View.INVISIBLE
            }
        }
    }

    private fun checkCollision() {
        val laneIndex = lane + 1
        if (lMatrix[4][laneIndex] == 1) {
            lMatrix[4][laneIndex] = 0
            lives--

            vibrate()
            lives()
            drawRocks()

            if (lives <= 0) {
                gameOver()
            }
        }
    }

    private fun gameOver() {
        stopRocks()
        Toast.makeText(this, "Game Over", Toast.LENGTH_SHORT).show()

        rockHandler.postDelayed({
            resetGame()
            startRocks()
        }, 500)
    }

    private fun showCar() {
        val idx = lane + 1
        for (i in 0..2) {
            cars[i].visibility = if (i == idx) View.VISIBLE else View.INVISIBLE
        }
    }

    private fun lives() {
        life1.visibility = if (lives >= 1) View.VISIBLE else View.INVISIBLE
        life2.visibility = if (lives >= 2) View.VISIBLE else View.INVISIBLE
        life3.visibility = if (lives >= 3) View.VISIBLE else View.INVISIBLE
    }

    private fun moveCar(input: Int, lane: Int): Int {
        var newLane = lane + input
        if (newLane < -1) newLane = -1
        if (newLane > 1) newLane = 1
        return newLane
    }

    private fun vibrate() {
        val v = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        if (!v.hasVibrator()) return

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            v.vibrate(VibrationEffect.createOneShot(200, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            v.vibrate(200)
        }
    }
}

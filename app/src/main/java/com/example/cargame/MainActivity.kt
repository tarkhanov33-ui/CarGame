package com.example.cargame

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import kotlin.random.Random
import android.os.VibrationEffect;
import android.os.Vibrator;
class MainActivity : AppCompatActivity() {

    private lateinit var car: ImageView
    private lateinit var btnLeft: ImageButton
    private lateinit var btnRight: ImageButton
    private lateinit var livesLayout: LinearLayout
    private lateinit var life1: ImageView
    private lateinit var life2: ImageView
    private lateinit var life3: ImageView

    private lateinit var RockLeft: ImageView
    private lateinit var RockMid: ImageView
    private lateinit var RockRight: ImageView

    private var lane = 0
    private var lives = 3

    private lateinit var laneCenters: FloatArray

    private lateinit var rockViews: Array<ImageView>
    private lateinit var rockY: FloatArray
    private lateinit var rockActive: BooleanArray

    private val rockHandler = Handler(Looper.getMainLooper())
    private val rockStep = 25f
    private val rockDelay = 50L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        car = findViewById(R.id.car)
        btnLeft = findViewById(R.id.btnLeft)
        btnRight = findViewById(R.id.btnRight)
        livesLayout = findViewById(R.id.livesLayout)
        life1 = findViewById(R.id.life1)
        life2 = findViewById(R.id.life2)
        life3 = findViewById(R.id.life3)

        RockLeft = findViewById(R.id.RockLeft)
        RockMid = findViewById(R.id.RockMid)
        RockRight = findViewById(R.id.RockRight)

        laneCenters = FloatArray(3)

        rockViews = arrayOf(RockLeft, RockMid, RockRight)
        rockY = FloatArray(3) { 0f }
        rockActive = BooleanArray(3) { false }

        lives()

        car.post {
            laneCenters[0] = RockLeft.x + RockLeft.width / 2f
            laneCenters[1] = RockMid.x + RockMid.width / 2f
            laneCenters[2] = RockRight.x + RockRight.width / 2f

            car.x = newCarPosition(laneCenters[lane + 1], car.width)

            startRocks()
        }

        btnLeft.setOnClickListener {
            lane = moveCar(-1, lane)
            car.x = newCarPosition(laneCenters[lane + 1], car.width)
        }

        btnRight.setOnClickListener {
            lane = moveCar(1, lane)
            car.x = newCarPosition(laneCenters[lane + 1], car.width)
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun startRocks() {
        rockHandler.post(object : Runnable {
            override fun run() {
                moveRocks()
                rockHandler.postDelayed(this, rockDelay)
            }
        })
    }

    private fun SpawnRock(laneIndex: Int) {
        if (rockActive[laneIndex]) return

        if (Random.nextFloat() < 0.02f) {
            val rock = rockViews[laneIndex]
            rockActive[laneIndex] = true
            rockY[laneIndex] = -rock.height.toFloat()
            rock.y = rockY[laneIndex]
            rock.visibility = View.VISIBLE
        }
    }

    private fun moveRocks() {
        for (i in 0..2) {
            if (!rockActive[i]) {
               SpawnRock(i)
                continue
            }

            val rock = rockViews[i]

            rockY[i] += rockStep
            rock.y = rockY[i]

            if (rockY[i] > car.y - rock.height) {
                checkCollision(i)
                rock.visibility = View.INVISIBLE
                rockActive[i] = false
            }
        }
    }

    private fun checkCollision(rockIndex: Int) {
        val rockLane = rockIndex - 1  // 0→-1, 1→0, 2→+1
        if (rockLane == lane) {
            lives--

            if (lives <= 0) {
                lives = 3
                Toast.makeText(this, "Game Over", Toast.LENGTH_SHORT).show()
            }
            vibrate()
            lives()
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

    private fun newCarPosition(laneCenter: Float, carWidth: Int): Float {
        return laneCenter - carWidth / 2f
    }
    private fun vibrate() {
        val v = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            v.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            v.vibrate(500)
        }
    }


}

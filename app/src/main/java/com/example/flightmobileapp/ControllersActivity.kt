package com.example.flightmobileapp

import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.SeekBar
import android.widget.TextView
import androidx.annotation.RequiresApi
import io.github.controlwear.virtual.joystick.android.JoystickView
import kotlinx.android.synthetic.main.activity_controllers.*
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

class ControllersActivity : AppCompatActivity() {

    private var elevator = 0.0;
    private var aileron = 0.0;
    private var rudder = 0.0;
    private var throttle = 0.0;


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_controllers)

        setBarListeners()
        setJoystickListeners()
    }

    private fun setJoystickListeners() {
        joystick.setOnMoveListener(JoystickView.OnMoveListener() { angle: Int, strength: Int ->
            val x = strength * cos(Math.toRadians(angle * 1.0))
            val y = strength * sin(Math.toRadians(angle * 1.0))
            val roundedX = x.roundToInt() / 100.0
            val roundedY = y.roundToInt() / 100.0
            aileronValue.text = roundedX.toString()
            elevatorValue.text = roundedY.toString()
            if(changedEnough200(roundedX, aileron) || changedEnough200(roundedY, elevator)) {
                aileron = roundedX
                elevator = roundedY
                println("Changed $aileron")
                println("and $elevator")
                //send();
            }
        })
    }

    private fun setBarListeners() {
        throttle_bar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, i: Int, b: Boolean) {
                throttleValue.text = (i/100.0).toString()
                if(changedEnough100(i/100.0, throttle)) {
                    throttle = i/100.0
                    // send()
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
                //TODO("Not yet implemented")
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                //TODO("Not yet implemented")
            }
        })

        rudder_bar.max = 200
        rudder_bar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, i: Int, b: Boolean) {
                if(i < 100){
                    rudderValue.text = ((i / 100.0) - 1).toString()
                    if(changedEnough100((i / 100.0) - 1, throttle)) {
                        rudder = (i / 100.0) - 1
                        // send()
                    }
                } else {
                    rudderValue.text = ((i - 100.0)/100).toString()
                    if(changedEnough100((i - 100.0)/100, throttle)) {
                        rudder = (i - 100.0)/100
                        // send()
                    }
                }
            }

            override fun onStartTrackingTouch(p0: SeekBar) {
                //TODO("Not yet implemented")
            }

            override fun onStopTrackingTouch(p0: SeekBar) {
                //TODO("Not yet implemented")
            }
        })

    }

    private fun changedEnough200(newValue: Double, prevValue: Double) : Boolean {
        if((newValue > prevValue) && (newValue-prevValue) >= 0.02){
            return true
        } else if((newValue < prevValue) && (prevValue-newValue) >= 0.02) {
            return true
        }
        return false
    }

    private fun changedEnough100(newValue: Double, prevValue: Double) : Boolean {
        if((newValue > prevValue) && (newValue-prevValue) >= 0.01){
            return true
        } else if((newValue < prevValue) && (prevValue-newValue) >= 0.01) {
            return true
        }
        return false
    }
}
package com.example.flightmobileapp

import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.SeekBar
import android.widget.TextView
import androidx.annotation.RequiresApi
import com.google.gson.GsonBuilder
import io.github.controlwear.virtual.joystick.android.JoystickView
import kotlinx.android.synthetic.main.activity_controllers.*
import okhttp3.MediaType
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin
//import kotlinx.coroutines.CoroutineScope
//import kotlinx.coroutines.Dispatchers.IO

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
        sendValues()
        loadImageLoop()
    }

    private fun setJoystickListeners() {
        joystick.setOnMoveListener(JoystickView.OnMoveListener() { angle: Int, strength: Int ->
            val x = strength * cos(Math.toRadians(angle * 1.0))
            val y = strength * sin(Math.toRadians(angle * 1.0))
            val roundedX = x.roundToInt() / 100.0
            val roundedY = y.roundToInt() / 100.0
            aileronValue.text = roundedX.toString()
            elevatorValue.text = roundedY.toString()
            if (changedEnough200(roundedX, aileron) || changedEnough200(roundedY, elevator)) {
                aileron = roundedX
                elevator = roundedY
                println("Changed $aileron")
                println("and $elevator")
                sendValues()
            }
        })
    }

    private fun setBarListeners() {
        throttle_bar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, i: Int, b: Boolean) {
                throttleValue.text = (i / 100.0).toString()
                if (changedEnough100(i / 100.0, throttle)) {
                    throttle = i / 100.0
                    sendValues()
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
                //TODO("Not yet implemented")
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                //TODO("Not yet implemented")
            }
        })

        // set a listener to the rudder slider
        rudder_bar.max = 200
        rudder_bar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, i: Int, b: Boolean) {
                if (i < 100) {
                    rudderValue.text = ((i / 100.0) - 1).toString()
                    if (changedEnough100((i / 100.0) - 1, throttle)) {
                        rudder = (i / 100.0) - 1

                        //CoroutineScope(IO).launch { sendValues() }
                        sendValues()
                    }
                } else {
                    rudderValue.text = ((i - 100.0) / 100).toString()
                    if (changedEnough100((i - 100.0) / 100, throttle)) {
                        rudder = (i - 100.0) / 100
                        sendValues()
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
    // Check if 1% of the value has changed. when 1% = 0.02.
    private fun changedEnough200(newValue: Double, prevValue: Double): Boolean {
        if ((newValue > prevValue) && (newValue - prevValue) >= 0.02) {
            return true
        } else if ((newValue < prevValue) && (prevValue - newValue) >= 0.02) {
            return true
        }
        return false
    }
    // Check if 1% of the value has changed. when 1% = 0.01.
    private fun changedEnough100(newValue: Double, prevValue: Double): Boolean {
        if ((newValue > prevValue) && (newValue - prevValue) >= 0.01) {
            return true
        } else if ((newValue < prevValue) && (prevValue - newValue) >= 0.01) {
            return true
        }
        return false
    }


    /*
    A function create to build a retrofit object to make GET request for image
    from the server.
     */

    private fun loadImageLoop() {
        val url = "http://10.0.2.2:5550/"
        val gson = GsonBuilder().setLenient().create()
        val retrofit =  Retrofit.Builder().baseUrl(url)
            .addConverterFactory(GsonConverterFactory.create(gson)).build()
        val api = retrofit.create(GetImageService::class.java)

        val body = api.getImg().enqueue(object: Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if(response.isSuccessful) {
                    println("OK")
                } else {
                    println("NOTOK")
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                t.printStackTrace();
                println("Not yet implemented")
            }
        })
    }

    private fun sendValues() {
        val json =  "{\"aileron\": $aileron,\n \"rudder\": $rudder,\n \"elevator\": $elevator,\n \"throttle\": $throttle\n}"
        val rb = RequestBody.create(MediaType.parse("application/json"), json)
        val gson = GsonBuilder().setLenient().create()
        val retrofit = Retrofit.Builder()
            .baseUrl(("http://10.0.2.2:5550/"))
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
        val api = retrofit.create(GetImageService::class.java)
        val body = api.postCommand(rb).enqueue(object : Callback<ResponseBody> {
            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
            }
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                try {
                    Log.d("FlightMobileApp", response.body().toString())
                    println("make the update correctly")

                }
                catch (e: IOException) {
                    e.printStackTrace()
                    println("failed to make any post: catch")
                }
            }
        })
    }

}
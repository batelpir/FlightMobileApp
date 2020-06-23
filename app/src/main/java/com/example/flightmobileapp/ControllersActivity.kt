package com.example.flightmobileapp

import android.graphics.BitmapFactory
//import android.icu.util.TimeUnit
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.GsonBuilder
import io.github.controlwear.virtual.joystick.android.JoystickView
import kotlinx.android.synthetic.main.activity_controllers.*
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import okhttp3.MediaType
import okhttp3.OkHttpClient
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


class ControllersActivity : AppCompatActivity() {

    private var elevator = 0.0;
    private var aileron = 0.0;
    private var rudder = 0.0;
    private var throttle = 0.0;

    private var imageChanged = false
    private var url: String = ""

    val handler: CoroutineExceptionHandler = CoroutineExceptionHandler { _, exception ->
        Log.v("Network", "Caught $exception")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_controllers)

        val extras = intent.extras
        if (extras != null) {
            url = extras!!.getString("url").toString()
        }

        setBarListeners()
        setJoystickListeners()
        loadImageLoop()
    }

    /**
     * Find and update the values of 'aileron' and 'elevator'.
     */
    private fun setJoystickListeners() {
        joystick.setOnMoveListener(JoystickView.OnMoveListener() { angle: Int, strength: Int ->
            val x = strength * cos(Math.toRadians(angle * 1.0))
            val y = strength * sin(Math.toRadians(angle * 1.0))
            val roundedX = x.roundToInt() / 100.0
            val roundedY = y.roundToInt() / 100.0
            /** Displays the found values. */
            aileronValue.text = roundedX.toString()
            elevatorValue.text = roundedY.toString()
            if (changedEnough200(roundedX, aileron) || changedEnough200(roundedY, elevator)) {
                aileron = roundedX
                elevator = roundedY

                /** Update the server that a value has changed. */
                CoroutineScope(IO).launch { sendValues() }
            }
        })
    }

    /**
     *  Find and update the values of 'rudder' and 'throttle'.
     */
    private fun setBarListeners() {
        setThrottleBarListeners()
        setRudderBarListeners()
    }

    /**
     *  Find and update the value of 'rudder'.
     */
    private fun setRudderBarListeners() {
        /** Set a listener to the rudder slider. */
        rudder_bar.max = 200
        rudder_bar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, i: Int, b: Boolean) {
                var rudderVal: Double
                if (i < 100) {
                    rudderValue.text = ((i / 100.0) - 1).toString()
                    rudderVal = (i / 100.0) - 1
                } else {
                    rudderValue.text = ((i - 100.0) / 100).toString()
                    rudderVal = (i - 100.0) / 100
                }
                if (changedEnough100(rudderVal, rudder)) {
                    rudder = rudderVal

                    /** Update the server that a value has changed. */
                    CoroutineScope(IO).launch { sendValues() }
                }
            }

            override fun onStartTrackingTouch(p0: SeekBar) {
            }

            override fun onStopTrackingTouch(p0: SeekBar) {
            }
        })
    }

    /**
     *  Find and update the value of 'throttle'.
     */
    private fun setThrottleBarListeners() {
        /** set a listener to the throttle slider. */
        throttle_bar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, i: Int, b: Boolean) {
                throttleValue.text = (i / 100.0).toString()
                if (changedEnough100(i / 100.0, throttle)) {
                    throttle = i / 100.0

                    /** Update the server that a value has changed. */
                    CoroutineScope(IO).launch { sendValues() }
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
            }
        })
    }

    /**
     *  Check if 1% of the value has changed. when 1% = 0.02.
     */
    private fun changedEnough200(newValue: Double, prevValue: Double): Boolean {
        if ((newValue > prevValue) && (newValue - prevValue) >= 0.02) {
            return true
        } else if ((newValue < prevValue) && (prevValue - newValue) >= 0.02) {
            return true
        }
        return false
    }

    /**
     * Check if 1% of the value has changed. when 1% = 0.01.
     */
    private fun changedEnough100(newValue: Double, prevValue: Double): Boolean {
        if ((newValue > prevValue) && (newValue - prevValue) >= 0.01) {
            return true
        } else if ((newValue < prevValue) && (prevValue - newValue) >= 0.01) {
            return true
        }
        return false
    }


    /**
     * A function create to build a retrofit object to make GET request for image
     * from the server.
     */
    private fun loadImage() {
        val client = OkHttpClient.Builder()
            .connectTimeout(100, java.util.concurrent.TimeUnit.SECONDS)
            .readTimeout(100, java.util.concurrent.TimeUnit.SECONDS).build()
        val gson = GsonBuilder().setLenient().create()
        val retrofit = Retrofit.Builder().baseUrl(url)
            .addConverterFactory(GsonConverterFactory.create(gson)).build()
        val api = retrofit.create(Api::class.java)
        val body = api.getImg().enqueue(object : Callback<ResponseBody> {
            /** In success. */
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    println("OK")
                    val inputstream = response?.body()?.byteStream()
                    val bitmap = BitmapFactory.decodeStream(inputstream)
                    runOnUiThread {
                        val imageView = findViewById<ImageView>(R.id.imageView)
                        imageView.setImageBitmap(bitmap)
                        println("debug:got image successfully")
                    }
                } else
                    println("NOTOK")
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Toast.makeText(
                    applicationContext, "Failed to load new screen",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

    /**
     * Loop which ask to load an image in every iteration.
     */
    private fun loadImageLoop() {
        imageChanged = true
        CoroutineScope(IO).launch(handler) {
            while (imageChanged) {
                loadImage()
                delay(2000)
            }
        }
    }

    override fun onStart() {
        super.onStart()
        if (!imageChanged) {
            loadImage()
        }
    }

    override fun onResume() {
        super.onResume()
        if (!imageChanged) {
            loadImage()
        }
    }

    override fun onPause() {
        super.onPause()
        if (imageChanged) {
            imageChanged = false
        }
    }

    /**
     * Make and send a json that contain 4 values to the server
     */
    private fun sendValues() {
        val json =
            "{\"aileron\": $aileron,\n \"rudder\": $rudder,\n \"elevator\": $elevator,\n \"throttle\": $throttle\n}"
        val rb = RequestBody.create(MediaType.parse("application/json"), json)
        val gson = GsonBuilder().setLenient().create()
        val retrofit = Retrofit.Builder().baseUrl((url))
            .addConverterFactory(GsonConverterFactory.create(gson)).build()
        val api = retrofit.create(Api::class.java)
        val body = api.postCommand(rb).enqueue(object : Callback<ResponseBody> {
            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                if (!t.toString().contains("timeout")) {
                    t.printStackTrace()
                    Toast.makeText(
                        applicationContext, "Failed to send values",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            /** In success. */
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                try {
                    Log.d("FlightMobileApp", response.body().toString())
                    println("make the update correctly")

                } catch (e: IOException) {
                    e.printStackTrace()
                    println("failed to make any post: catch")

                }
            }
        })
    }

}
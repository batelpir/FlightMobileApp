package com.example.flightmobileapp

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.time.LocalDateTime
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var urlViewModel: UrlViewModel

    /** Init all the necessary components. */
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerview)
        val adapter = UrlListAdapter(this)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)
        urlViewModel = ViewModelProvider(
            this,
            UrlViewModelFactory(this.application)
        ).get(
            UrlViewModel::class.java
        )
        /** Set observer for urls list. */
        urlViewModel.allUrls.observe(this, Observer { urls ->
            urls?.let { adapter.setUrls(it) }
        })
    }

    /** Copy the clicked url from the list to the url input text. */
    public fun insertUrl(view: View) {
        val str = (view as TextView).text.toString()
        val urlInputText = findViewById<EditText>(R.id.urlInput)
        urlInputText.text = Editable.Factory.getInstance().newEditable(str)
    }

    /** Insert the url to the db and try to connect to the server. */
    @RequiresApi(Build.VERSION_CODES.O)
    public fun connectButton(view: View) {
        val url = findViewById<EditText>(R.id.urlInput).text.toString()
        urlViewModel.insert(Url(url.toLowerCase(Locale.ROOT), LocalDateTime.now().toString()))
        connectToSrv(url)
    }

    /** Connect to the server using http get request. */
    private fun connectToSrv(url: String) {
        //val URL = "http://10.0.2.2:5550/"
        try {
            val client = OkHttpClient.Builder()
                .connectTimeout(100, java.util.concurrent.TimeUnit.SECONDS)
                .readTimeout(100, java.util.concurrent.TimeUnit.SECONDS).build()

            val gson = GsonBuilder().setLenient().create()

            val retrofit = Retrofit.Builder().baseUrl(url)
                .addConverterFactory(GsonConverterFactory.create(gson)).build()

            val api = retrofit.create(GetImageService::class.java)
            /** Send a http request for getting screenshot and handle response. */
            val body = api.getImg().enqueue(object: Callback<ResponseBody> {
                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    Toast.makeText(
                        applicationContext,
                        "Connection failed",
                        Toast.LENGTH_SHORT
                    ).show()
                    return
                }

                override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                    if(response.isSuccessful) {
                        openActivity(url)
                    } else {
                        Toast.makeText(
                            applicationContext,
                            "Couldn't get an image from the server",
                            Toast.LENGTH_SHORT
                        )
                    }
                }
            })
        }
        catch (e : Exception) {
                Toast.makeText(
                applicationContext,
                "Connection failed",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    /** Move to the next activity after successful connection. */
    private fun openActivity(url: String) {
        val intent = Intent(this, ControllersActivity::class.java)
        intent.putExtra("url", url)
        startActivity(intent)
    }
}
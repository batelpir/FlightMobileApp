package com.example.flightmobileapp

import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.POST

interface GetImageService {
    @Headers("Content-Type: image/png")
    @GET("/screenshot")
    fun getImg(): Call<ResponseBody>
    @POST("/api/command")
    fun postCommand(@Body rb : RequestBody): Call<ResponseBody>
}
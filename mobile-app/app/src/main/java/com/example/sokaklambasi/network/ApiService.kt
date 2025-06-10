package com.example.sokaklambasi.network

import retrofit2.http.*

interface ApiService {
    @GET("status")
    suspend fun getSensorData(): Map<String, Any>

    @POST("lights/on")
    suspend fun lampOn(): Map<String, Any>

    @POST("lights/off")
    suspend fun lampOff(): Map<String, Any>

    @POST("lights/mode")
    suspend fun setManualMode(@Body data: Map<String, Boolean>): Map<String, Any>

    @POST("register-token")
    suspend fun registerToken(@Body token: Map<String, String>): Map<String, Any>
}

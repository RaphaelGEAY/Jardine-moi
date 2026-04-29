package com.example.jardinemoi.data.trefle

import retrofit2.http.GET
import retrofit2.http.Query

interface TrefleApi {
    @GET("plants")
    suspend fun getPlants(
        @Query("token") token: String,
        @Query("page") page: Int = 1
    ): TreflePlantResponse
}

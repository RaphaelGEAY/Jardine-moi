package com.example.jardinemoi.data.trefle

import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface TrefleApi {
    @GET("plants")
    suspend fun getPlants(
        @Query("token") token: String,
        @Query("page") page: Int = 1
    ): TreflePlantResponse

    @GET("plants/search")
    suspend fun searchPlants(
        @Query("token") token: String,
        @Query("q") query: String,
        @Query("page") page: Int = 1
    ): TreflePlantResponse

    @GET("plants/{id}")
    suspend fun getPlant(
        @Path("id") id: String,
        @Query("token") token: String
    ): TreflePlantDetailResponse
}

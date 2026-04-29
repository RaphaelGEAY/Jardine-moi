package com.example.jardinemoi.data.trefle

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object TrefleClient {

    private const val BASE_URL = "https://trefle.io/api/v1/"

    val api: TrefleApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(TrefleApi::class.java)
    }
}

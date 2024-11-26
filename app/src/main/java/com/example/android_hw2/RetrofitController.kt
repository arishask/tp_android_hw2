package com.example.android_hw2

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


class RetrofitController(api: String) : RequestController {
    private val retrofit = Retrofit.Builder()
        .baseUrl(api)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val gifApi = retrofit.create(GifAPI::class.java)

    override suspend fun fetchGifFromApi(): String {
        try {
            val response = gifApi.getRandomGif()
            return response.data.images.original.url
        } catch (e: Exception) {
            throw e
        }
    }
}
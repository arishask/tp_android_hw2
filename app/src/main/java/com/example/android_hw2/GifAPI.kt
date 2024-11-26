package com.example.android_hw2

import retrofit2.http.GET

interface GifAPI {
    @GET("random?api_key=aptMGWS7L81tDPSrwH5qzmkdgakxPfQs")
    suspend fun getRandomGif(): GifResponse
}
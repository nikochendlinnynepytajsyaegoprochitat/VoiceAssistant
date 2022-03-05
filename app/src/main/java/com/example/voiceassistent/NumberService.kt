package com.example.voiceassistent

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object NumberService {
    val api: NumberAPI
        get() {
            val retrofit = Retrofit.Builder()
                .baseUrl("https://htmlweb.ru")
                .addConverterFactory(GsonConverterFactory.create())
                .build()
            return retrofit.create(NumberAPI::class.java)
        }
}
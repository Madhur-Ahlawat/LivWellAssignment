package com.example.livwellassignment.network

import okhttp3.Interceptor
import okhttp3.Response

class OMDB_Interceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val originalUrl = originalRequest.url

        val newUrl = originalUrl.newBuilder()
            .addQueryParameter("apikey", "5ef16d1")
            .addQueryParameter("plot", "full")
            .build()

        val newRequest = originalRequest.newBuilder()
            .url(newUrl)
            .build()

        return chain.proceed(newRequest)
    }
}
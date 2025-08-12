package com.example.myandroidproject.network

import com.example.myandroidproject.models.OMDB_SearchMovieResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface TMDB_OMDBService {
    @GET("/")
    suspend fun getMovieDetails(
        @Query("page") page: Int,
        @Query("s") keyword: String
    ): OMDB_SearchMovieResponse
}
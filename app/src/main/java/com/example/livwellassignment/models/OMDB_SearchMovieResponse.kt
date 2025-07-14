package com.example.livwellassignment.models

import com.google.gson.annotations.SerializedName

data class OMDB_SearchMovieResponse(
    @SerializedName("Search") val search: List<MovieListItem>?,
    @SerializedName("totalResults") val totalResults: String?,
    @SerializedName("Response") val response: String?
)
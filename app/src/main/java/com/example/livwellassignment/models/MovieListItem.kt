package com.example.livwellassignment.models

import com.google.gson.annotations.SerializedName

data class MovieListItem(
    @SerializedName("Poster") val poster: String,
    @SerializedName("Title") val title: String,
    @SerializedName("Type") val type: String,
    @SerializedName("Year") val year: String,
    @SerializedName("imdbID") val imdbID: String
)
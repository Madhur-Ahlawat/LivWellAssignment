package com.example.livwellassignment.models

import androidx.paging.PagingData

sealed class MovieUiState {
    object Loading : MovieUiState()
    data class Success(val movies: PagingData<MovieListItem>) : MovieUiState()
    data class Error(val message: String) : MovieUiState()
}
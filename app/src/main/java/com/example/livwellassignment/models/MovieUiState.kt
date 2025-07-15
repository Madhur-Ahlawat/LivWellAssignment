package com.example.livwellassignment.models

import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow

sealed class MovieUiState {
    object Loading : MovieUiState()
    data class Success(val data: Flow<PagingData<MovieListItem>>) : MovieUiState()
    data class Error(val message: String) : MovieUiState()
}
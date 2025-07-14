package com.example.livwellassignment.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.livwellassignment.models.MovieUiState
import com.example.livwellassignment.network.repositories.MovieRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.stateIn

@HiltViewModel
class MovieViewModel @Inject constructor(
    private val repository: MovieRepository
) : ViewModel() {
    var _isDarkTheme: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val isDarkTheme: StateFlow<Boolean> = _isDarkTheme

    var _searchText: MutableStateFlow<String> = MutableStateFlow("")
    val searchext: StateFlow<String> = _searchText

    val moviesFlow = repository.getMovies()

    val uiState: StateFlow<MovieUiState> = moviesFlow
        .map {
            MovieUiState.Success(it) as MovieUiState
        }
        .catch {
            emit(MovieUiState.Error(it.message ?: "Unknown error"))
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), MovieUiState.Loading)
}
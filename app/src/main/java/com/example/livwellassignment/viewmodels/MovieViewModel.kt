package com.example.livwellassignment.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import com.example.livwellassignment.models.MovieListItem
import com.example.livwellassignment.models.MovieUiState
import com.example.livwellassignment.network.repositories.MovieRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn

@HiltViewModel
class MovieViewModel @Inject constructor(
    private val repository: MovieRepository
) : ViewModel() {
    private val _isDarkTheme = MutableStateFlow(false)
    val isDarkTheme: StateFlow<Boolean> = _isDarkTheme

    fun setDarkTheme(value: Boolean) {
        _isDarkTheme.value = value
    }
    var _searchText: MutableStateFlow<String> = MutableStateFlow("")
    val searchext: StateFlow<String> = _searchText
    var onInputKeywordChanged: (String) -> Unit = this::onInputKeywordChanged
    var movieResponse: Flow<PagingData<MovieListItem>> = repository.getMovies(searchext.value)

    fun onInputKeywordChanged(keyword: String) {
        _searchText.value = keyword
    }

    val uiState: StateFlow<MovieUiState> = _searchText
        .debounce(500)
        .distinctUntilChanged()
        .mapLatest { input ->
            if (input.isBlank()) {
                MovieUiState.Error("Please enter movie name!")
            } else {
                try {
                    movieResponse = repository.getMovies(input)
                    MovieUiState.Success(movieResponse)
                } catch (e: Exception) {
                    MovieUiState.Error(e.message ?: "Unknown error")
                }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), MovieUiState.Loading)
}
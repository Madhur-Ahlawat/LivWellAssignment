package com.example.livwellassignment.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.example.livwellassignment.models.MovieListItem
import com.example.livwellassignment.models.MovieUiState
import com.example.livwellassignment.network.repositories.MovieRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class MovieViewModel @Inject constructor(
    private val repository: MovieRepository
) : ViewModel() {

    private val _isDarkTheme = MutableStateFlow(false)
    val isDarkTheme: StateFlow<Boolean> = _isDarkTheme

    fun setDarkTheme(value: Boolean) {
        _isDarkTheme.value = value
    }

    private val _searchText = MutableStateFlow("")
    val searchText: StateFlow<String> = _searchText
    var movieResponse: Flow<PagingData<MovieListItem>>? = null

    var onInputKeywordChanged: (String) -> Unit = {
        _searchText.value = it
    }

//    val uiState: StateFlow<MovieUiState> = _searchText
//        .debounce(300)
//        .distinctUntilChanged()
//        .flatMapLatest { input ->
//            if (input.isBlank()) {
//                flow {
//                    emit(MovieUiState.Error("Please enter movie name!"))
//                }
//            } else {
//                repository.getMovies(input)
//                    .map { pagingData ->
//                        MovieUiState.Success(pagingData) as MovieUiState
//                    }
//                    .onStart {
//                        emit(MovieUiState.Loading)
//                    }
//                    .catch { e ->
//                        emit(MovieUiState.Error(e.message ?: "Unknown error"))
//                    }
//            }
//        }
//        .stateIn(
//            viewModelScope,
//            SharingStarted.WhileSubscribed(5000),
//            MovieUiState.Loading
//        )
val movies: Flow<PagingData<MovieListItem>> = _searchText
    .debounce(300)
    .distinctUntilChanged()
    .flatMapLatest { input ->
        if (input.isBlank()) {
            flowOf(PagingData.empty())
        } else {
            repository.getMovies(input)
        }.cachedIn(viewModelScope)
    }
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), PagingData.empty())
}
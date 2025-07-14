package com.example.livwellassignment.ui.composables

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.livwellassignment.models.MovieUiState
import com.example.livwellassignment.viewmodels.MovieViewModel
import kotlinx.coroutines.flow.MutableStateFlow

@Composable
fun MovieGridScreen(modifier: Modifier, viewModel: MovieViewModel = hiltViewModel()) {
    viewModel._isDarkTheme = MutableStateFlow(isSystemInDarkTheme())
    val state by viewModel.uiState.collectAsState()
    when (state) {
        is MovieUiState.Loading -> CircularProgressIndicator()
        is MovieUiState.Error -> Text("Error: ${(state as MovieUiState.Error).message}")
        is MovieUiState.Success -> MovieGrid(modifier,viewModel)
    }
}
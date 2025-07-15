package com.example.livwellassignment.ui.composables

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.livwellassignment.models.MovieUiState
import com.example.livwellassignment.viewmodels.MovieViewModel

@Composable
fun MovieGridScreen(
    modifier: Modifier = Modifier,
    viewModel: MovieViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    Column(modifier = modifier
        .fillMaxSize()
        .padding(top = 10.dp)) {
        SearchBar(
            onSearch = viewModel.onInputKeywordChanged,
            viewModel = viewModel
        )

        when (state) {
            is MovieUiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            is MovieUiState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${(state as MovieUiState.Error).message}",
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }

            is MovieUiState.Success -> {
                MovieGrid(modifier = Modifier.weight(1f), viewModel = viewModel)
            }
        }
    }
}
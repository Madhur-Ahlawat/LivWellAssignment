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
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import com.example.livwellassignment.models.MovieUiState
import com.example.livwellassignment.viewmodels.MovieViewModel

@Composable
fun MovieGridScreen(
    modifier: Modifier = Modifier,
    viewModel: MovieViewModel = hiltViewModel()
) {
    val search by viewModel.searchText.collectAsState()
    val movies = viewModel.movies.collectAsLazyPagingItems()
    Column(modifier = modifier
        .fillMaxSize()
        .padding(top = 10.dp)) {
        SearchBar(
            onSearch = viewModel.onInputKeywordChanged,
            viewModel = viewModel
        )

        when {
            movies.loadState.refresh is LoadState.Loading -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }

            movies.loadState.refresh is LoadState.Error -> {
                val error = movies.loadState.refresh as LoadState.Error
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Error: ${error.error.message}")
                }
            }

            movies.itemCount == 0 && search.isNotBlank() -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No results found.")
                }
            }
            search.isBlank() -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Please enter movie name!")
                }
            }
            else ->{
                MovieGrid(modifier = Modifier.weight(1f), viewModel = viewModel)
            }
        }
    }
}
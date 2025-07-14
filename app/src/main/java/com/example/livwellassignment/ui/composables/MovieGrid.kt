package com.example.livwellassignment.ui.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.example.livwellassignment.models.MovieListItem
import com.example.livwellassignment.viewmodels.MovieViewModel

@Composable
fun MovieGrid(modifier: Modifier, viewModel: MovieViewModel = hiltViewModel()) {
    val movies:LazyPagingItems<MovieListItem> = viewModel.moviesFlow!!.collectAsLazyPagingItems()
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 100.dp),
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5)),
        contentPadding = PaddingValues(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(movies.itemCount) { index ->
            val movie: MovieListItem? = movies[index]
            movie?.let { movie ->
                MovieCard(movie)
            }
        }
    }
}
package com.example.livwellassignment.network.repositories

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.example.livwellassignment.models.MovieListItem
import com.example.livwellassignment.network.TMDB_OMDBService
import com.example.livwellassignment.paging.SearchMoviePagingSource
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class MovieRepository @Inject constructor(private val service: TMDB_OMDBService) {
    fun getMovies(input: String): Flow<PagingData<MovieListItem>> = Pager(
        PagingConfig(pageSize = 10)
    ) {
        SearchMoviePagingSource(service, input)
    }.flow
}
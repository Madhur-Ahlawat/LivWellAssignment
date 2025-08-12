package com.example.myandroidproject.network.repositories

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.example.myandroidproject.models.MovieListItem
import com.example.myandroidproject.network.TMDB_OMDBService
import com.example.myandroidproject.paging.SearchMoviePagingSource
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class MovieRepository @Inject constructor(private val service: TMDB_OMDBService) {
    fun getMovies(input: String): Flow<PagingData<MovieListItem>> = Pager(
        PagingConfig(pageSize = 10, prefetchDistance = 2)
    ) {
        SearchMoviePagingSource(service, input)
    }.flow
}
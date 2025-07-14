package com.example.livwellassignment.network.repositories

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.example.livwellassignment.models.MovieListItem
import com.example.livwellassignment.network.TMDB_OMDBService
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class MovieRepository @Inject constructor(private val service: TMDB_OMDBService) {
    fun getMovies(): Flow<PagingData<MovieListItem>> = Pager(
        PagingConfig(pageSize = 10)
    ) {
        object : PagingSource<Int, MovieListItem>() {
            override suspend fun load(params: LoadParams<Int>): LoadResult<Int, MovieListItem> {
                return try {
                    val page = params.key ?: 1
                    val response = service.getMovieDetails(keyword = "Batman", page = page)
                    LoadResult.Page(
                        data = response.search!!,
                        prevKey = if (page == 1) null else page - 1,
                        nextKey = if (response.search!!.isEmpty()) null else page + 1
                    )
                } catch (e: Exception) {
                    LoadResult.Error(e)
                }
            }

            override fun getRefreshKey(state: PagingState<Int, MovieListItem>) = null
        }
    }.flow
}
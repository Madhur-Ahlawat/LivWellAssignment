package com.example.livwellassignment.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.example.livwellassignment.models.MovieListItem
import com.example.livwellassignment.network.TMDB_OMDBService

class SearchMoviePagingSource(
    private val service: TMDB_OMDBService,
    private val query: String
) : PagingSource<Int, MovieListItem>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, MovieListItem> {
        return try {
            val page = params.key ?: 1
            val response = service.getMovieDetails(keyword = query, page = page)

            LoadResult.Page(
                data = response.search.orEmpty(),
                prevKey = if (page == 1) null else page - 1,
                nextKey = if (response.search.isNullOrEmpty()) null else page + 1
            )
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, MovieListItem>): Int? = null
}
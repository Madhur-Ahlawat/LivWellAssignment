package com.example.livwellassignment.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.example.livwellassignment.models.MovieListItem
import com.example.livwellassignment.network.TMDB_OMDBService
import kotlin.math.ceil

class SearchMoviePagingSource(
    private val service: TMDB_OMDBService,
    private val input: String
) : PagingSource<Int, MovieListItem>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, MovieListItem> {
        return try {
            val page = params.key ?: 1
            val response = service.getMovieDetails(keyword = input, page = page)
            val totalResults = response.totalResults?.toIntOrNull() ?: 0
            val totalPages = ceil(totalResults / 10.0).toInt()
            val next = if (page < totalPages) page + 1 else null
            LoadResult.Page(
                data = response.search.orEmpty(),
                prevKey = if (page == 1) null else page - 1,
                nextKey = if (response.search.isNullOrEmpty()) null else page + 1
            )
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, MovieListItem>): Int? {
        return state.anchorPosition?.let { anchor ->
            val page = state.closestPageToPosition(anchor)
            page?.prevKey?.plus(1) ?: page?.nextKey?.minus(1)
        }
    }}
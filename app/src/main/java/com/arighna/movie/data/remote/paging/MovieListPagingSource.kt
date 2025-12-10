package com.arighna.movie.data.remote.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState

/**
 * A [PagingSource] that loads movies from the API in a paginated way.
 *
 * @param movieApi The API service to fetch movies from.
 * @param genre An optional genre to filter the movies by.
 */
class MovieListPagingSource(
    private val movieApi: com.arighna.movie.data.remote.MovieApi,
    private val genre: String?
) : PagingSource<Int, com.arighna.movie.data.remote.dto.MovieDto>() {

    /**
     * Loads a page of movies from the API.
     *
     * @param params The parameters for the load, including the key for the page to load.
     * @return A [LoadResult] containing the loaded data or an error.
     */
    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, com.arighna.movie.data.remote.dto.MovieDto> {
        val from = params.key ?: 0
        return try {
            val movies = movieApi.getMovies(
                limit = params.loadSize,
                from = from,
                genre = genre
            )
            LoadResult.Page(
                data = movies,
                prevKey = if (from == 0) null else from - params.loadSize,
                nextKey = if (movies.isEmpty()) null else from + params.loadSize
            )
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }

    /**
     * Provides a key for refreshing the data. This is a callback for the Paging 3 library,
     * which it calls when the data is invalidated to determine the starting point for a new
     * data load. The key is calculated based on the user's last scroll position to ensure a
     * smooth refresh experience.
     */
    override fun getRefreshKey(state: PagingState<Int, com.arighna.movie.data.remote.dto.MovieDto>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(state.config.pageSize)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(state.config.pageSize)
        }
    }
}

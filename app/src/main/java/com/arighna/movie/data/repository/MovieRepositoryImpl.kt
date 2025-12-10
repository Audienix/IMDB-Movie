package com.arighna.movie.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import com.arighna.movie.common.Constants
import com.arighna.movie.data.remote.MovieApi
import com.arighna.movie.data.remote.dto.MovieDto
import com.arighna.movie.data.remote.paging.MovieListPagingSource
import com.arighna.movie.domain.model.Genre
import com.arighna.movie.domain.model.Movie
import com.arighna.movie.domain.repository.MovieRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class MovieRepositoryImpl @Inject constructor(
    private val movieApi: MovieApi
) : MovieRepository {

    /**
     * Retrieves a paginated flow of movies.
     *
     * This function uses a smaller `initialLoadSize` than the `pageSize` as a performance
     * optimization. This ensures the initial screen load is as fast as possible, getting
     * content to the user quickly, while subsequent pages are loaded in larger chunks for
     * smoother scrolling.
     *
     * @param genre An optional genre to filter the movies by.
     * @return A Flow of `PagingData<Movie>`.
     */
    override fun getMovies(genre: String?): Flow<PagingData<Movie>> {
        return Pager(
            config = PagingConfig(
                pageSize = Constants.PAGE_SIZE,
                initialLoadSize = Constants.INITIAL_LOAD_SIZE,
                enablePlaceholders = false
            ),
            pagingSourceFactory = { MovieListPagingSource(movieApi, genre) }
        ).flow.map { pagingData ->
            pagingData.map { movieDto ->
                movieDto.toMovie()
            }
        }
    }

    override suspend fun getGenres(): Result<List<Genre>> {
        return try {
            val result = movieApi.getGenres().map { (name, count) ->
                Genre(name, count.toInt())
            }
            Result.success(result)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

fun MovieDto.toMovie(): Movie {
    return Movie(
        id = id,
        title = title,
        overview = overview,
        releaseYear = releaseDate.split("-").first(),
        genres = genres,
        url = url
    )
}

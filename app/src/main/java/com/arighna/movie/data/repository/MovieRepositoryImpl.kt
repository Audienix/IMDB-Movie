package com.arighna.movie.data.repository

import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import com.arighna.movie.common.Constants
import com.arighna.movie.data.local.MovieDatabase
import com.arighna.movie.data.local.entity.MovieEntity
import com.arighna.movie.data.remote.MovieApi
import com.arighna.movie.data.remote.dto.MovieDto
import com.arighna.movie.data.remote.paging.MovieRemoteMediator
import com.arighna.movie.domain.model.Genre
import com.arighna.movie.domain.model.Movie
import com.arighna.movie.domain.repository.MovieRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class MovieRepositoryImpl @Inject constructor(
    private val movieApi: MovieApi,
    private val movieDb: MovieDatabase
) : MovieRepository {

    /**
     * Retrieves a paginated flow of movies with offline support.
     */
    @OptIn(ExperimentalPagingApi::class)
    override fun getMovies(genre: String?): Flow<PagingData<Movie>> {
        return Pager(
            config = PagingConfig(
                pageSize = Constants.PAGE_SIZE,
                initialLoadSize = Constants.INITIAL_LOAD_SIZE,
                enablePlaceholders = false
            ),
            remoteMediator = MovieRemoteMediator(
                movieApi = movieApi,
                movieDb = movieDb,
                genre = genre
            ),
            pagingSourceFactory = {
                movieDb.movieDao().getMovies(genre)
            }
        ).flow.map { pagingData ->
            pagingData.map { movieEntity ->
                movieEntity.toMovie()
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

fun MovieEntity.toMovie(): Movie {
    return Movie(
        id = id,
        title = title,
        overview = overview,
        releaseYear = releaseDate.split("-").first(),
        genres = genres.split(","),
        url = url
    )
}

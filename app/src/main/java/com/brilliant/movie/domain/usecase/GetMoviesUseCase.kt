package com.brilliant.movie.domain.usecase

import androidx.paging.PagingData
import com.brilliant.movie.domain.model.Movie
import com.brilliant.movie.domain.repository.MovieRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetMoviesUseCase @Inject constructor(
    private val movieRepository: MovieRepository
) {
    operator fun invoke(genre: String?): Flow<PagingData<Movie>> {
        return movieRepository.getMovies(genre)
    }
}
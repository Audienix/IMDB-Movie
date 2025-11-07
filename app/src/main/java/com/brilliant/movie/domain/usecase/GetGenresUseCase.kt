package com.brilliant.movie.domain.usecase

import com.brilliant.movie.domain.model.Genre
import com.brilliant.movie.domain.repository.MovieRepository
import javax.inject.Inject

class GetGenresUseCase @Inject constructor(
    private val movieRepository: MovieRepository
) {
    suspend operator fun invoke(): Result<List<Genre>> {
        return movieRepository.getGenres()
    }
}
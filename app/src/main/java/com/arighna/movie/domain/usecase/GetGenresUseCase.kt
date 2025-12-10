package com.arighna.movie.domain.usecase

import javax.inject.Inject

class GetGenresUseCase @Inject constructor(
    private val movieRepository: com.arighna.movie.domain.repository.MovieRepository
) {
    suspend operator fun invoke(): Result<List<com.arighna.movie.domain.model.Genre>> {
        return movieRepository.getGenres()
    }
}
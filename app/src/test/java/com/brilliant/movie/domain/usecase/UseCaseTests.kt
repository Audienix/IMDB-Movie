package com.brilliant.movie.domain.usecase

import com.brilliant.movie.domain.model.Genre
import com.brilliant.movie.domain.repository.MovieRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.Test

class UseCaseTests {

    @Test
    fun `GetMoviesUseCase invokes repository`() {
        val repository = mockk<MovieRepository>(relaxed = true)
        val useCase = GetMoviesUseCase(repository)

        useCase.invoke("Action")

        coVerify { repository.getMovies("Action") }
    }

    @Test
    fun `GetGenresUseCase invokes repository`() = runBlocking {
        val repository = mockk<MovieRepository>()
        val useCase = GetGenresUseCase(repository)
        val genres = listOf(Genre("Action", 100))
        coEvery { repository.getGenres() } returns Result.success(genres)

        useCase.invoke()

        coVerify { repository.getGenres() }
    }
}
package com.brilliant.movie.data.repository

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.brilliant.movie.TestCoroutineRule
import com.brilliant.movie.data.remote.MovieApi
import com.brilliant.movie.domain.model.Genre
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@ExperimentalCoroutinesApi
class MovieRepositoryImplTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val testCoroutineRule = TestCoroutineRule()

    private val movieApi: MovieApi = mockk()
    private val repository = MovieRepositoryImpl(movieApi)

    @Test
    fun `getGenres returns success result on successful fetch`() = runTest {
        val genreData = listOf(listOf("Action", "100"), listOf("Comedy", "200"))
        coEvery { movieApi.getGenres() } returns genreData

        val result = repository.getGenres()

        assertTrue(result.isSuccess)
        val genres = result.getOrNull()
        assertEquals(2, genres?.size)
        assertEquals(Genre("Action", 100), genres?.get(0))
        assertEquals(Genre("Comedy", 200), genres?.get(1))
    }

    @Test
    fun `getGenres returns failure result on API error`() = runTest {
        val exception = RuntimeException("API Error")
        coEvery { movieApi.getGenres() } throws exception

        val result = repository.getGenres()

        assertTrue(result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
    }
}

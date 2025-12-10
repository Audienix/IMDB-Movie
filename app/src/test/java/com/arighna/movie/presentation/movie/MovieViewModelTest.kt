package com.arighna.movie.presentation.movie

import com.arighna.movie.TestCoroutineRule
import com.arighna.movie.domain.model.Genre
import com.arighna.movie.domain.usecase.GetGenresUseCase
import com.arighna.movie.domain.usecase.GetMoviesUseCase
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@ExperimentalCoroutinesApi
class MovieViewModelTest {

    @get:Rule
    val testCoroutineRule = TestCoroutineRule()

    private lateinit var getMoviesUseCase: GetMoviesUseCase
    private lateinit var getGenresUseCase: GetGenresUseCase
    private lateinit var viewModel: MovieViewModel

    @Before
    fun setUp() {
        getMoviesUseCase = mockk(relaxed = true)
        getGenresUseCase = mockk()
        viewModel = MovieViewModel(getMoviesUseCase, getGenresUseCase)
    }

    @Test
    fun `onGenreSelected updates the selected genre`() = runTest {
        val genre = Genre("Action", 100)
        viewModel.onGenreSelected(genre)
        assertEquals(genre, viewModel.state.value.selectedGenre)
    }

    @Test
    fun `loadGenres updates genres on success`() = runTest {
        val genres = listOf(Genre("Comedy", 200))
        coEvery { getGenresUseCase() } returns Result.success(genres)

        viewModel.loadGenres()

        assertEquals(genres, viewModel.state.value.genres)
    }

    @Test
    fun `loadGenres updates error on failure`() = runTest {
        val errorMessage = "Error fetching genres"
        coEvery { getGenresUseCase() } returns Result.failure(Exception(errorMessage))

        viewModel.loadGenres()

        assertEquals(errorMessage, viewModel.state.value.error)
    }
}

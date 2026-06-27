package com.arighna.movie.data.repository

import com.arighna.movie.data.local.entity.MovieEntity
import com.arighna.movie.data.remote.dto.MovieDto
import com.arighna.movie.domain.model.Movie
import org.junit.Assert.assertEquals
import org.junit.Test

class MovieMapperTest {

    @Test
    fun `toMovie correctly maps MovieDto to Movie`() {
        val movieDto = MovieDto(
            id = "1",
            title = "Title",
            overview = "Overview",
            releaseDate = "2023-01-01",
            genres = listOf("Action", "Drama"),
            url = "url"
        )

        val expectedMovie = Movie(
            id = "1",
            title = "Title",
            overview = "Overview",
            releaseYear = "2023",
            genres = listOf("Action", "Drama"),
            url = "url"
        )

        val actualMovie = movieDto.toMovie()

        assertEquals(expectedMovie, actualMovie)
    }

    @Test
    fun `toMovie correctly maps MovieEntity to Movie`() {
        val movieEntity = MovieEntity(
            id = "1",
            title = "Title",
            overview = "Overview",
            releaseDate = "2023-01-01",
            genres = "Action,Drama",
            url = "url",
            category = "Action"
        )

        val expectedMovie = Movie(
            id = "1",
            title = "Title",
            overview = "Overview",
            releaseYear = "2023",
            genres = listOf("Action", "Drama"),
            url = "url"
        )

        val actualMovie = movieEntity.toMovie()

        assertEquals(expectedMovie, actualMovie)
    }
}

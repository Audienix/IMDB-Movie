package com.brilliant.movie.domain.model

import org.junit.Assert.assertEquals
import org.junit.Test

class MovieTest {

    @Test
    fun `Movie properties are correctly assigned`() {
        val movie = Movie(
            id = "1",
            title = "Title",
            overview = "Overview",
            releaseYear = "2023",
            genres = listOf("Action"),
            url = "url"
        )

        assertEquals("1", movie.id)
        assertEquals("Title", movie.title)
        assertEquals("Overview", movie.overview)
        assertEquals("2023", movie.releaseYear)
        assertEquals(listOf("Action"), movie.genres)
        assertEquals("url", movie.url)
    }
}

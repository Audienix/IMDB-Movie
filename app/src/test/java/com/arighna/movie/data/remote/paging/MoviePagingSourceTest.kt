package com.arighna.movie.data.remote.paging

import androidx.paging.PagingConfig
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.arighna.movie.data.remote.MovieApi
import com.arighna.movie.data.remote.dto.MovieDto
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.IOException

@ExperimentalCoroutinesApi
class MoviePagingSourceTest {

    private val movieApi: MovieApi = mockk()
    private val movieDtoList = listOf(
        MovieDto("1", listOf("Action"), "2023-01-01", "Title 1", "Overview 1", "url1"),
        MovieDto("2", listOf("Comedy"), "2022-01-01", "Title 2", "Overview 2", "url2")
    )
    private val genre = "Action"

    @Test
    fun `load - refresh success`() = runTest {
        val pagingSource = MovieListPagingSource(movieApi, genre)
        coEvery { movieApi.getMovies(limit = 2, from = 0, genre = genre) } returns movieDtoList

        val result = pagingSource.load(
            PagingSource.LoadParams.Refresh(
                key = 0,
                loadSize = 2,
                placeholdersEnabled = false
            )
        )

        val expected = PagingSource.LoadResult.Page(
            data = movieDtoList,
            prevKey = null,
            nextKey = 2
        )
        assertEquals(expected, result)
    }

    @Test
    fun `load - append success`() = runTest {
        val pagingSource = MovieListPagingSource(movieApi, genre)
        coEvery { movieApi.getMovies(limit = 2, from = 2, genre = genre) } returns movieDtoList

        val result = pagingSource.load(
            PagingSource.LoadParams.Append(
                key = 2,
                loadSize = 2,
                placeholdersEnabled = false
            )
        )

        val expected = PagingSource.LoadResult.Page(
            data = movieDtoList,
            prevKey = 0,
            nextKey = 4
        )
        assertEquals(expected, result)
    }

    @Test
    fun `load - returns empty page at end of data`() = runTest {
        val pagingSource = MovieListPagingSource(movieApi, genre)
        coEvery { movieApi.getMovies(limit = 2, from = 10, genre = genre) } returns emptyList()

        val result = pagingSource.load(
            PagingSource.LoadParams.Append(
                key = 10,
                loadSize = 2,
                placeholdersEnabled = false
            )
        )

        val expected = PagingSource.LoadResult.Page(
            data = emptyList(),
            prevKey = 8,
            nextKey = null
        )
        assertEquals(expected, result)
    }

    @Test
    fun `load - returns error on API failure`() = runTest {
        val pagingSource = MovieListPagingSource(movieApi, genre)
        val exception = IOException("API error")
        coEvery { movieApi.getMovies(any(), any(), any()) } throws exception

        val result = pagingSource.load(
            PagingSource.LoadParams.Refresh(
                key = 0,
                loadSize = 2,
                placeholdersEnabled = false
            )
        )

        assertTrue(result is PagingSource.LoadResult.Error)
        assertEquals(exception, (result as PagingSource.LoadResult.Error).throwable)
    }

    @Test
    fun `getRefreshKey returns correct key`() {
        val pagingSource = MovieListPagingSource(movieApi, null)
        val pages = listOf(
            PagingSource.LoadResult.Page(data = movieDtoList, prevKey = null, nextKey = 2),
            PagingSource.LoadResult.Page(data = movieDtoList, prevKey = 0, nextKey = 4)
        )
        val state = PagingState<Int, MovieDto>(
            pages = pages,
            anchorPosition = 3,
            config = PagingConfig(pageSize = 2),
            leadingPlaceholderCount = 0
        )

        val refreshKey = pagingSource.getRefreshKey(state)

        assertEquals(2, refreshKey)
    }
}

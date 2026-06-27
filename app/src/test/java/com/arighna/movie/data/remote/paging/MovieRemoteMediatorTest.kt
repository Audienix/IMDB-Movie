package com.arighna.movie.data.remote.paging

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingConfig
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import com.arighna.movie.data.local.MovieDao
import com.arighna.movie.data.local.MovieDatabase
import com.arighna.movie.data.local.RemoteKeyDao
import com.arighna.movie.data.local.entity.MovieEntity
import com.arighna.movie.data.remote.MovieApi
import com.arighna.movie.data.remote.dto.MovieDto
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.slot
import io.mockk.unmockkStatic
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalPagingApi::class)
@ExperimentalCoroutinesApi
class MovieRemoteMediatorTest {

    private val movieApi: MovieApi = mockk()
    private val movieDb: MovieDatabase = mockk()
    private val movieDao: MovieDao = mockk()
    private val remoteKeyDao: RemoteKeyDao = mockk()

    @Before
    fun setup() {
        every { movieDb.movieDao() } returns movieDao
        every { movieDb.remoteKeyDao() } returns remoteKeyDao
        
        // Mocking withTransaction extension function
        mockkStatic("androidx.room.RoomDatabaseKt")
        val transactionLambda = slot<suspend () -> Any?>()
        coEvery { movieDb.withTransaction<Any?>(capture(transactionLambda)) } coAnswers {
            transactionLambda.captured.invoke()
        }
    }

    @After
    fun tearDown() {
        unmockkStatic("androidx.room.RoomDatabaseKt")
    }

    @Test
    fun `load returns success result when more data is present`() = runTest {
        val movieDtoList = listOf(
            MovieDto("1", listOf("Action"), "2023-01-01", "Title 1", "Overview 1", "url1")
        )
        coEvery { movieApi.getMovies(any(), any(), any()) } returns movieDtoList
        coEvery { movieDao.clearAll() } returns 0
        coEvery { remoteKeyDao.clearRemoteKeys() } returns 0
        coEvery { movieDao.insertAll(any()) } returns listOf(1L)
        coEvery { remoteKeyDao.insertAll(any()) } returns listOf(1L)
        coEvery { remoteKeyDao.remoteKeysMovieId(any()) } returns null

        val mediator = MovieRemoteMediator(movieApi, movieDb, null)
        val pagingState = PagingState<Int, MovieEntity>(
            pages = listOf(),
            anchorPosition = null,
            config = PagingConfig(pageSize = 2),
            leadingPlaceholderCount = 0
        )

        val result = mediator.load(LoadType.REFRESH, pagingState)

        assertTrue(result is RemoteMediator.MediatorResult.Success)
        assertFalse((result as RemoteMediator.MediatorResult.Success).endOfPaginationReached)
    }

    @Test
    fun `load returns success and end of pagination reached when no more data`() = runTest {
        coEvery { movieApi.getMovies(any(), any(), any()) } returns emptyList()
        coEvery { movieDao.clearAll() } returns 0
        coEvery { remoteKeyDao.clearRemoteKeys() } returns 0
        coEvery { movieDao.insertAll(any()) } returns emptyList()
        coEvery { remoteKeyDao.insertAll(any()) } returns emptyList()
        coEvery { remoteKeyDao.remoteKeysMovieId(any()) } returns null

        val mediator = MovieRemoteMediator(movieApi, movieDb, null)
        val pagingState = PagingState<Int, MovieEntity>(
            pages = listOf(),
            anchorPosition = null,
            config = PagingConfig(pageSize = 2),
            leadingPlaceholderCount = 0
        )

        val result = mediator.load(LoadType.REFRESH, pagingState)

        assertTrue(result is RemoteMediator.MediatorResult.Success)
        assertTrue((result as RemoteMediator.MediatorResult.Success).endOfPaginationReached)
    }

    @Test
    fun `load returns error result when error occurs`() = runTest {
        coEvery { movieApi.getMovies(any(), any(), any()) } throws java.io.IOException()

        val mediator = MovieRemoteMediator(movieApi, movieDb, null)
        val pagingState = PagingState<Int, MovieEntity>(
            pages = listOf(),
            anchorPosition = null,
            config = PagingConfig(pageSize = 2),
            leadingPlaceholderCount = 0
        )

        val result = mediator.load(LoadType.REFRESH, pagingState)

        assertTrue(result is RemoteMediator.MediatorResult.Error)
    }
}

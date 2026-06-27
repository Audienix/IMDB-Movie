package com.arighna.movie.data.remote.paging

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import com.arighna.movie.data.local.MovieDatabase
import com.arighna.movie.data.local.entity.MovieEntity
import com.arighna.movie.data.local.entity.RemoteKeyEntity
import com.arighna.movie.data.remote.MovieApi

@OptIn(ExperimentalPagingApi::class)
class MovieRemoteMediator(
    private val movieApi: MovieApi,
    private val movieDb: MovieDatabase,
    private val genre: String?
) : RemoteMediator<Int, MovieEntity>() {

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, MovieEntity>
    ): MediatorResult {
        val from = when (loadType) {
            LoadType.REFRESH -> {
                val remoteKeys = getRemoteKeyClosestToCurrentPosition(state)
                remoteKeys?.nextKey?.minus(state.config.pageSize) ?: 0
            }
            LoadType.PREPEND -> {
                val remoteKeys = getRemoteKeyForFirstItem(state)
                val prevKey = remoteKeys?.prevKey
                    ?: return MediatorResult.Success(endOfPaginationReached = remoteKeys != null)
                prevKey
            }
            LoadType.APPEND -> {
                val remoteKeys = getRemoteKeyForLastItem(state)
                val nextKey = remoteKeys?.nextKey
                    ?: return MediatorResult.Success(endOfPaginationReached = remoteKeys != null)
                nextKey
            }
        }

        try {
            val response = movieApi.getMovies(
                limit = state.config.pageSize,
                from = from,
                genre = genre
            )

            val endOfPaginationReached = response.isEmpty()
            movieDb.withTransaction {
                if (loadType == LoadType.REFRESH) {
                    movieDb.remoteKeyDao().clearRemoteKeys()
                    movieDb.movieDao().clearAll()
                }
                val prevKey = if (from == 0) null else from - state.config.pageSize
                val nextKey = if (endOfPaginationReached) null else from + state.config.pageSize
                val keys = response.map {
                    RemoteKeyEntity(movieId = it.id, prevKey = prevKey, nextKey = nextKey)
                }
                movieDb.remoteKeyDao().insertAll(keys)
                movieDb.movieDao().insertAll(response.map { dto ->
                    MovieEntity(
                        id = dto.id,
                        title = dto.title,
                        overview = dto.overview,
                        releaseDate = dto.releaseDate,
                        genres = dto.genres.joinToString(","),
                        url = dto.url,
                        category = genre
                    )
                })
            }
            return MediatorResult.Success(endOfPaginationReached = endOfPaginationReached)
        } catch (e: Exception) {
            return MediatorResult.Error(e)
        }
    }

    private suspend fun getRemoteKeyForLastItem(state: PagingState<Int, MovieEntity>): RemoteKeyEntity? {
        return state.pages.lastOrNull { it.data.isNotEmpty() }?.data?.lastOrNull()
            ?.let { movie ->
                movieDb.remoteKeyDao().remoteKeysMovieId(movie.id)
            }
    }

    private suspend fun getRemoteKeyForFirstItem(state: PagingState<Int, MovieEntity>): RemoteKeyEntity? {
        return state.pages.firstOrNull { it.data.isNotEmpty() }?.data?.firstOrNull()
            ?.let { movie ->
                movieDb.remoteKeyDao().remoteKeysMovieId(movie.id)
            }
    }

    private suspend fun getRemoteKeyClosestToCurrentPosition(
        state: PagingState<Int, MovieEntity>
    ): RemoteKeyEntity? {
        return state.anchorPosition?.let { position ->
            state.closestItemToPosition(position)?.id?.let { movieId ->
                movieDb.remoteKeyDao().remoteKeysMovieId(movieId)
            }
        }
    }
}

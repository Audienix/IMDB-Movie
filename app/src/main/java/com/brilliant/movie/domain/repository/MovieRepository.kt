package com.brilliant.movie.domain.repository

import androidx.paging.PagingData
import com.brilliant.movie.domain.model.Genre
import com.brilliant.movie.domain.model.Movie
import kotlinx.coroutines.flow.Flow

interface MovieRepository {

    fun getMovies(genre: String?): Flow<PagingData<Movie>>

    suspend fun getGenres(): Result<List<Genre>>
}
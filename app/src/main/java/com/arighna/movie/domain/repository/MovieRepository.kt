package com.arighna.movie.domain.repository

import androidx.paging.PagingData
import com.arighna.movie.domain.model.Genre
import com.arighna.movie.domain.model.Movie
import kotlinx.coroutines.flow.Flow

interface MovieRepository {

    fun getMovies(genre: String?): Flow<PagingData<Movie>>

    suspend fun getGenres(): Result<List<Genre>>
}
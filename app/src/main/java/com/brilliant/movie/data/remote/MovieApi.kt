package com.brilliant.movie.data.remote

import com.brilliant.movie.data.remote.dto.MovieDto
import retrofit2.http.GET
import retrofit2.http.Query

interface MovieApi {

    @GET("api/genres")
    suspend fun getGenres(): List<List<String>>

    @GET("api/movies")
    suspend fun getMovies(
        @Query("limit") limit: Int,
        @Query("from") from: Int,
        @Query("genre") genre: String? = null
    ): List<MovieDto>
}
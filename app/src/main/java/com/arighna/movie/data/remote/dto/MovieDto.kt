package com.arighna.movie.data.remote.dto

import com.squareup.moshi.Json

data class MovieDto(
    val id: String,
    val genres: List<String>,
    @Json(name = "release_date") val releaseDate: String,
    val title: String,
    val overview: String,
    val url: String
)
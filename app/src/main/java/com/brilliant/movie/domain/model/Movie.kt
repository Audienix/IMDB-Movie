package com.brilliant.movie.domain.model

data class Movie(
    val id: String,
    val title: String,
    val overview: String,
    val releaseYear: String,
    val genres: List<String>,
    val url: String
)

package com.arighna.movie.presentation.movie

data class MovieState(
    val genres: List<com.arighna.movie.domain.model.Genre> = emptyList(),
    val selectedGenre: com.arighna.movie.domain.model.Genre? = null,
    val lastSelectedGenre: com.arighna.movie.domain.model.Genre? = null,
    val error: String? = null
)

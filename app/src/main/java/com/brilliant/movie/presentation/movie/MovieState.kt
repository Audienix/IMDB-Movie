package com.brilliant.movie.presentation.movie

import com.brilliant.movie.domain.model.Genre

data class MovieState(
    val genres: List<Genre> = emptyList(),
    val selectedGenre: Genre? = null,
    val lastSelectedGenre: Genre? = null,
    val error: String? = null
)

package com.brilliant.movie.presentation.movie

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.brilliant.movie.domain.model.Genre
import com.brilliant.movie.domain.model.Movie
import com.brilliant.movie.domain.usecase.GetGenresUseCase
import com.brilliant.movie.domain.usecase.GetMoviesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * The ViewModel for the movie screen. This class is responsible for managing the UI state,
 * fetching movies and genres, and handling user interactions.
 */
@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class MovieViewModel @Inject constructor(
    private val getMoviesUseCase: GetMoviesUseCase,
    private val getGenresUseCase: GetGenresUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(MovieState())
    val state: StateFlow<MovieState> = _state

    private val _selectedGenre = MutableStateFlow<Genre?>(null)

    /**
     * A reactive stream of movies that automatically updates when the selected genre changes.
     * The `flatMapLatest` operator ensures that when a new genre is selected, the old movie
     * stream is cancelled and a new one is created, preventing race conditions.
     * The `cachedIn` operator caches the movie data in the ViewModel's scope, so it survives
     * configuration changes.
     */
    val movies: Flow<PagingData<Movie>> = _selectedGenre.flatMapLatest { genre ->
        getMoviesUseCase(genre?.name)
    }.cachedIn(viewModelScope)

    fun onGenreSelected(genre: Genre?) {
        val currentState = _state.value
        _state.value = currentState.copy(
            selectedGenre = genre,
            lastSelectedGenre = currentState.selectedGenre
        )
        _selectedGenre.value = genre
    }

    /**
     * Loads the list of genres from the repository and updates the UI state.
     * This is called from the UI to initialize the genre list.
     */
    fun loadGenres() {
        viewModelScope.launch {
            getGenresUseCase()
                .onSuccess { genres ->
                    _state.value = _state.value.copy(genres = genres)
                }
                .onFailure { error ->
                    _state.value = _state.value.copy(error = error.message)
                }
        }
    }
}
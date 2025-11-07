package com.brilliant.movie.presentation.movie

import android.net.Uri
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.brilliant.movie.R
import com.brilliant.movie.data.di.NoConnectivityException
import com.brilliant.movie.domain.model.Genre
import com.brilliant.movie.domain.model.Movie
import com.brilliant.movie.presentation.components.GenreDropdown
import com.brilliant.movie.presentation.components.MovieItem
import kotlinx.coroutines.launch

@Composable
fun MovieScreen(viewModel: MovieViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsState()
    val movies = viewModel.movies.collectAsLazyPagingItems()
    val snackbarHostState = remember { SnackbarHostState() }
    var showOfflineError by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val offlineFilterMessage = stringResource(id = R.string.error_offline_filter)
    val unknownErrorMessage = stringResource(id = R.string.error_unknown)

    // Handles the initial genre load
    LaunchedEffect(key1 = Unit) {
        viewModel.loadGenres()
    }

    // Handles generic errors from the genre loading
    LaunchedEffect(state.error) {
        state.error?.let {
            scope.launch { snackbarHostState.showSnackbar(it) }
        }
    }

    // Centralized error handler based on Paging LoadState
    LaunchedEffect(movies.loadState) {
        val refreshState = movies.loadState.refresh
        if (refreshState is LoadState.Error) {
            val error = refreshState.error
            if (error is NoConnectivityException) {
                if (movies.itemCount > 0) {
                    // 3. Has data, offline, changing filter -> Snackbar and revert
                    scope.launch { snackbarHostState.showSnackbar(offlineFilterMessage) }
                    viewModel.onGenreSelected(state.lastSelectedGenre)
                } else {
                    // 1. First load, offline -> Full-screen error
                    showOfflineError = true
                }
            } else {
                // Handle other generic refresh errors with a snackbar
                scope.launch { snackbarHostState.showSnackbar(error.message ?: unknownErrorMessage) }
            }
        } else {
            // If refresh succeeds, hide the full-screen error
            showOfflineError = false
        }
    }

    MovieScreenContent(
        movies = movies,
        genres = state.genres,
        selectedGenre = state.selectedGenre,
        onGenreSelected = viewModel::onGenreSelected,
        snackbarHostState = snackbarHostState,
        isOffline = showOfflineError
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MovieScreenContent(
    movies: LazyPagingItems<Movie>,
    genres: List<Genre>,
    selectedGenre: Genre?,
    onGenreSelected: (Genre?) -> Unit,
    snackbarHostState: SnackbarHostState,
    isOffline: Boolean
) {
    val context = LocalContext.current

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(text = stringResource(id = R.string.app_name)) }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (isOffline) {
                FullScreenError()
            } else if (movies.loadState.refresh is LoadState.Loading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                GenreDropdown(
                    genres = genres,
                    selectedGenre = selectedGenre,
                    onGenreSelected = onGenreSelected
                )
                MovieList(movies = movies, onMovieClicked = {
                    CustomTabsIntent.Builder().build().launchUrl(context, Uri.parse(it.url))
                })
            }
        }
    }
}

@Composable
private fun FullScreenError() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp), contentAlignment = Alignment.Center
    ) {
        Text(
            text = stringResource(id = R.string.error_offline),
            color = MaterialTheme.colorScheme.error,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun MovieList(movies: LazyPagingItems<Movie>, onMovieClicked: (Movie) -> Unit) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(movies.itemCount) { index ->
            movies[index]?.let { movie ->
                MovieItem(
                    movie = movie,
                    onMovieClicked = { onMovieClicked(movie) }
                )
            }
        }

        when (movies.loadState.append) {
            is LoadState.Error -> {
                item {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text(
                            text = stringResource(id = R.string.error_loading_more_movies),
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            is LoadState.Loading -> {
                item {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
            }

            else -> {}
        }
    }
}

package com.arighna.movie.presentation.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import com.arighna.movie.R

@Composable
fun GenreDropdown(
    genres: List<com.arighna.movie.domain.model.Genre>,
    selectedGenre: com.arighna.movie.domain.model.Genre?,
    onGenreSelected: (com.arighna.movie.domain.model.Genre?) -> Unit
) {
    var isDropdownExpanded by remember { mutableStateOf(false) }
    val screenWidth = LocalConfiguration.current.screenWidthDp.dp
    val totalMovieCount = remember(genres) { genres.sumOf { it.movieCount } }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(modifier = Modifier.wrapContentSize(Alignment.TopStart)) {
            Row(
                modifier = Modifier.clickable { isDropdownExpanded = true },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(id = R.string.select_genre),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.secondary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = selectedGenre?.name ?: stringResource(id = R.string.all_genres),
                    style = MaterialTheme.typography.bodyMedium
                )
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = stringResource(id = R.string.select_genre)
                )
            }

            if (isDropdownExpanded) {
                Popup(
                    alignment = Alignment.TopCenter,
                    properties = PopupProperties(focusable = true),
                    onDismissRequest = { isDropdownExpanded = false }
                ) {
                    Card(
                        modifier = Modifier.width(screenWidth - 32.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        LazyColumn {
                            item {
                                DropdownItemWithDivider(
                                    text = stringResource(id = R.string.genre_with_count, stringResource(id = R.string.all_genres), totalMovieCount),
                                    isSelected = selectedGenre == null,
                                    onClick = {
                                        onGenreSelected(null)
                                        isDropdownExpanded = false
                                    }
                                )
                            }
                            items(genres) { genre ->
                                DropdownItemWithDivider(
                                    text = stringResource(id = R.string.genre_with_count, genre.name, genre.movieCount),
                                    isSelected = selectedGenre == genre,
                                    onClick = {
                                        onGenreSelected(genre)
                                        isDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DropdownItemWithDivider(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(modifier = Modifier.width(24.dp)) {
                if (isSelected) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = stringResource(id = R.string.selected)
                    )
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = text, modifier = Modifier.weight(1f))
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.tertiary, thickness = 0.5.dp)
    }
}

@Preview(showBackground = true)
@Composable
fun GenreDropdownPreview() {
    _root_ide_package_.com.arighna.movie.ui.theme.IMDBMovieTheme {
        GenreDropdown(
            genres = listOf(
                _root_ide_package_.com.arighna.movie.domain.model.Genre("Action", 123),
                _root_ide_package_.com.arighna.movie.domain.model.Genre("Comedy", 456)
            ),
            selectedGenre = _root_ide_package_.com.arighna.movie.domain.model.Genre("Action", 123),
            onGenreSelected = {}
        )
    }
}

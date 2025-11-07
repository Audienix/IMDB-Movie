package com.brilliant.movie.presentation.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.brilliant.movie.R
import com.brilliant.movie.domain.model.Movie
import com.brilliant.movie.ui.theme.BrilliantMovieTheme

@Composable
fun MovieItem(
    movie: Movie,
    onMovieClicked: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onMovieClicked() },
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = stringResource(
                    id = R.string.movie_title_year,
                    movie.title,
                    movie.releaseYear
                ),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 8.dp),
                color = MaterialTheme.colorScheme.outlineVariant
            )
            Text(text = movie.overview, style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                Text(
                    text = stringResource(id = R.string.genres, movie.genres.joinToString()),
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MovieItemPreview() {
    BrilliantMovieTheme {
        MovieItem(
            movie = Movie(
                id = "1",
                title = "Movie Title",
                overview = "This is a great movie.",
                releaseYear = "2023",
                genres = listOf("Action", "Adventure"),
                url = ""
            ),
            onMovieClicked = {}
        )
    }
}

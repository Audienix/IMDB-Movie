package com.arighna.movie.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "movies")
data class MovieEntity(
    @PrimaryKey val id: String,
    val title: String,
    val overview: String,
    val releaseDate: String,
    val genres: String, // Stored as comma-separated string or use TypeConverter
    val url: String,
    val category: String? = null // To store which genre it belongs to if needed
)

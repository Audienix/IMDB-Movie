package com.arighna.movie.data.local

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.arighna.movie.data.local.entity.MovieEntity

@Dao
interface MovieDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(movies: List<MovieEntity>): List<Long>

    @Query("SELECT * FROM movies WHERE (:genre IS NULL OR genres LIKE '%' || :genre || '%')")
    fun getMovies(genre: String?): PagingSource<Int, MovieEntity>

    @Query("DELETE FROM movies")
    suspend fun clearAll(): Int
}

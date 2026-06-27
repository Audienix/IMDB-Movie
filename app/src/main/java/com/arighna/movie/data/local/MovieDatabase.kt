package com.arighna.movie.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.arighna.movie.data.local.entity.MovieEntity
import com.arighna.movie.data.local.entity.RemoteKeyEntity

@Database(
    entities = [MovieEntity::class, RemoteKeyEntity::class],
    version = 1,
    exportSchema = false
)
abstract class MovieDatabase : RoomDatabase() {
    abstract fun movieDao(): MovieDao
    abstract fun remoteKeyDao(): RemoteKeyDao
}

package com.arighna.movie.data.di

import android.content.Context
import androidx.room.Room
import com.arighna.movie.data.local.MovieDao
import com.arighna.movie.data.local.MovieDatabase
import com.arighna.movie.data.local.RemoteKeyDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideMovieDatabase(@ApplicationContext context: Context): MovieDatabase {
        return Room.databaseBuilder(
            context,
            MovieDatabase::class.java,
            "movie_db"
        ).build()
    }

    @Provides
    fun provideMovieDao(database: MovieDatabase): MovieDao {
        return database.movieDao()
    }

    @Provides
    fun provideRemoteKeyDao(database: MovieDatabase): RemoteKeyDao {
        return database.remoteKeyDao()
    }
}

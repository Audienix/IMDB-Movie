package com.arighna.movie.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.arighna.movie.data.local.entity.RemoteKeyEntity

@Dao
interface RemoteKeyDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(remoteKey: List<RemoteKeyEntity>): List<Long>

    @Query("SELECT * FROM remote_keys WHERE movieId = :movieId")
    suspend fun remoteKeysMovieId(movieId: String): RemoteKeyEntity?

    @Query("DELETE FROM remote_keys")
    suspend fun clearRemoteKeys(): Int
}

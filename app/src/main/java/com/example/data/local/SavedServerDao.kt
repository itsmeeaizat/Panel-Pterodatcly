package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.SavedServerEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SavedServerDao {
    @Query("SELECT * FROM saved_servers WHERE panelAccountId = :panelAccountId ORDER BY isFavorite DESC, name ASC")
    fun getSavedServersForAccount(panelAccountId: Long): Flow<List<SavedServerEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertServers(servers: List<SavedServerEntity>)

    @Query("UPDATE saved_servers SET isFavorite = :isFavorite WHERE identifier = :identifier")
    suspend fun setFavorite(identifier: String, isFavorite: Boolean)

    @Query("DELETE FROM saved_servers WHERE panelAccountId = :panelAccountId")
    suspend fun clearServersForAccount(panelAccountId: Long)
}

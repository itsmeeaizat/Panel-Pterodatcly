package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.data.model.PanelAccount
import kotlinx.coroutines.flow.Flow

@Dao
interface PanelAccountDao {
    @Query("SELECT * FROM panel_accounts ORDER BY createdAt DESC")
    fun getAllAccounts(): Flow<List<PanelAccount>>

    @Query("SELECT * FROM panel_accounts WHERE isActive = 1 LIMIT 1")
    fun getActiveAccountFlow(): Flow<PanelAccount?>

    @Query("SELECT * FROM panel_accounts WHERE isActive = 1 LIMIT 1")
    suspend fun getActiveAccount(): PanelAccount?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAccount(account: PanelAccount): Long

    @Query("UPDATE panel_accounts SET isActive = 0")
    suspend fun clearActiveAccounts()

    @Query("UPDATE panel_accounts SET isActive = 1 WHERE id = :accountId")
    suspend fun setActiveAccount(accountId: Long)

    @Transaction
    suspend fun setSingleActiveAccount(accountId: Long) {
        clearActiveAccounts()
        setActiveAccount(accountId)
    }

    @Delete
    suspend fun deleteAccount(account: PanelAccount)

    @Query("DELETE FROM panel_accounts WHERE id = :accountId")
    suspend fun deleteAccountById(accountId: Long)
}

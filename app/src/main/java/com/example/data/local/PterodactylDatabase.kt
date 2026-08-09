package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.model.PanelAccount
import com.example.data.model.SavedServerEntity

@Database(
    entities = [PanelAccount::class, SavedServerEntity::class],
    version = 2,
    exportSchema = false
)
abstract class PterodactylDatabase : RoomDatabase() {
    abstract fun panelAccountDao(): PanelAccountDao
    abstract fun savedServerDao(): SavedServerDao

    companion object {
        @Volatile
        private var INSTANCE: PterodactylDatabase? = null

        fun getInstance(context: Context): PterodactylDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    PterodactylDatabase::class.java,
                    "pterodactyl_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

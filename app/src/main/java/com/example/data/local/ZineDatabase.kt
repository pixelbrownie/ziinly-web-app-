package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.models.ZineEntity

@Database(entities = [ZineEntity::class], version = 1, exportSchema = false)
abstract class ZineDatabase : RoomDatabase() {
    abstract fun zineDao(): ZineDao

    companion object {
        @Volatile
        private var INSTANCE: ZineDatabase? = null

        fun getDatabase(context: Context): ZineDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ZineDatabase::class.java,
                    "ziinly_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.models.ZineEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ZineDao {
    @Query("SELECT * FROM zines ORDER BY createdAt DESC")
    fun getAllZines(): Flow<List<ZineEntity>>

    @Query("SELECT * FROM zines WHERE isPublic = 1 ORDER BY createdAt DESC")
    fun getPublicZines(): Flow<List<ZineEntity>>

    @Query("SELECT * FROM zines WHERE id = :id LIMIT 1")
    fun getZineById(id: Int): Flow<ZineEntity?>

    @Query("SELECT * FROM zines WHERE id = :id LIMIT 1")
    suspend fun getZineByIdSync(id: Int): ZineEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertZine(zine: ZineEntity): Long

    @Update
    suspend fun updateZine(zine: ZineEntity)

    @Delete
    suspend fun deleteZine(zine: ZineEntity)
}

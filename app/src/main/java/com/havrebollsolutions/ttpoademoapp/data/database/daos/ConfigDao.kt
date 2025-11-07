package com.havrebollsolutions.ttpoademoapp.data.database.daos

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.havrebollsolutions.ttpoademoapp.data.database.entities.Config

@Dao
interface ConfigDao {
    @Query("SELECT * FROM config")
    fun getConfig(): Config?

    @Insert
    suspend fun insertConfig(config: Config): Long

    @Delete
    suspend fun deleteConfig(config: Config)
}
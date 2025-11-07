package com.havrebollsolutions.ttpoademoapp.data.database.daos

import androidx.room.*
import com.havrebollsolutions.ttpoademoapp.data.database.entities.MenuItem
import kotlinx.coroutines.flow.Flow

@Dao
interface MenuItemDao {
    @Query("SELECT * FROM menu_items")
    fun getAllMenuItems(): Flow<List<MenuItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMenuItem(menuItem: MenuItem)

    @Delete
    suspend fun deleteMenuItem(menuItem: MenuItem)

    @Update
    suspend fun updateMenuItem(menuItem: MenuItem)

    @Query("SELECT * FROM menu_items WHERE id = :itemId")
    suspend fun getMenuItemById(itemId: Long): MenuItem?
}

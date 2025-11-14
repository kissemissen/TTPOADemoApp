package com.havrebollsolutions.ttpoademoapp.data.database.daos

import androidx.room.*
import com.havrebollsolutions.ttpoademoapp.data.database.entities.MenuItem
import kotlinx.coroutines.flow.Flow

@Dao
interface MenuItemDao {
    @Query("SELECT * FROM menu_items ORDER BY orderIndex ASC, id ASC") // Use orderIndex for primary sort
    fun getAllMenuItems(): Flow<List<MenuItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMenuItem(menuItem: MenuItem)

    @Delete
    suspend fun deleteMenuItem(menuItem: MenuItem)

    @Update
    suspend fun updateMenuItem(menuItem: MenuItem)

    @Query("SELECT * FROM menu_items WHERE id = :itemId")
    suspend fun getMenuItemById(itemId: Long): MenuItem?

    /**
     * Retrieves the highest current orderIndex used in the menu_items table.
     * Used when inserting a new item to place it at the end of the list.
     * @return The maximum index value, or 0 if the table is empty.
     */
    @Query("SELECT MAX(orderIndex) FROM menu_items")
    suspend fun getMaxOrderIndex(): Int
}

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

    /**
     * Retrieves the menu item with an orderIndex immediately preceding the given currentIndex.
     * This is useful for reordering operations, specifically when moving an item up.
     *
     * @param currentIndex The orderIndex of the item for which to find the previous item.
     * @return The MenuItem with the next lower orderIndex, or null if no such item exists.
     */
    @Query("SELECT * FROM menu_items WHERE orderIndex < :currentIndex ORDER BY orderIndex DESC LIMIT 1")
    suspend fun getPreviousMenuItem(currentIndex: Int): MenuItem?

    /**
     * Retrieves the menu item with an orderIndex immediately following the given currentIndex.
     * This is useful for reordering operations, specifically when moving an item down.
     *
     * @param currentIndex The orderIndex of the item for which to find the next item.
     * @return The MenuItem with the next higher orderIndex, or null if no such item exists.
     */
    @Query("SELECT * FROM menu_items WHERE orderIndex > :currentIndex ORDER BY orderIndex ASC LIMIT 1")
    suspend fun getNextMenuItem(currentIndex: Int): MenuItem?

    /**
     * This method performs a transaction to ensure that the order indices of two menu items are
     * swapped atomically. It updates the `orderIndex` of `item1` to `item2`'s original `orderIndex`,
     * and simultaneously updates the `orderIndex` of `item2` to `item1`'s original `orderIndex`.
     * This is crucial for maintaining data consistency when reordering items in the UI.
     *
     * @param item1 The first MenuItem to swap.
     * @param item2 The second MenuItem to swap.
     */
    @Transaction
    suspend fun swapOrderIndices(item1: MenuItem, item2: MenuItem) {
        // 1. Update the first item with the second item's index
        updateMenuItem(item1.copy(orderIndex = item2.orderIndex))
        // 2. Update the second item with the first item's index
        updateMenuItem(item2.copy(orderIndex = item1.orderIndex))
    }
}

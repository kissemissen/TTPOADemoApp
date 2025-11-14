package com.havrebollsolutions.ttpoademoapp.data.repository

import com.havrebollsolutions.ttpoademoapp.data.database.daos.MenuItemDao
import com.havrebollsolutions.ttpoademoapp.data.database.entities.MenuItem
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MenuItemRepository @Inject constructor(
    private val menuItemDao: MenuItemDao
) {
    fun getAllMenuItems(): Flow<List<MenuItem>> {
        return menuItemDao.getAllMenuItems()
    }

    suspend fun insertMenuItem(menuItem: MenuItem) {
        // 1. Find the current highest index
        val maxIndex = menuItemDao.getMaxOrderIndex()
        val newIndex = maxIndex + 1

        // 2. Insert the item with the new index
        val newItem = menuItem.copy(orderIndex = newIndex)
        menuItemDao.insertMenuItem(newItem)
    }

    suspend fun deleteMenuItem(menuItem: MenuItem) {
        menuItemDao.deleteMenuItem(menuItem)
    }

    suspend fun updateMenuItem(menuItem: MenuItem) {
        menuItemDao.updateMenuItem(menuItem)
    }

    suspend fun moveMenuItemDown(currentItem: MenuItem) {
        // Find the item with the next highest index
        val nextItem = menuItemDao.getNextMenuItem(currentItem.orderIndex)

        // Only swap if the next item exists (i.e., not the last item)
        if (nextItem != null) {
            menuItemDao.swapOrderIndices(currentItem, nextItem)
        }
    }

    suspend fun moveMenuItemUp(currentItem: MenuItem) {
        // Find the item with the next lowest index
        val previousItem = menuItemDao.getPreviousMenuItem(currentItem.orderIndex)

        // Only swap if the previous item exists (i.e., not the first item)
        if (previousItem != null) {
            menuItemDao.swapOrderIndices(currentItem, previousItem)
        }
    }
}
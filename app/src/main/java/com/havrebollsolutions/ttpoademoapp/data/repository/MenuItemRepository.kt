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
}
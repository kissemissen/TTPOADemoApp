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
        menuItemDao.insertMenuItem(menuItem)
    }

    suspend fun deleteMenuItem(menuItem: MenuItem) {
        menuItemDao.deleteMenuItem(menuItem)
    }

    suspend fun updateMenuItem(menuItem: MenuItem) {
        menuItemDao.updateMenuItem(menuItem)
    }
}
package com.havrebollsolutions.ttpoademoapp.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.havrebollsolutions.ttpoademoapp.data.database.daos.ConfigDao
import com.havrebollsolutions.ttpoademoapp.data.database.daos.MenuItemDao
import com.havrebollsolutions.ttpoademoapp.data.database.daos.OrderDao
import com.havrebollsolutions.ttpoademoapp.data.database.entities.Config
import com.havrebollsolutions.ttpoademoapp.data.database.entities.MenuItem
import com.havrebollsolutions.ttpoademoapp.data.database.entities.Order
import com.havrebollsolutions.ttpoademoapp.data.database.entities.OrderItem

@Database(entities = [MenuItem::class, Order::class, OrderItem::class, Config::class], version = 3, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun menuItemDao(): MenuItemDao
    abstract fun orderDao(): OrderDao
    abstract fun configDao(): ConfigDao

}
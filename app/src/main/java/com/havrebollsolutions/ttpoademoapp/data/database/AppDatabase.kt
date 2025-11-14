package com.havrebollsolutions.ttpoademoapp.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.havrebollsolutions.ttpoademoapp.data.database.daos.ConfigDao
import com.havrebollsolutions.ttpoademoapp.data.database.daos.MenuItemDao
import com.havrebollsolutions.ttpoademoapp.data.database.daos.OrderDao
import com.havrebollsolutions.ttpoademoapp.data.database.entities.Config
import com.havrebollsolutions.ttpoademoapp.data.database.entities.MenuItem
import com.havrebollsolutions.ttpoademoapp.data.database.entities.Order
import com.havrebollsolutions.ttpoademoapp.data.database.entities.OrderItem

val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // Step 1: Add the new column (orderIndex)
        db.execSQL("ALTER TABLE menu_items ADD COLUMN orderIndex INTEGER NOT NULL DEFAULT 0")

        // Step 2: Update existing items to have sequential, unique indices based on their ID
        db.execSQL("""
            UPDATE menu_items 
            SET orderIndex = (
                SELECT COUNT(T2.id) 
                FROM menu_items AS T2 
                WHERE T2.id <= menu_items.id
            )
        """)
    }
}

@Database(entities = [MenuItem::class, Order::class, OrderItem::class, Config::class], version = 4, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun menuItemDao(): MenuItemDao
    abstract fun orderDao(): OrderDao
    abstract fun configDao(): ConfigDao

}
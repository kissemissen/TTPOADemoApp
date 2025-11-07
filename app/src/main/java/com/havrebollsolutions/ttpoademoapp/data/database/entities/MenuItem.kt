package com.havrebollsolutions.ttpoademoapp.data.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "menu_items")
data class MenuItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val description: String,
    val price: Double,
    val vatRate: Double,
    val quantityInStock: Int,
    val imagePath: String?
)

package com.havrebollsolutions.ttpoademoapp.data.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "orders")
data class Order(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val totalAmount: Double,
    val paymentMethod: String,
    val transactionId: String,
    val timestamp: Long = System.currentTimeMillis()
)

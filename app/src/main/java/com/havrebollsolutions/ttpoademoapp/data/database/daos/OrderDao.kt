package com.havrebollsolutions.ttpoademoapp.data.database.daos

import androidx.room.*
import com.havrebollsolutions.ttpoademoapp.data.database.entities.Order
import com.havrebollsolutions.ttpoademoapp.data.database.entities.OrderItem
import com.havrebollsolutions.ttpoademoapp.data.database.entities.OrderWithItems
import kotlinx.coroutines.flow.Flow

@Dao
interface OrderDao {
    @Insert
    suspend fun insertOrder(order: Order): Long

    @Insert
    suspend fun insertOrderItems(orderItems: List<OrderItem>)

    @Query("SELECT * FROM orders ORDER BY timestamp DESC")
    fun getAllOrders(): Flow<List<Order>>

    // This is crucial. Without a method that returns OrderWithItems, the processor might fail.
    @Transaction
    @Query("SELECT * FROM orders")
    fun getOrdersWithItems(): Flow<List<OrderWithItems>>
}
package com.havrebollsolutions.ttpoademoapp.data.repository

import com.havrebollsolutions.ttpoademoapp.data.database.daos.OrderDao
import com.havrebollsolutions.ttpoademoapp.data.database.entities.Order
import com.havrebollsolutions.ttpoademoapp.data.database.entities.OrderItem
import com.havrebollsolutions.ttpoademoapp.viewmodel.CartItem
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OrderRepository @Inject constructor(
    private val orderDao: OrderDao
) {
    suspend fun saveOrder(order: Order, cartItems: List<CartItem>) {
        // Step 1: Insert the order and get the auto-generated ID
        val orderId = orderDao.insertOrder(order)
        // Step 2: Map the CartItem list to OrderItem entities, including the new orderId
        val orderItems = cartItems.map { cartItem ->
            OrderItem(
                orderId = orderId, // Use the ID from the inserted Order
                menuItemId = cartItem.menuItem.id,
                quantity = cartItem.quantity,
                priceAtTimeOfPurchase = cartItem.menuItem.price
            )
        }

        // Step 3: Insert the list of order items
        orderDao.insertOrderItems(orderItems)
    }
}
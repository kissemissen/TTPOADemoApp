package com.havrebollsolutions.ttpoademoapp.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.havrebollsolutions.ttpoademoapp.viewmodel.CartItem
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.RemoveCircle
import androidx.compose.ui.text.style.TextAlign
import com.havrebollsolutions.ttpoademoapp.data.models.Currency


@Composable
fun CartItemRow(
    selectedCurrency: Currency,
    cartItem: CartItem,
    onQuantityChanged: (Int) -> Unit,
    onRemoveItem: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = cartItem.menuItem.name,
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = "${selectedCurrency.symbol} ${"%.2f".format(cartItem.menuItem.price)}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { onQuantityChanged(cartItem.quantity - 1) }) {
                Icon(Icons.Default.RemoveCircle, contentDescription = "Decrease quantity")
            }
            Text(
                text = cartItem.quantity.toString(),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.width(24.dp),
                textAlign = TextAlign.Center
            )
            IconButton(onClick = { onQuantityChanged(cartItem.quantity + 1) }) {
                Icon(Icons.Default.AddCircle, contentDescription = "Increase quantity")
            }
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(onClick = onRemoveItem) {
                Icon(Icons.Default.Delete, contentDescription = "Remove item")
            }
        }
    }
}
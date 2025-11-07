package com.havrebollsolutions.ttpoademoapp.ui.components

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.havrebollsolutions.ttpoademoapp.R
import com.havrebollsolutions.ttpoademoapp.data.database.entities.MenuItem
import androidx.core.net.toUri
import coil3.compose.AsyncImage
import com.havrebollsolutions.ttpoademoapp.data.models.Currency


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MenuItemCard(
    selectedCurrency: Currency,
    menuItem: MenuItem,
    onAddItemToCart: (MenuItem) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = { onAddItemToCart(menuItem) }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Assuming image path is a resource name for now
            val imageResId = menuItem.imagePath?.let {
                // Get resource ID dynamically
                // For a real app, you'd handle image loading from a URI or URL
                R.drawable.placeholder_image // Placeholder
            } ?: R.drawable.placeholder_image

            // Display the image from resource
            if (menuItem.imagePath != null) {
                AsyncImage(
                    model = menuItem.imagePath.toUri(),
                    contentDescription = menuItem.name,
                    placeholder = painterResource(id = imageResId),
                    error = painterResource(id = imageResId),
                    modifier = Modifier
                        .size(80.dp),
                    contentScale = ContentScale.Crop
                )
            } else {
                Image(
                    painter = painterResource(id = imageResId),
                    contentDescription = menuItem.name,
                    modifier = Modifier
                        .size(80.dp)
                        .aspectRatio(1f),
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = menuItem.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = menuItem.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = stringResource(R.string.price_tag, selectedCurrency.symbol, "%.2f".format(menuItem.price)),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            IconButton(onClick = { onAddItemToCart(menuItem) }) {
                Icon(
                    imageVector = Icons.Default.AddCircle,
                    contentDescription = "Add to cart",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .size(32.dp)
                )
            }
        }
    }
}

@Composable
@Preview(showBackground = true)
fun MenuItemCardPreview() {
    val previewMenuItem = MenuItem(
        id = 1,
        name = "Example Item",
        description = "This is a preview item for the menu item card.",
        price = 9.0,
        imagePath = null,
        vatRate = 12.0,
        quantityInStock = 100
    )
    MenuItemCard(menuItem = previewMenuItem, onAddItemToCart = {}, selectedCurrency = Currency.SEK)
}
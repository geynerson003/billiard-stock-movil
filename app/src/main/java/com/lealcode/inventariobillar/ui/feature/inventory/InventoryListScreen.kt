package com.lealcode.inventariobillar.ui.feature.inventory

import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.lealcode.inventariobillar.data.model.Product
import com.lealcode.inventariobillar.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
/**
 * Pantalla que lista productos y expone acciones del inventario.
 */
fun InventoryListScreen(
    onAddProduct: () -> Unit,
    onEditProduct: (Product) -> Unit,
    onDeleteProduct: (Product) -> Unit,
    onProductClick: (Product) -> Unit,
    viewModel: InventoryViewModel = hiltViewModel()
) {
    val products by viewModel.products.collectAsState()
    var animateList by remember { mutableStateOf(false) }

    LaunchedEffect(products) { animateList = true }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Inventario",
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = AzulAcento
                        )
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = FondoTarjeta
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddProduct,
                containerColor = AzulAcento,
                contentColor = Color.White,
                shape = CircleShape,
                elevation = FloatingActionButtonDefaults.elevation(12.dp),
                modifier = Modifier.shadow(12.dp, CircleShape)
            ) {
                Icon(Icons.Outlined.Add, contentDescription = "Agregar producto", modifier = Modifier.size(32.dp))
            }
        }
    ) { innerPadding ->
        Box(
            Modifier
                .fillMaxSize()
                .background(FondoClaro)
                .padding(innerPadding)
        ) {
            AnimatedVisibility(
                visible = products.isEmpty(),
                enter = fadeIn(tween(600)) + scaleIn(tween(600)),
                exit = fadeOut() + scaleOut()
            ) {
                Column(
                    Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Outlined.Inventory2,
                        contentDescription = null,
                        tint = AzulAcento,
                        modifier = Modifier.size(64.dp)
                    )
                    Text(
                        "No hay productos en inventario",
                        color = AzulAcento,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            AnimatedVisibility(
                visible = products.isNotEmpty() && animateList,
                enter = fadeIn(tween(700)) + scaleIn(tween(700)),
                exit = fadeOut() + scaleOut()
            ) {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier
                        .fillMaxSize()
                ) {
                    items(products, key = { it.id }) { product ->
                        val isLowStock = product.stock <= product.minStock
                        val animScale by animateFloatAsState(
                            targetValue = 1f,
                            animationSpec = tween(600),
                            label = "cardScale"
                        )
                        InventoryProductCard(
                            product = product,
                            isLowStock = isLowStock,
                            modifier = Modifier
                                .graphicsLayer(scaleX = animScale, scaleY = animScale)
                                .clickable { onProductClick(product) },
                            onDelete = { onDeleteProduct(product) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun InventoryProductCard(
    product: Product,
    isLowStock: Boolean,
    modifier: Modifier = Modifier,
    onDelete: () -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }


    Card(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 90.dp)
            .shadow(8.dp, shape = MaterialTheme.shapes.large)
            .border(
                1.dp,
                if (isLowStock) RojoAcento.copy(alpha = 0.3f) else AzulAcento.copy(alpha = 0.15f),
                MaterialTheme.shapes.large
            ),
        colors = CardDefaults.cardColors(containerColor = FondoTarjeta),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        (if (isLowStock) RojoAcento else AzulAcento).copy(alpha = 0.12f),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Inventory2,
                    contentDescription = null,
                    tint = if (isLowStock) RojoAcento else AzulAcento,
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = product.name,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                    )
                    if (isLowStock) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Badge(
                            containerColor = RojoAcento,
                            contentColor = Color.White
                        ) {
                            Icon(
                                Icons.Outlined.WarningAmber,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Text("Stock bajo", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Stock: ${product.stock}",
                    color = if (isLowStock) RojoAcento else GrisTextoSecundario,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "Precio: $${product.salePrice}",
                    color = GrisTextoSecundario,
                    fontWeight = FontWeight.Normal
                )
            }

            IconButton(
                onClick = { showDeleteDialog = true },
                modifier = Modifier
                    .size(40.dp)
                    .background(RojoAcento.copy(alpha = 0.10f), shape = CircleShape)
            ) {
                Icon(
                    Icons.Outlined.Delete,
                    contentDescription = "Eliminar",
                    tint = RojoAcento,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }

    if (showDeleteDialog) {
        DeleteProductDialog(
            productName = product.name,
            onConfirm = {
                showDeleteDialog = false
                onDelete()
            },
            onDismiss = { showDeleteDialog = false }
        )
    }
}

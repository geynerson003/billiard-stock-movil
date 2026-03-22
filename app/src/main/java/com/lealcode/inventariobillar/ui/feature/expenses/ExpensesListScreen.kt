package com.lealcode.inventariobillar.ui.feature.expenses

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
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.lealcode.inventariobillar.data.model.Expense
import com.lealcode.inventariobillar.data.model.Table
import com.lealcode.inventariobillar.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
/**
 * Pantalla que lista gastos y permite gestionarlos.
 */
fun ExpensesListScreen(
    onAddExpense: () -> Unit,
    onExpenseClick: (Expense) -> Unit,
    onDeleteExpense: (Expense) -> Unit = {},
    viewModel: ExpensesViewModel = hiltViewModel()
) {
    val expenses by viewModel.expenses.collectAsState()
    var animateList by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var expenseToDelete by remember { mutableStateOf<Expense?>(null) }

    LaunchedEffect(expenses) {
        animateList = true
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Gastos",
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = RojoAcento
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
                onClick = onAddExpense,
                containerColor = RojoAcento,
                contentColor = Color.White,
                shape = CircleShape,
                elevation = FloatingActionButtonDefaults.elevation(12.dp),
                modifier = Modifier.shadow(12.dp, CircleShape)
            ) {
                Icon(Icons.Outlined.Add, contentDescription = "Agregar gasto", modifier = Modifier.size(32.dp))
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
                visible = expenses.isEmpty(),
                enter = fadeIn(tween(600)) + scaleIn(tween(600)),
                exit = fadeOut() + scaleOut()
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Outlined.ReceiptLong,
                        contentDescription = null,
                        tint = RojoAcento,
                        modifier = Modifier.size(64.dp)
                    )
                    Text(
                        "No hay gastos registrados",
                        color = RojoAcento,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            AnimatedVisibility(
                visible = expenses.isNotEmpty() && animateList,
                enter = fadeIn(tween(700)) + scaleIn(tween(700)),
                exit = fadeOut() + scaleOut()
            ) {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier
                        .fillMaxSize()
                ) {
                    items(expenses, key = { it.id }) { expense ->
                        val animScale by animateFloatAsState(
                            targetValue = 1f,
                            animationSpec = tween(600),
                            label = "cardScale"
                        )
                        ExpenseCard(
                            expense = expense,
                            onDelete = {
                                expenseToDelete = expense
                                showDeleteDialog = true
                            },
                            modifier = Modifier
                                .graphicsLayer(scaleX = animScale, scaleY = animScale)
                                .clickable { onExpenseClick(expense) }
                        )
                    }
                }
            }
        }
        if (showDeleteDialog && expenseToDelete != null) {
            AlertDialog(
                onDismissRequest = {
                    showDeleteDialog = false
                    expenseToDelete = null
                },
                title = {
                    Text(
                        "Eliminar Gasto",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = RojoAcento
                        )
                    )
                },
                text = {
                    Text(
                        "¿Estás seguro de que quieres eliminar el gasto \"${expenseToDelete?.description}\"? Esta acción no se puede deshacer.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            expenseToDelete?.let { expense ->
                                onDeleteExpense(expense)
                            }
                            showDeleteDialog = false
                            expenseToDelete= null
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = RojoAcento,
                            contentColor = Color.White
                        )
                    ) {
                        Text("Eliminar")
                    }
                },
                dismissButton = {
                    OutlinedButton(
                        onClick = {
                            showDeleteDialog = false
                            expenseToDelete = null
                        }
                    ) {
                        Text("Cancelar")
                    }
                },
                containerColor = FondoTarjeta,
                shape = MaterialTheme.shapes.large
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ExpenseCard(
    expense: Expense,
    onDelete: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val icon = Icons.Outlined.ReceiptLong
    val badgeColor = OrangeBright
    val badgeText = expense.category.takeIf { it.isNotBlank() } ?: "General"

    Card(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 90.dp)
            .shadow(8.dp, shape = MaterialTheme.shapes.large)
            .border(1.dp, RojoAcento.copy(alpha = 0.15f), MaterialTheme.shapes.large),
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
                    .background(RojoAcento.copy(alpha = 0.12f), shape = CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = RojoAcento,
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = expense.description,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Badge(
                        containerColor = badgeColor,
                        contentColor = Color.White
                    ) {
                        Text(
                            text = badgeText,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Monto: $${expense.amount}",
                    color = GrisTextoSecundario,
                    fontWeight = FontWeight.SemiBold
                )

                /*Boton eliminar*/

            }
            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(40.dp),
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = RojoAcento.copy(alpha = 0.1f),
                    contentColor = RojoAcento
                )
            ) {
                Icon(
                    imageVector = Icons.Outlined.Delete,
                    contentDescription = "Eliminar gasto",
                    modifier = Modifier.size(20.dp)
                )
            }
        }

    }
}

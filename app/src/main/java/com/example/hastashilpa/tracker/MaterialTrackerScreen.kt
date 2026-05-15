package com.example.hastashilpa.tracker

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.*
import com.example.hastashilpa.products.ProductViewModel

data class MaterialLog(
    val id: String = UUID.randomUUID().toString(),
    val productName: String = "",
    val materialsUsed: String = "",
    val batchQuantity: Int = 0,
    val date: Long = System.currentTimeMillis()
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MaterialTrackerScreen(
    productViewModel: ProductViewModel,
    materialViewModel: MaterialViewModel
) {
    val logs by materialViewModel.logs
    val isLoading by materialViewModel.isLoading
    val products by productViewModel.products
    var showDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Material Tracker", color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.White)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showDialog = true },
                containerColor = Color(0xFF2E7D32),
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Log Batch")
            }
        },
        containerColor = Color(0xFFF1F8E9)
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            if (isLoading && logs.isEmpty()) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = Color(0xFF2E7D32)
                )
            } else if (logs.isEmpty()) {
                Text(
                    "No logs yet. Start by tracking a batch!",
                    modifier = Modifier.align(Alignment.Center),
                    color = Color.Gray
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(logs) { log ->
                        MaterialLogCard(log)
                    }
                }
            }
        }

        if (showDialog) {
            AddLogDialog(
                productNames = products.map { it.name },
                onDismiss = { showDialog = false },
                onConfirm = { productName, materials, quantity ->
                    materialViewModel.addLog(productName, materials, quantity)
                    showDialog = false
                }
            )
        }
    }
}

@Composable
fun MaterialLogCard(log: MaterialLog) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = log.productName,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2E7D32)
                    )
                    Text(
                        text = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date(log.date)),
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
                Surface(
                    color = Color(0xFFE8F5E9),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "${log.batchQuantity} pcs",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2E7D32)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(thickness = 0.5.dp, color = Color.LightGray)
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Materials Used:",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = Color.Gray
            )
            Text(
                text = log.materialsUsed,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddLogDialog(
    productNames: List<String>,
    onDismiss: () -> Unit,
    onConfirm: (String, String, Int) -> Unit
) {
    var selectedProduct by remember { mutableStateOf(if (productNames.isNotEmpty()) productNames[0] else "") }
    var materialsUsed by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Log Production Batch") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // Dropdown for Product Selection
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = selectedProduct,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Select Product") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth(),
                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        productNames.forEach { name ->
                            DropdownMenuItem(
                                text = { Text(name) },
                                onClick = {
                                    selectedProduct = name
                                    expanded = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = materialsUsed,
                    onValueChange = { materialsUsed = it },
                    label = { Text("Materials Used (e.g. 5m Bamboo, 2kg Cane)") },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("List materials and quantities...") }
                )
                OutlinedTextField(
                    value = quantity,
                    onValueChange = { quantity = it },
                    label = { Text("Batch Quantity (pcs)") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                        keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { 
                    if (selectedProduct.isNotEmpty()) {
                        onConfirm(selectedProduct, materialsUsed, quantity.toIntOrNull() ?: 0)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                enabled = selectedProduct.isNotEmpty() && materialsUsed.isNotEmpty()
            ) {
                Text("Save Log")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

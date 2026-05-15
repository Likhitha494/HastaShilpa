package com.example.hastashilpa.calculator

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.hastashilpa.products.ProductViewModel
import com.example.hastashilpa.data.Product
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.RequestOptions
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PriceSuggesterScreen(productViewModel: ProductViewModel) {
    val products by productViewModel.products
    val coroutineScope = rememberCoroutineScope()
    
    var selectedProduct by remember { mutableStateOf<Product?>(null) }
    var materialCostPerUnit by remember { mutableStateOf("") }
    var unitsUsed by remember { mutableStateOf("") }
    var laborHours by remember { mutableStateOf("") }
    var hourlyRate by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }
    
    var aiSuggestedPrice by remember { mutableStateOf<String?>(null) }
    var isGenerating by remember { mutableStateOf(false) }

    // API Key - Free Version
    val apiKey = "AIzaSyDhluBmiQ9t1QLck1jmGp1jX_QEjbrTicQ"

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("AI Price Suggester", color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = Color(0xFFF1F8E9)
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                "Select a product and enter costs. Our AI will suggest a fair market price.",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )

            // Product Selection Dropdown
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded }
            ) {
                OutlinedTextField(
                    value = selectedProduct?.name ?: "Select Product",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Product to Price") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier.menuAnchor().fillMaxWidth(),
                    colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    products.forEach { product ->
                        DropdownMenuItem(
                            text = { Text(product.name) },
                            onClick = {
                                selectedProduct = product
                                expanded = false
                                aiSuggestedPrice = null 
                            }
                        )
                    }
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Cost Breakdown", fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
                    OutlinedTextField(
                        value = materialCostPerUnit,
                        onValueChange = { materialCostPerUnit = it },
                        label = { Text("Material Cost per Unit (₹)") },
                        modifier = Modifier.fillMaxWidth(),
                        prefix = { Text("₹") }
                    )
                    OutlinedTextField(
                        value = unitsUsed,
                        onValueChange = { unitsUsed = it },
                        label = { Text("Total Units Used") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = laborHours,
                        onValueChange = { laborHours = it },
                        label = { Text("Labor Hours") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = hourlyRate,
                        onValueChange = { hourlyRate = it },
                        label = { Text("Hourly Wage (₹)") },
                        modifier = Modifier.fillMaxWidth(),
                        prefix = { Text("₹") }
                    )

                    Button(
                        onClick = {
                            if (selectedProduct != null && materialCostPerUnit.isNotEmpty() && laborHours.isNotEmpty()) {
                                isGenerating = true
                                aiSuggestedPrice = null
                                coroutineScope.launch {
                                    val prompt = "Product: ${selectedProduct!!.name}, Material: $materialCostPerUnit, Units: $unitsUsed, Labor Hours: $laborHours, Hourly Wage: $hourlyRate. Suggest a single numerical selling price in INR. Return only the number."
                                    
                                    try {
                                        val model = GenerativeModel(
                                            modelName = "gemini-1.5-flash",
                                            apiKey = apiKey.trim(),
                                            requestOptions = RequestOptions(apiVersion = "v1beta")
                                        )
                                        
                                        val response = withContext(Dispatchers.IO) {
                                            model.generateContent(prompt)
                                        }
                                        val resultText = response.text?.trim()?.filter { it.isDigit() || it == '.' }
                                        if (!resultText.isNullOrEmpty()) {
                                            aiSuggestedPrice = resultText
                                        } else {
                                            throw Exception("Empty AI response")
                                        }
                                    } catch (e: Exception) {
                                        // SMART FALLBACK: If AI fails, use expert formula
                                        val material = materialCostPerUnit.toDoubleOrNull() ?: 0.0
                                        val units = unitsUsed.toDoubleOrNull() ?: 1.0
                                        val hours = laborHours.toDoubleOrNull() ?: 0.0
                                        val wage = hourlyRate.toDoubleOrNull() ?: 0.0
                                        
                                        val totalCost = (material * units) + (hours * wage)
                                        val suggestedPrice = (totalCost * 1.35).toInt() // 35% Profit Margin
                                        
                                        aiSuggestedPrice = suggestedPrice.toString()
                                    }
                                    
                                    isGenerating = false
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                        enabled = !isGenerating && selectedProduct != null
                    ) {
                        if (isGenerating) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                        } else {
                            Icon(Icons.Default.Calculate, null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Get AI Suggestion")
                        }
                    }
                }
            }

            aiSuggestedPrice?.let { result ->
                val isError = result.startsWith("Error")
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = if (isError) Color(0xFFFFEBEE) else Color(0xFFE8F5E9)),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, if (isError) Color.Red else Color(0xFF2E7D32))
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(if (isError) "Calculation Alert" else "AI Predicted Value", fontWeight = FontWeight.Bold, color = if (isError) Color.Red else Color(0xFF2E7D32))
                        Text(
                            text = if (isError) result else "₹$result",
                            style = if (isError) MaterialTheme.typography.bodyMedium else MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Bold,
                            color = if (isError) Color.Red else Color(0xFF1B5E20)
                        )
                        
                        if (!isError) {
                            Text("Does this price seem fair for your ${selectedProduct?.name}?", fontSize = 12.sp, color = Color.Gray)
                            
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                Button(
                                    onClick = {
                                        selectedProduct?.let { product ->
                                            productViewModel.updateProductPrice(product.id, result)
                                            aiSuggestedPrice = null
                                            selectedProduct = null
                                        }
                                    },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))
                                ) {
                                    Text("Approve")
                                }
                                OutlinedButton(
                                    onClick = { aiSuggestedPrice = null },
                                    modifier = Modifier.weight(1f),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.Red),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red)
                                ) {
                                    Text("Reject")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

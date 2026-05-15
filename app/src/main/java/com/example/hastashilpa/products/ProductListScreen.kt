package com.example.hastashilpa.products

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.hastashilpa.auth.AuthViewModel
import com.example.hastashilpa.data.Product

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductScreen(
    productViewModel: ProductViewModel,
    authViewModel: AuthViewModel,
    onLogout: () -> Unit,
    onNavigateToBlueprint: (Product) -> Unit
) {
    val products by productViewModel.products
    val favorites by productViewModel.favorites
    val isLoading by productViewModel.isLoading

    var searchQuery by remember { mutableStateOf("") }
    var showFavoritesOnly by remember { mutableStateOf(false) }

    var productToEdit by remember { mutableStateOf<Product?>(null) }
    var showAddDialog by remember { mutableStateOf(false) }

    val filteredProducts = products.filter { product ->
        (product.name.contains(searchQuery, true) ||
                product.description.contains(searchQuery, true)) &&
                (!showFavoritesOnly || favorites.contains(product.id))
    }

    Scaffold(
        topBar = {
            Column(modifier = Modifier.background(Color.White)) {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            "HastaShilpa",
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2E7D32)
                        )
                    },
                    actions = {
                        IconButton(onClick = { showFavoritesOnly = !showFavoritesOnly }) {
                            Icon(
                                if (showFavoritesOnly) Icons.Default.Favorite else Icons.Outlined.FavoriteBorder,
                                contentDescription = "Favorites",
                                tint = if (showFavoritesOnly) Color.Red else Color(0xFF2E7D32)
                            )
                        }
                        IconButton(onClick = {
                            authViewModel.signOut()
                            onLogout()
                        }) {
                            Icon(
                                Icons.AutoMirrored.Filled.ExitToApp,
                                contentDescription = "Logout",
                                tint = Color(0xFF2E7D32)
                            )
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.White)
                )

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    placeholder = { Text("Search bamboo creations...") },
                    leadingIcon = { Icon(Icons.Default.Search, null) },
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = Color(0xFF2E7D32),
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, null)
            }
        },
        containerColor = Color(0xFFF1F8E9)
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            if (isLoading && products.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color(0xFF2E7D32))
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        val title = if (showFavoritesOnly) "Your Favorites ❤️" else "Bamboo Creations 🌿"
                        Text(
                            text = title,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    items(filteredProducts) { product ->
                        ProductCard(
                            product = product,
                            isFavorite = favorites.contains(product.id),
                            onDelete = { productViewModel.deleteProduct(product.id) },
                            onEdit = { productToEdit = product },
                            onToggleFavorite = { productViewModel.toggleFavorite(product.id) },
                            onViewBlueprint = { onNavigateToBlueprint(product) }
                        )
                    }
                }
            }
        }

        if (showAddDialog) {
            ProductDialog(
                onDismiss = { showAddDialog = false },
                onConfirm = { name, price, desc, uri, blueUri, meas, cat ->
                    productViewModel.addProduct(name, price, desc, uri, blueUri, meas, cat)
                    showAddDialog = false
                }
            )
        }

        if (productToEdit != null) {
            ProductDialog(
                product = productToEdit,
                onDismiss = { productToEdit = null },
                onConfirm = { name, price, desc, uri, blueUri, meas, cat ->
                    val updated = productToEdit!!.copy(
                        name = name,
                        price = price,
                        description = desc,
                        category = cat,
                        measurements = meas
                    )
                    productViewModel.updateProduct(updated, uri, blueUri)
                    productToEdit = null
                }
            )
        }
    }
}

@Composable
fun ProductCard(
    product: Product,
    isFavorite: Boolean,
    onDelete: () -> Unit,
    onEdit: () -> Unit,
    onToggleFavorite: () -> Unit,
    onViewBlueprint: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(Color.White)
    ) {
        Column {
            AsyncImage(
                model = product.image,
                contentDescription = product.name,
                modifier = Modifier.fillMaxWidth().height(220.dp),
                contentScale = ContentScale.Crop
            )

            Column(modifier = Modifier.padding(16.dp)) {
                Text(product.name, fontWeight = FontWeight.Bold, fontSize = 22.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Text(if (product.price.isEmpty()) "Set Price in Suggester 📈" else "₹${product.price}", color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Text(product.description)
                Spacer(modifier = Modifier.height(12.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = onEdit) { Text("Edit") }
                    Button(onClick = onDelete, colors = ButtonDefaults.buttonColors(containerColor = Color.Red)) { Text("Delete") }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = onToggleFavorite,
                    colors = ButtonDefaults.buttonColors(containerColor = if (isFavorite) Color.Red else Color.Gray)
                ) {
                    Text(if (isFavorite) "Remove Favorite" else "Add Favorite")
                }

                if (product.blueprintImage.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = onViewBlueprint,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))
                    ) {
                        Icon(Icons.Default.Architecture, null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("View Blueprint")
                    }
                }
            }
        }
    }
}

@Composable
fun ProductDialog(
    product: Product? = null,
    onDismiss: () -> Unit,
    onConfirm: (String, String, String, Uri?, Uri?, String, String) -> Unit
) {
    var name by remember { mutableStateOf(product?.name ?: "") }
    var desc by remember { mutableStateOf(product?.description ?: "") }
    var measurements by remember { mutableStateOf(product?.measurements ?: "") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var selectedBlueprintUri by remember { mutableStateOf<Uri?>(null) }

    val imagePickerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        selectedImageUri = uri
    }

    val blueprintPickerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        selectedBlueprintUri = uri
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (product == null) "Add Product" else "Edit Product") },
        text = {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                item {
                    OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Name") }, modifier = Modifier.fillMaxWidth())
                }
                item {
                    OutlinedTextField(value = measurements, onValueChange = { measurements = it }, label = { Text("Measurements") }, modifier = Modifier.fillMaxWidth())
                }
                item {
                    OutlinedTextField(value = desc, onValueChange = { desc = it }, label = { Text("Description") }, modifier = Modifier.fillMaxWidth(), minLines = 3)
                }
                item {
                    Text("Product Banner", fontWeight = FontWeight.SemiBold)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(onClick = { imagePickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }, modifier = Modifier.weight(1f)) {
                            Icon(Icons.Default.AddAPhoto, null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Upload")
                        }
                        Button(
                            onClick = {
                                if (name.isNotEmpty()) {
                                    // Using a prompt-based image generator for "Auto-Generate"
                                    val prompt = "${name} made up of bamboo, high quality, professional product photography, neutral background"
                                    val encodedPrompt = java.net.URLEncoder.encode(prompt, "UTF-8")
                                    selectedImageUri = Uri.parse("https://image.pollinations.ai/prompt/$encodedPrompt")
                                }
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))
                        ) {
                            Icon(Icons.Default.AutoAwesome, null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Auto-Generate")
                        }
                    }

                    if (selectedImageUri != null || product?.image?.isNotEmpty() == true) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Box(modifier = Modifier.fillMaxWidth().height(150.dp).clip(RoundedCornerShape(12.dp)).background(Color.LightGray.copy(alpha = 0.3f))) {
                            AsyncImage(model = selectedImageUri ?: product?.image, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(name, product?.price ?: "", desc, selectedImageUri, null, measurements, "Bamboo") }) {
                Text(if (product == null) "Add" else "Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
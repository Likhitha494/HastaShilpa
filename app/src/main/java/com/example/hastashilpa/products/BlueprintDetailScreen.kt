package com.example.hastashilpa.products

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.hastashilpa.data.Product

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BlueprintDetailScreen(
    product: Product,
    onBack: () -> Unit
) {
    var scale by remember { mutableStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    val state = rememberTransformableState { zoomChange, offsetChange, _ ->
        scale *= zoomChange
        offset += offsetChange
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Blueprint: ${product.name}") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = Color.Black
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            // Measurements Bar
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color(0xFF2E7D32),
                contentColor = Color.White
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Measurements: ${product.measurements.ifEmpty { "Not specified" }}",
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
                    .clipToBounds()
                    .background(Color.Black)
            ) {
                if (product.blueprintImage.isNotEmpty()) {
                    AsyncImage(
                        model = product.blueprintImage,
                        contentDescription = "Blueprint",
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer(
                                scaleX = scale.coerceIn(1f, 5f),
                                scaleY = scale.coerceIn(1f, 5f),
                                translationX = offset.x,
                                translationY = offset.y
                            )
                            .transformable(state = state),
                        contentScale = ContentScale.Fit
                    )
                } else {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No blueprint available for this product", color = Color.Gray)
                    }
                }
            }
        }
    }
}

private fun Modifier.clipToBounds() = this.graphicsLayer(clip = true)

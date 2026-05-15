package com.example.hastashilpa.products

import android.net.Uri
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.example.hastashilpa.data.Product
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import java.util.UUID

class ProductViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()
    private val auth = com.google.firebase.auth.FirebaseAuth.getInstance()
    private val productsCollection = db.collection("products")
    private val favoritesCollection = db.collection("users")

    private val _products = mutableStateOf<List<Product>>(emptyList())
    val products: State<List<Product>> = _products

    private val _favorites = mutableStateOf<Set<String>>(emptySet())
    val favorites: State<Set<String>> = _favorites

    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoading

    private val _error = mutableStateOf<String?>(null)
    val error: State<String?> = _error

    init {
        fetchProducts()
        fetchFavorites()
    }

    private fun fetchFavorites() {
        val userId = auth.currentUser?.uid ?: return
        favoritesCollection.document(userId).collection("favorites")
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    _favorites.value = snapshot.documents.map { it.id }.toSet()
                }
            }
    }

    fun toggleFavorite(productId: String) {
        val userId = auth.currentUser?.uid ?: return
        val docRef = favoritesCollection.document(userId).collection("favorites").document(productId)
        
        if (favorites.value.contains(productId)) {
            docRef.delete()
        } else {
            docRef.set(mapOf("timestamp" to System.currentTimeMillis()))
        }
    }

    fun fetchProducts() {
        _isLoading.value = true
        productsCollection.orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, e ->
                _isLoading.value = false
                if (e != null) {
                    _error.value = e.message
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    _products.value = snapshot.toObjects(Product::class.java)
                }
            }
    }

    fun addProduct(
        name: String, 
        price: String, 
        description: String, 
        imageUri: Uri?, 
        blueprintUri: Uri?,
        measurements: String,
        category: String
    ) {
        if (name.isEmpty()) {
            _error.value = "Name is required"
            return
        }

        _isLoading.value = true
        
        // Handle image upload and blueprint upload
        if (imageUri != null) {
            val uriString = imageUri.toString()
            if (uriString.startsWith("http")) {
                // It's a web URL from "Auto-Find", save it directly
                if (blueprintUri != null) {
                    uploadFile(blueprintUri, "blueprints") { blueprintUrl ->
                        saveProductToFirestore(name, price, description, uriString, blueprintUrl, measurements, category)
                    }
                } else {
                    saveProductToFirestore(name, price, description, uriString, "", measurements, category)
                }
            } else {
                // It's a local file URI, upload it first
                uploadFile(imageUri, "images") { imageUrl ->
                    if (blueprintUri != null) {
                        uploadFile(blueprintUri, "blueprints") { blueprintUrl ->
                            saveProductToFirestore(name, price, description, imageUrl, blueprintUrl, measurements, category)
                        }
                    } else {
                        saveProductToFirestore(name, price, description, imageUrl, "", measurements, category)
                    }
                }
            }
        } else {
            saveProductToFirestore(name, price, description, "https://images.unsplash.com/photo-1505693416388-ac5ce068fe85", "", measurements, category)
        }
    }

    private fun uploadFile(uri: Uri, folder: String, onSuccess: (String) -> Unit) {
        val fileName = UUID.randomUUID().toString()
        val ref = storage.reference.child("$folder/$fileName")
        
        ref.putFile(uri)
            .addOnSuccessListener {
                ref.downloadUrl.addOnSuccessListener { url ->
                    onSuccess(url.toString())
                }
            }
            .addOnFailureListener { e ->
                _isLoading.value = false
                _error.value = "$folder upload failed: ${e.message}"
            }
    }

    private fun saveProductToFirestore(
        name: String, 
        price: String, 
        description: String, 
        imageUrl: String, 
        blueprintUrl: String,
        measurements: String,
        category: String
    ) {
        val product = Product(
            name = name,
            price = price,
            description = description,
            image = imageUrl,
            blueprintImage = blueprintUrl,
            measurements = measurements,
            category = category,
            timestamp = System.currentTimeMillis()
        )

        productsCollection.add(product)
            .addOnSuccessListener {
                _isLoading.value = false
                _error.value = null
            }
            .addOnFailureListener { e ->
                _isLoading.value = false
                _error.value = e.message
            }
    }

    fun deleteProduct(productId: String) {
        productsCollection.document(productId).delete()
            .addOnFailureListener { e ->
                _error.value = "Delete failed: ${e.message}"
            }
    }

    fun updateProduct(product: Product, newImageUri: Uri?, newBlueprintUri: Uri?) {
        _isLoading.value = true
        
        if (newImageUri != null) {
            val uriString = newImageUri.toString()
            if (uriString.startsWith("http")) {
                // Web URL, save directly
                val updatedProduct = product.copy(image = uriString)
                handleBlueprintUpdate(updatedProduct, newBlueprintUri)
            } else {
                // Local file, upload then save
                uploadFile(newImageUri, "images") { imageUrl ->
                    val updatedProduct = product.copy(image = imageUrl)
                    handleBlueprintUpdate(updatedProduct, newBlueprintUri)
                }
            }
        } else if (newBlueprintUri != null) {
            handleBlueprintUpdate(product, newBlueprintUri)
        } else {
            saveUpdateToFirestore(product)
        }
    }

    private fun handleBlueprintUpdate(product: Product, newBlueprintUri: Uri?) {
        if (newBlueprintUri != null) {
            uploadFile(newBlueprintUri, "blueprints") { blueprintUrl ->
                saveUpdateToFirestore(product.copy(blueprintImage = blueprintUrl))
            }
        } else {
            saveUpdateToFirestore(product)
        }
    }

    private fun saveUpdateToFirestore(product: Product) {
        productsCollection.document(product.id).set(product)
            .addOnSuccessListener {
                _isLoading.value = false
                _error.value = null
            }
            .addOnFailureListener { e ->
                _isLoading.value = false
                _error.value = e.message
            }
    }

    fun updateProductPrice(productId: String, newPrice: String) {
        _isLoading.value = true
        productsCollection.document(productId).update("price", newPrice)
            .addOnSuccessListener {
                _isLoading.value = false
            }
            .addOnFailureListener { e ->
                _isLoading.value = false
                _error.value = e.message
            }
    }
}

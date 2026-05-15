package com.example.hastashilpa.tracker

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.util.UUID

class MaterialViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    
    private val _logs = mutableStateOf<List<MaterialLog>>(emptyList())
    val logs: State<List<MaterialLog>> = _logs
    
    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoading

    init {
        fetchLogs()
    }

    fun fetchLogs() {
        val userId = auth.currentUser?.uid ?: return
        _isLoading.value = true
        
        db.collection("users").document(userId).collection("material_logs")
            .orderBy("date", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, e ->
                _isLoading.value = false
                if (e != null) return@addSnapshotListener
                
                if (snapshot != null) {
                    _logs.value = snapshot.toObjects(MaterialLog::class.java)
                }
            }
    }

    fun addLog(productName: String, materials: String, quantity: Int) {
        val userId = auth.currentUser?.uid ?: return
        val log = MaterialLog(
            id = UUID.randomUUID().toString(),
            productName = productName,
            materialsUsed = materials,
            batchQuantity = quantity,
            date = System.currentTimeMillis()
        )
        
        db.collection("users").document(userId).collection("material_logs")
            .document(log.id)
            .set(log)
    }
}

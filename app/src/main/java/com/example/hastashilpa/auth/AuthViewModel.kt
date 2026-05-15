package com.example.hastashilpa.auth

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class AuthViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    
    private val _user = mutableStateOf<FirebaseUser?>(auth.currentUser)
    val user: State<FirebaseUser?> = _user

    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoading

    private val _error = mutableStateOf<String?>(null)
    val error: State<String?> = _error

    fun signUp(email: String, pass: String, onSuccess: () -> Unit) {
        if (email.isEmpty() || pass.isEmpty()) {
            _error.value = "Email and password cannot be empty"
            return
        }
        _isLoading.value = true
        _error.value = null
        auth.createUserWithEmailAndPassword(email, pass)
            .addOnCompleteListener { task ->
                _isLoading.value = false
                if (task.isSuccessful) {
                    _user.value = auth.currentUser
                    onSuccess()
                } else {
                    _error.value = task.exception?.message ?: "Signup failed"
                }
            }
    }

    fun signIn(email: String, pass: String, onSuccess: () -> Unit) {
        if (email.isEmpty() || pass.isEmpty()) {
            _error.value = "Email and password cannot be empty"
            return
        }
        _isLoading.value = true
        _error.value = null
        auth.signInWithEmailAndPassword(email, pass)
            .addOnCompleteListener { task ->
                _isLoading.value = false
                if (task.isSuccessful) {
                    _user.value = auth.currentUser
                    onSuccess()
                } else {
                    _error.value = task.exception?.message ?: "Login failed"
                }
            }
    }

    fun signOut() {
        auth.signOut()
        _user.value = null
    }
}

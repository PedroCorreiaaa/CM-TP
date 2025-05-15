package pt.techcare.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import pt.techcare.app.data.model.LoginResponse
import pt.techcare.app.data.repository.AuthRepository

class AuthViewModel(private val repository: AuthRepository) : ViewModel() {

    private val _loginState = MutableStateFlow<LoginResponse?>(null)
    val loginState: StateFlow<LoginResponse?> = _loginState

    fun login(email: String, password: String) {
        viewModelScope.launch {
            val response = repository.login(email, password)
            if (response.isSuccessful) {
                _loginState.value = response.body()
            } else {
                _loginState.value = LoginResponse(false, "Erro ao conectar", null)
            }
        }
    }

    fun register(nome: String, email: String, password: String) {
        viewModelScope.launch {
            val response = repository.register(nome, email, password)
            if (response.isSuccessful) {
                _loginState.value = response.body()
            }
        }
    }
}
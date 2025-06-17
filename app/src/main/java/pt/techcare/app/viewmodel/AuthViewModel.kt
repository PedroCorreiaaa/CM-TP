package pt.techcare.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import pt.techcare.app.data.model.LoginResponse
import pt.techcare.app.data.repository.AuthRepository

// ViewModel responsável pela lógica de autenticação (login e registo)
class AuthViewModel(private val repository: AuthRepository) : ViewModel() {

    // Estado observável que guarda a resposta do login ou registo
    private val _loginState = MutableStateFlow<LoginResponse?>(null)
    val loginState: StateFlow<LoginResponse?> = _loginState

    // Função para efetuar login com email e password
    fun login(email: String, password: String) {
        viewModelScope.launch {
            val response = repository.login(email, password)
            if (response.isSuccessful) {
                _loginState.value = response.body()
            } else {
                _loginState.value = LoginResponse(
                    success = false,
                    message = "Erro ao conectar",
                    user = null
                )
            }
        }
    }
}
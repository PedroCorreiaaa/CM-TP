package pt.techcare.app.data.repository

import pt.techcare.app.data.api.ApiService
import pt.techcare.app.data.model.LoginRequest
import pt.techcare.app.data.model.RegisterRequest

class AuthRepository(private val api: ApiService) {

    suspend fun login(email: String, password: String) =
        api.login(LoginRequest(email, password))

    suspend fun register(nome: String, email: String, password: String) =
        api.register(RegisterRequest(nome, email, password))
}
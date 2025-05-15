package pt.techcare.app.data.model

data class RegisterRequest(
    val nome: String,
    val email: String,
    val password: String
)
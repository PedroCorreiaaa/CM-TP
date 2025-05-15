package pt.techcare.app.data.model

data class User(
    val id_utilizador: Int,
    val nome: String,
    val email: String,
    val id_tipo_utilizador: Int
)
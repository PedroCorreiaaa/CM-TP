package pt.techcare.app.data.model

data class Comentario(
    val id_comentario: Int,
    val nome_utilizador: String,
    val conteudo: String,
    val data_comentario: String,
    val tipo_utilizador: Int
)
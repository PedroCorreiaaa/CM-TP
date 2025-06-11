package pt.techcare.app.data.model

data class NotificacaoRequest(
    val id_avaria: Int,
    val mensagem: String,
    val id_utilizador: Int
)
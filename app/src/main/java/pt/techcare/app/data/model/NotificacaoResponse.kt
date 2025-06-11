package pt.techcare.app.data.model

data class NotificacaoResponse(
    val success: Boolean,
    val notificacao: Notificacao? = null,
    val message: String? = null
)
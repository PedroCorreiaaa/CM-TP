package pt.techcare.app.data.model

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

data class Notificacao(
    val id_notificacao: Int,
    val id_avaria: Int,
    val mensagem: String,
    val data_emissao: String,
    val id_utilizador: Int
) {
    val dataEmissaoAsDateTime: LocalDateTime
        get() = LocalDateTime.parse(data_emissao, DateTimeFormatter.ISO_OFFSET_DATE_TIME)

    val tipo: String
        get() = when {
            mensagem.lowercase().contains("atribuído") -> "Técnico atribuído"
            mensagem.lowercase().contains("resolvida") -> "Avaria resolvida"
            mensagem.lowercase().contains("criada") -> "Avaria criada"
            mensagem.lowercase().contains("prioridade") -> "Prioridade alterada"
            else -> {
                "Técnico atribuído"
            }
        }
}
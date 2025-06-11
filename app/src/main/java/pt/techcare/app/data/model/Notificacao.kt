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
            mensagem.contains("atribuído") -> "Técnico atribuído"
            mensagem.contains("resolvida") -> "Avaria resolvida"
            mensagem.contains("criada") -> "Avaria criada"
            mensagem.contains("prioridade") -> "Prioridade alterada"
            else -> "Desconhecido"
        }
}
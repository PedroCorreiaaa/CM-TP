package pt.techcare.app.data.model

data class Avaria(
    val id_avaria: Int,
    val id_utilizador: Int,
    val descricao: String,
    val descricao_equipamento: String,
    val localizacao: String,
    val grau_urgencia: String,
    val data_registo: String,
    val resolucao: String?,
    val estado: EstadoAvaria
)
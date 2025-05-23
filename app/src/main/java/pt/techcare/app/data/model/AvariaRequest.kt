package pt.techcare.app.data.model

data class AvariaRequest(
    val id_utilizador: Int,
    val descricao: String,
    val descricao_equipamento: String,
    val localizacao: String,
    val grau_urgencia: String
)
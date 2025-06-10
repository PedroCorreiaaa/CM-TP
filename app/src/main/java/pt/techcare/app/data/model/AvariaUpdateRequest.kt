package pt.techcare.app.data.model

data class AvariaUpdateRequest(
    val id_estado_avaria: Int? = null,
    val grau_urgencia: String? = null,
    val id_responsavel: Int? = null
)
package pt.techcare.app.data.repository

import pt.techcare.app.data.api.ApiService
import pt.techcare.app.data.model.ComentarioRequest

class ComentarioRepository(private val api: ApiService) {
    suspend fun getComentarios(avariaId: Int) = api.getComentarios(avariaId)
    suspend fun enviarComentario(avariaId: Int, comentario: ComentarioRequest) =
        api.enviarComentario(avariaId, comentario)
}
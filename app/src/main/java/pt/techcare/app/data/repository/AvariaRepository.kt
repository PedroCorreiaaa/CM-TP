package pt.techcare.app.data.repository

import okhttp3.MultipartBody
import pt.techcare.app.data.api.ApiService
import pt.techcare.app.data.model.AtribuirTecnicoRequest
import pt.techcare.app.data.model.AvariaRequest
import pt.techcare.app.data.model.AvariaUpdateRequest

class AvariaRepository(private val api: ApiService) {

    suspend fun getAvarias() = api.getAvarias()
    suspend fun getAvariasTecnico(idTecnico: Int) = api.getAvariasTecnico(idTecnico)
    suspend fun getAvariasUtilizador(idUtilizador: Int) = api.getAvariasUtilizador(idUtilizador)
    suspend fun registarAvaria(avaria: AvariaRequest) = api.registarAvaria(avaria)
    suspend fun uploadImagem(idAvaria: Int, imagem: MultipartBody.Part) = api.uploadImagem(idAvaria, imagem)
    suspend fun atribuirTecnico(idAvaria: Int, idTecnico: Int) = api.atribuirTecnico(idAvaria, AtribuirTecnicoRequest(idTecnico))
    suspend fun atualizarAvaria(idAvaria: Int, campos: AvariaUpdateRequest) = api.atualizarAvaria(idAvaria, campos)
    suspend fun getTecnicos() = api.getTecnicos()
    suspend fun registrarTecnico(nome: String, email: String, password: String) = api.registrarTecnico(nome, email, password)
}
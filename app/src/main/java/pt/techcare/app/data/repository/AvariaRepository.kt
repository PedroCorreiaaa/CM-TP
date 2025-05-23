package pt.techcare.app.data.repository

import pt.techcare.app.data.api.ApiService
import pt.techcare.app.data.model.AvariaRequest

class AvariaRepository(private val api: ApiService) {
    suspend fun getAvarias() = api.getAvarias()
    suspend fun registarAvaria(avaria: AvariaRequest) = api.registarAvaria(avaria)
}
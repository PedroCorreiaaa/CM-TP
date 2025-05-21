package pt.techcare.app.data.repository

import pt.techcare.app.data.api.ApiService

class RelatorioRepository(private val api: ApiService) {
    suspend fun getEstatisticas() = api.getEstatisticas()
}
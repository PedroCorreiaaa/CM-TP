package pt.techcare.app.data.repository

import pt.techcare.app.data.api.ApiService
import pt.techcare.app.data.model.RelatorioEstatisticas
import retrofit2.Response

class RelatorioRepository(private val api: ApiService) {

    suspend fun getEstatisticas(
        datas: String? = null,
        tipoEquipamento: String? = null,
        localizacao: String? = null
    ): Response<RelatorioEstatisticas> {
        return api.getEstatisticas(datas, tipoEquipamento, localizacao)
    }
}

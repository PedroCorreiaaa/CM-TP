package pt.techcare.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import pt.techcare.app.data.api.ApiClient
import pt.techcare.app.data.model.RelatorioEstatisticas
import pt.techcare.app.data.repository.RelatorioRepository

class RelatorioViewModel : ViewModel() {
    private val repository = RelatorioRepository(ApiClient.apiService)

    private val _estatisticas = MutableStateFlow<RelatorioEstatisticas?>(null)
    val estatisticas: StateFlow<RelatorioEstatisticas?> = _estatisticas

    fun carregarEstatisticas() {
        viewModelScope.launch {
            val response = repository.getEstatisticas()
            if (response.isSuccessful) {
                _estatisticas.value = response.body()
            }
        }
    }

    fun carregarEstatisticasComFiltro(datas: String?, tipoEquipamento: String?, localizacao: String?) {
        viewModelScope.launch {
            val response = repository.getEstatisticas(datas, tipoEquipamento, localizacao)
            if (response.isSuccessful) {
                _estatisticas.value = response.body()
            }
        }
    }
}


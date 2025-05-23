package pt.techcare.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import pt.techcare.app.data.api.ApiClient
import pt.techcare.app.data.model.Avaria
import pt.techcare.app.data.model.AvariaItem
import pt.techcare.app.data.repository.AvariaRepository

class AvariaViewModel : ViewModel() {

    private val repository = AvariaRepository(ApiClient.apiService)

    private val _avarias = MutableStateFlow<List<AvariaItem>>(emptyList())
    val avarias: StateFlow<List<AvariaItem>> = _avarias

    fun carregarAvarias() {
        viewModelScope.launch {
            val response = repository.getAvarias()
            if (response.isSuccessful) {
                val lista = response.body() ?: emptyList()
                _avarias.value = lista.map { avaria ->
                    AvariaItem(
                        titulo = avaria.descricao_equipamento,
                        prioridade = avaria.grau_urgencia,
                        descricao = avaria.descricao,
                        data = avaria.data_registo
                    )
                }
            }
        }
    }
}
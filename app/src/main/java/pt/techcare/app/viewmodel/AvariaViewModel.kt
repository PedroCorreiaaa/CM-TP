package pt.techcare.app.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import pt.techcare.app.data.api.ApiClient
import pt.techcare.app.data.model.Avaria
import pt.techcare.app.data.repository.AvariaRepository

class AvariaViewModel : ViewModel() {
    private val repository = AvariaRepository(ApiClient.apiService)

    private val _avarias = MutableStateFlow<List<Avaria>>(emptyList())
    val avarias: StateFlow<List<Avaria>> = _avarias

    fun carregarAvarias(userType: Int, userId: Int) {
        viewModelScope.launch {
            val response = when (userType) {
                1 -> repository.getAvariasUtilizador(userId)
                2 -> repository.getAvariasTecnico(userId)
                3 -> repository.getAvarias()
                else -> repository.getAvarias()
            }
            if (response.isSuccessful) {
                _avarias.value = response.body() ?: emptyList()
                Log.d("AvariaViewModel", "Avarias carregadas: ${_avarias.value.size}")
            } else {
                Log.e("AvariaViewModel", "Erro ao carregar avarias: ${response.code()} - ${response.message()}")
            }
        }
    }

    suspend fun atribuirTecnico(idAvaria: Int, idUtilizador: Int): Boolean {
        try {
            Log.d("AvariaViewModel", "Atribuindo técnico. idAvaria: $idAvaria, idUtilizador: $idUtilizador")
            val response = repository.atribuirTecnico(idAvaria, idUtilizador)
            if (response.isSuccessful) {
                Log.d("AvariaViewModel", "Técnico atribuído com sucesso. Response: ${response.body()}")
                return true
            } else {
                Log.e("AvariaViewModel", "Erro na API: ${response.code()} - ${response.message()}")
                return false
            }
        } catch (e: Exception) {
            Log.e("AvariaViewModel", "Exceção ao atribuir técnico: ${e.message}")
            return false
        }
    }
}
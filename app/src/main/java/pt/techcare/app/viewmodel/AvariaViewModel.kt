package pt.techcare.app.viewmodel

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
            }
        }
    }

    suspend fun atribuirTecnico(idAvaria: Int, idTecnico: Int): Boolean {
        val response = repository.atribuirTecnico(idAvaria, idTecnico)
        return response.isSuccessful
    }
}
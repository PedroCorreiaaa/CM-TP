package pt.techcare.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import pt.techcare.app.data.api.ApiClient
import pt.techcare.app.data.model.Comentario
import pt.techcare.app.data.model.ComentarioRequest
import pt.techcare.app.data.repository.ComentarioRepository

class ComentarioViewModel : ViewModel() {

    private val repository = ComentarioRepository(ApiClient.apiService)

    private val _comentarios = MutableStateFlow<List<Comentario>>(emptyList())
    val comentarios: StateFlow<List<Comentario>> = _comentarios

    private val _comentarioEnviado = MutableStateFlow<Boolean>(false)
    val comentarioEnviado: StateFlow<Boolean> = _comentarioEnviado

    fun carregarComentarios(idAvaria: Int) {
        viewModelScope.launch {
            val response = repository.getComentarios(idAvaria)
            if (response.isSuccessful) {
                _comentarios.value = response.body() ?: emptyList()
            }
        }
    }

    fun enviarComentario(idAvaria: Int, comentario: ComentarioRequest) {
        viewModelScope.launch {
            val response = repository.enviarComentario(idAvaria, comentario)
            _comentarioEnviado.value = response.isSuccessful
            if (response.isSuccessful) {
                carregarComentarios(idAvaria)
            }
        }
    }
}
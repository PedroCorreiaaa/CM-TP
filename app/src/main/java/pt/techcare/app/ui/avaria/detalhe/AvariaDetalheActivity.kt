package pt.techcare.app.ui.avaria.detalhe

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.coroutines.launch
import pt.techcare.app.data.api.ApiClient
import pt.techcare.app.data.model.ComentarioRequest
import pt.techcare.app.data.repository.ComentarioRepository
import pt.techcare.app.databinding.ActivityAvariaDetalheBinding
import pt.techcare.app.ui.comentario.ComentarioAdapter
import pt.techcare.app.util.SessionManager

class AvariaDetalheActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAvariaDetalheBinding
    private val comentarioRepository = ComentarioRepository(ApiClient.apiService)
    private lateinit var sessionManager: SessionManager
    private var idAvaria: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAvariaDetalheBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)
        idAvaria = intent.getIntExtra("id_avaria", -1)

        setupRecyclerView()
        carregarComentarios()

        binding.btnEnviarComentario.setOnClickListener {
            val texto = binding.etNovoComentario.text.toString().trim()
            if (texto.isNotEmpty()) {
                enviarComentario(texto)
            }
        }
    }

    private fun setupRecyclerView() {
        binding.recyclerComentarios.layoutManager = LinearLayoutManager(this)
    }

    private fun carregarComentarios() {
        lifecycleScope.launch {
            val response = comentarioRepository.getComentarios(idAvaria)
            if (response.isSuccessful) {
                val lista = response.body() ?: emptyList()
                binding.recyclerComentarios.adapter = ComentarioAdapter(lista)
            } else {
                Toast.makeText(this@AvariaDetalheActivity, "Erro ao carregar comentários", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun enviarComentario(texto: String) {
        val userId = sessionManager.getUserId()
        val comentario = ComentarioRequest(userId, texto)
        lifecycleScope.launch {
            val response = comentarioRepository.enviarComentario(idAvaria, comentario)
            if (response.isSuccessful) {
                binding.etNovoComentario.text.clear()
                carregarComentarios()
            } else {
                Toast.makeText(this@AvariaDetalheActivity, "Erro ao enviar comentário", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
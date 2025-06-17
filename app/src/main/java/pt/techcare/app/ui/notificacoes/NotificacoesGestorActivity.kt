package pt.techcare.app.ui.notificacoes

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.coroutines.launch
import pt.techcare.app.R
import pt.techcare.app.data.api.ApiClient
import pt.techcare.app.data.repository.AvariaRepository
import pt.techcare.app.databinding.ActivityNotificacoesBinding
import pt.techcare.app.util.SessionManager

class NotificacoesGestorActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNotificacoesBinding
    private val avariaRepository = AvariaRepository(ApiClient.apiService)
    private lateinit var sessionManager: SessionManager

    // Adapter da lista de notificações com ação ao clicar
    private val notificacaoAdapter = NotificacaoAdapter(emptyList()) { notificacao ->
        Toast.makeText(this, "Clicou em: ${notificacao.mensagem}", Toast.LENGTH_SHORT).show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNotificacoesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)

        // Configura o RecyclerView com layout vertical e o adapter
        binding.recyclerNotificacoes.layoutManager = LinearLayoutManager(this)
        binding.recyclerNotificacoes.adapter = notificacaoAdapter

        carregarNotificacoes()
    }

    // Carrega todas as notificações da API e exibe no adapter
    private fun carregarNotificacoes() {
        val userId = sessionManager.getUserId() ?: return
        lifecycleScope.launch {
            try {
                val response = avariaRepository.getNotificacoes(userId)
                if (response.isSuccessful) {
                    response.body()?.let { notificacoes ->
                        notificacaoAdapter.updateList(notificacoes)
                    } ?: run {
                        Toast.makeText(this@NotificacoesGestorActivity, R.string.toast_nenhuma_notificacao, Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this@NotificacoesGestorActivity, "Erro ao carregar notificações: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e("NotificacoesGestor", "Erro ao carregar notificações: ${e.message}")
                Toast.makeText(this@NotificacoesGestorActivity, "Erro: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
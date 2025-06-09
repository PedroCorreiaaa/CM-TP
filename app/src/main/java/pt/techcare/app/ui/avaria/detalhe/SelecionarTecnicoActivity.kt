package pt.techcare.app.ui.avaria.detalhe

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.coroutines.launch
import pt.techcare.app.data.api.ApiClient
import pt.techcare.app.data.model.User
import pt.techcare.app.databinding.ActivitySelecionarTecnicoBinding

class SelecionarTecnicoActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySelecionarTecnicoBinding
    private var idAvaria: Int = -1
    private val tecnicos = mutableListOf<User>()
    private lateinit var adapter: TecnicoAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySelecionarTecnicoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        idAvaria = intent.getIntExtra("id_avaria", -1)

        adapter = TecnicoAdapter(tecnicos) { tecnico ->
            val resultIntent = Intent().apply {
                putExtra("id_tecnico", tecnico.id_utilizador)
            }
            setResult(Activity.RESULT_OK, resultIntent)
            finish()
        }

        binding.recyclerTecnicos.layoutManager = LinearLayoutManager(this)
        binding.recyclerTecnicos.adapter = adapter

        carregarTecnicos()
    }

    private fun carregarTecnicos() {
        lifecycleScope.launch {
            try {
                val response = ApiClient.apiService.getTecnicos()
                if (response.isSuccessful) {
                    tecnicos.clear()
                    tecnicos.addAll(response.body() ?: emptyList())
                    adapter.notifyDataSetChanged()
                } else {
                    Toast.makeText(this@SelecionarTecnicoActivity, "Erro ao carregar técnicos", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@SelecionarTecnicoActivity, "Erro: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
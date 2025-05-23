package pt.techcare.app.ui.avaria.registar

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import pt.techcare.app.data.api.ApiClient
import pt.techcare.app.data.model.AvariaRequest
import pt.techcare.app.data.repository.AvariaRepository
import pt.techcare.app.databinding.ActivityRegistarAvariaBinding
import pt.techcare.app.util.SessionManager

class RegistarAvariaActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegistarAvariaBinding
    private lateinit var sessionManager: SessionManager
    private val avariaRepository = AvariaRepository(ApiClient.apiService)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegistarAvariaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)

        val opcoesUrgencia = listOf("Selecionar prioridade", "Alta", "Média", "Baixa")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, opcoesUrgencia)
        binding.spinnerPrioridade.adapter = adapter

        binding.btnSubmeter.setOnClickListener {
            val equipamento = binding.etEquipamento.text.toString().trim()
            val localizacao = binding.etLocalizacao.text.toString().trim()
            val descricao = binding.etDescricao.text.toString().trim()
            val grauUrgencia = binding.spinnerPrioridade.selectedItem.toString()
            val idUtilizador = sessionManager.getUserId()

            if (equipamento.isBlank() || localizacao.isBlank() || descricao.isBlank() || grauUrgencia == "Selecionar prioridade") {
                Toast.makeText(this, "Preencha todos os campos obrigatórios", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val novaAvaria = AvariaRequest(
                id_utilizador = idUtilizador,
                descricao = descricao,
                descricao_equipamento = equipamento,
                localizacao = localizacao,
                grau_urgencia = grauUrgencia
            )

            lifecycleScope.launch {
                val response = avariaRepository.registarAvaria(novaAvaria)
                if (response.isSuccessful) {
                    Toast.makeText(this@RegistarAvariaActivity, "Avaria registada com sucesso ✅", Toast.LENGTH_LONG).show()
                    finish()
                } else {
                    Toast.makeText(this@RegistarAvariaActivity, "Erro ao registar avaria", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
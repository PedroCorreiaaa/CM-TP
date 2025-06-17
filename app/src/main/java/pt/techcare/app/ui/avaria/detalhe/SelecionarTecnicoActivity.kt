package pt.techcare.app.ui.avaria.detalhe

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.coroutines.launch
import pt.techcare.app.R
import pt.techcare.app.data.api.ApiClient
import pt.techcare.app.data.repository.AvariaRepository
import pt.techcare.app.databinding.ActivitySelecionarTecnicoBinding
import pt.techcare.app.util.SessionManager
import pt.techcare.app.viewmodel.AvariaViewModel

class SelecionarTecnicoActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySelecionarTecnicoBinding
    private lateinit var viewModel: AvariaViewModel
    private val avariaRepository = AvariaRepository(ApiClient.apiService)
    private lateinit var sessionManager: SessionManager
    private var idAvaria: Int = -1
    private val tecnicoAdapter = TecnicoAdapter(emptyList()) { user ->
        val intent = Intent()
        intent.putExtra("id_tecnico", user.id_utilizador)
        setResult(RESULT_OK, intent)
        finish()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState) //Chama a implementação da superclasse
        binding = ActivitySelecionarTecnicoBinding.inflate(layoutInflater) //Inicializa o View Binding
        setContentView(binding.root)

        viewModel = AvariaViewModel()
        sessionManager = SessionManager(this)
        idAvaria = intent.getIntExtra("id_avaria", -1)

        binding.recyclerTecnicos.layoutManager = LinearLayoutManager(this)
        binding.recyclerTecnicos.adapter = tecnicoAdapter

        val userType = sessionManager.getUserType()
        if (userType == 3) {
            binding.btnCriarTecnico.visibility = View.VISIBLE
            binding.btnCriarTecnico.setOnClickListener {
                mostrarDialogoCriarTecnico()
            }
        } else {
            binding.btnCriarTecnico.visibility = View.GONE
        }

        carregarTecnicos()
    }

    private fun carregarTecnicos() {
        lifecycleScope.launch {
            try {
                val response = avariaRepository.getTecnicos()
                if (response.isSuccessful) {
                    response.body()?.let { tecnicos ->
                        tecnicoAdapter.updateList(tecnicos)
                    } ?: run {
                        Toast.makeText(this@SelecionarTecnicoActivity, "Nenhum técnico encontrado.", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this@SelecionarTecnicoActivity, "Erro ao carregar técnicos: ${response.code()} - ${response.errorBody()?.string()}", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e("SelecionarTecnico", "Erro ao carregar técnicos: ${e.message}")
                Toast.makeText(this@SelecionarTecnicoActivity, "Erro: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun mostrarDialogoCriarTecnico() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(R.string.text_registar_novo_tecnico)

        val layout = android.view.LayoutInflater.from(this).inflate(R.layout.dialog_criar_tecnico, null)
        val etNome = layout.findViewById<EditText>(R.id.etNome)
        val etEmail = layout.findViewById<EditText>(R.id.etEmail)
        val etPassword = layout.findViewById<EditText>(R.id.etPassword)

        builder.setView(layout)

        builder.setPositiveButton(R.string.btn_criar) { _, _ ->
            val nome = etNome.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (nome.isNotEmpty() && email.isNotEmpty() && password.isNotEmpty()) {
                criarTecnico(nome, email, password)
            } else {
                Toast.makeText(this@SelecionarTecnicoActivity, R.string.toast_preencha_campos, Toast.LENGTH_SHORT).show()
            }
        }
        builder.setNegativeButton(R.string.btn_cancelar) { dialog, _ ->
            dialog.dismiss()
        }
        builder.show()
    }

    private fun criarTecnico(nome: String, email: String, password: String) {
        lifecycleScope.launch {
            try {
                Log.d("SelecionarTecnico", "Enviando requisição: nome=$nome, email=$email, password=$password")
                val response = avariaRepository.registrarTecnico(nome, email, password)
                if (response.isSuccessful) {
                    Toast.makeText(this@SelecionarTecnicoActivity, "Técnico criado com sucesso!", Toast.LENGTH_SHORT).show()
                    carregarTecnicos()
                } else {
                    val errorMsg = when (response.code()) {
                        400 -> R.string.erro_email_registado.toString()
                        else -> "Erro ao criar técnico: ${response.code()} - ${response.errorBody()?.string()}"
                    }
                    Toast.makeText(this@SelecionarTecnicoActivity, errorMsg, Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e("SelecionarTecnico", "Erro ao criar técnico: ${e.message}")
                Toast.makeText(this@SelecionarTecnicoActivity, "Erro: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
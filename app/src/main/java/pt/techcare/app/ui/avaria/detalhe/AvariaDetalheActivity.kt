package pt.techcare.app.ui.avaria.detalhe

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import pt.techcare.app.data.api.ApiClient
import pt.techcare.app.data.model.Avaria
import pt.techcare.app.data.repository.AvariaRepository
import pt.techcare.app.databinding.ActivityAvariaDetalheBinding
import pt.techcare.app.util.SessionManager
import pt.techcare.app.viewmodel.AvariaViewModel

class AvariaDetalheActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAvariaDetalheBinding
    private lateinit var viewModel: AvariaViewModel
    private val avariaRepository = AvariaRepository(ApiClient.apiService)
    private lateinit var sessionManager: SessionManager
    private var idAvaria: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAvariaDetalheBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = AvariaViewModel()
        sessionManager = SessionManager(this)
        idAvaria = intent.getIntExtra("id_avaria", -1)

        val userType = sessionManager.getUserType()
        if (userType == 3) {
            binding.btnAlterarTecnico.visibility = View.VISIBLE
        } else {
            binding.btnAlterarTecnico.visibility = View.GONE
        }

        if (idAvaria != -1) {
            carregarDetalhesAvaria(idAvaria)
        } else {
            Toast.makeText(this, "ID da avaria não encontrado.", Toast.LENGTH_SHORT).show()
            finish()
        }

        binding.btnAlterarTecnico.setOnClickListener {
            val intent = Intent(this, SelecionarTecnicoActivity::class.java)
            intent.putExtra("id_avaria", idAvaria)
            startActivityForResult(intent, 100)
        }
    }

    private fun carregarDetalhesAvaria(id: Int) {
        lifecycleScope.launch {
            try {
                val response = ApiClient.apiService.getAvariaById(id)
                if (response.isSuccessful) {
                    response.body()?.let { preencherDetalhes(it) }
                } else {
                    Toast.makeText(this@AvariaDetalheActivity, "Erro ao carregar detalhes", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@AvariaDetalheActivity, "Erro: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun preencherDetalhes(avaria: Avaria) {
        binding.txtTitulo.text = avaria.descricao_equipamento
        binding.txtDescricao.text = avaria.descricao
        binding.txtEstado.text = avaria.estado.descricao
        binding.txtTipoEquipamento.text = avaria.descricao_equipamento
        binding.txtSolicitacao.text = avaria.resolucao ?: "Sem solicitação"
        binding.txtPrioridade.text = avaria.grau_urgencia
    }

    private fun atribuirTecnico(tecnicoId: Int) {
        lifecycleScope.launch {
            try {
                val sucesso = viewModel.atribuirTecnico(idAvaria, tecnicoId)
                if (sucesso) {
                    Toast.makeText(this@AvariaDetalheActivity, "Técnico atribuído com sucesso!", Toast.LENGTH_SHORT).show()
                    carregarDetalhesAvaria(idAvaria)
                } else {
                    Toast.makeText(this@AvariaDetalheActivity, "Erro ao atribuir técnico.", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@AvariaDetalheActivity, "Erro: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 100 && resultCode == Activity.RESULT_OK) {
            val idTecnico = data?.getIntExtra("id_tecnico", -1)
            idTecnico?.let {
                atribuirTecnico(it)
            }
        }
    }
}
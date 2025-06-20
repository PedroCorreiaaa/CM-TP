package pt.techcare.app.ui.avaria.detalhe

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import pt.techcare.app.R
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
    private val prioridades = arrayOf("Baixa", "Média", "Alta")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAvariaDetalheBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = AvariaViewModel()
        sessionManager = SessionManager(this)
        idAvaria = intent.getIntExtra("id_avaria", -1)

        val spinnerPrioridade = binding.txtPrioridade
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, prioridades)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerPrioridade.adapter = adapter

        // Ativa edição de prioridade apenas para gestores
        if (sessionManager.getUserType() == 3) {
            spinnerPrioridade.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                    val prioridadeSelecionada = prioridades[position]
                    atualizarPrioridade(prioridadeSelecionada) // Atualiza prioridade selecionada
                }
                override fun onNothingSelected(parent: AdapterView<*>) {}
            }
        } else {
            spinnerPrioridade.isEnabled = false
        }

        // Exibe as opcoes apenas para gestores
        val userType = sessionManager.getUserType()
        if (userType == 3) {
            binding.btnAlterarTecnico.visibility = View.VISIBLE
            binding.txtPrioridade.visibility = View.VISIBLE
        } else {
            binding.btnAlterarTecnico.visibility = View.GONE
            binding.txtPrioridade.visibility = View.GONE
        }

        if (idAvaria != -1) {
            carregarDetalhesAvaria(idAvaria)
        } else {
            Toast.makeText(this, "ID da avaria não encontrado.", Toast.LENGTH_SHORT).show()
            finish()
        }

        binding.btnAlterarTecnico.setOnClickListener {
            Log.d("AvariaDetalhe", "Abrindo SelecionarTecnicoActivity para idAvaria: $idAvaria")
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
                    response.body()?.let { avaria ->
                        preencherDetalhes(avaria)

                        val estadoResolvido = avaria.estado?.id_estado_avaria == 2
                        if (estadoResolvido) {
                            binding.txtPrioridade.isEnabled = false
                            binding.btnAlterarTecnico.isEnabled = false
                            binding.btnAlterarEstado.visibility = View.GONE
                        } else {
                            if (sessionManager.getUserType() == 2) {
                                binding.btnAlterarEstado.visibility = View.VISIBLE
                                binding.btnAlterarEstado.setText(R.string.btn_asinalar_resolvida)
                                binding.btnAlterarEstado.setOnClickListener {
                                    mostrarDialogoAssinalarResolvida()
                                }
                            } else {
                                binding.btnAlterarEstado.visibility = View.GONE
                            }
                        }
                    }
                } else {
                    Toast.makeText(this@AvariaDetalheActivity, "Erro ao carregar detalhes: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e("AvariaDetalhe", "Erro ao carregar detalhes: ${e.message}")
                Toast.makeText(this@AvariaDetalheActivity, "Erro: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun preencherDetalhes(avaria: Avaria) {
        binding.txtTitulo.text = avaria.descricao_equipamento ?: "Sem título"
        binding.txtDescricao.text = avaria.descricao ?: "Sem descrição"
        binding.txtEstado.text = avaria.estado?.descricao ?: "Sem estado"
        binding.txtTipoEquipamento.text = avaria.descricao_equipamento ?: "Sem equipamento"
        binding.txtSolicitacao.text = avaria.resolucao ?: "Por resolver"
        val prioridadeIndex = when (avaria.grau_urgencia) {
            "Baixa" -> 0
            "Média" -> 1
            "Alta" -> 2
            else -> 0
        }
        binding.txtPrioridade.setSelection(prioridadeIndex)
    }

    private fun atribuirTecnico(tecnicoId: Int) {
        lifecycleScope.launch {
            try {
                val idResponsavel = sessionManager.getUserId() ?: -1
                val sucesso = viewModel.atribuirTecnico(idAvaria, tecnicoId, idResponsavel)
                if (sucesso) {
                    Toast.makeText(this@AvariaDetalheActivity, "Técnico atribuído com sucesso!", Toast.LENGTH_SHORT).show()
                    carregarDetalhesAvaria(idAvaria)
                } else {
                    Toast.makeText(this@AvariaDetalheActivity, "Erro ao atribuir técnico.", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e("AvariaDetalhe", "Erro ao atribuir técnico: ${e.message}")
                Toast.makeText(this@AvariaDetalheActivity, "Erro: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun atualizarPrioridade(prioridade: String) {
        lifecycleScope.launch {
            try {
                val idResponsavel = sessionManager.getUserId() ?: -1
                val sucesso = viewModel.atualizarAvaria(idAvaria, mapOf("grau_urgencia" to prioridade), idResponsavel)
                if (sucesso) {
                    carregarDetalhesAvaria(idAvaria)
                } else {
                    Toast.makeText(this@AvariaDetalheActivity, "Erro ao atualizar prioridade.", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e("AvariaDetalhe", "Erro ao atualizar prioridade: ${e.message}")
                Toast.makeText(this@AvariaDetalheActivity, "Erro: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun mostrarDialogoAssinalarResolvida() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(R.string.btn_asinalar_resolvida)

        val input = EditText(this)
        input.hint = getString(R.string.hint_inserir_resolucao)
        input.inputType = InputType.TYPE_CLASS_TEXT
        builder.setView(input)

        builder.setPositiveButton(R.string.btn_confirmar) { _, _ ->
            val resolucao = input.text.toString().trim()
            if (resolucao.isNotEmpty()) {
                atualizarEstadoAvaria(2, resolucao)
            } else {
                Toast.makeText(this@AvariaDetalheActivity, R.string.toast_erro_resolucao, Toast.LENGTH_SHORT).show()
            }
        }
        builder.setNegativeButton(R.string.btn_cancelar, null)
        builder.show()
    }

    private fun atualizarEstadoAvaria(novoEstado: Int, resolucao: String) {
        lifecycleScope.launch {
            try {
                val idResponsavel = sessionManager.getUserId() ?: -1
                val sucesso = viewModel.atualizarAvaria(idAvaria, mapOf("id_estado_avaria" to novoEstado, "resolucao" to resolucao), idResponsavel)
                if (sucesso) {
                    Toast.makeText(this@AvariaDetalheActivity, R.string.toast_avaria_resolvida, Toast.LENGTH_SHORT).show()
                    carregarDetalhesAvaria(idAvaria)
                } else {
                    Toast.makeText(this@AvariaDetalheActivity, R.string.toast_erro_avaria_resolvida, Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e("AvariaDetalhe", "Erro ao atualizar estado: ${e.message}")
                Toast.makeText(this@AvariaDetalheActivity, "Erro: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 100 && resultCode == Activity.RESULT_OK) {
            val idTecnico: Int? = data?.getIntExtra("id_tecnico", -1)
            if (idTecnico != null && idTecnico != -1) { //Se recebeu um ID válido chama a função
                Log.d("AvariaDetalhe", "Recebido idTecnico: $idTecnico")
                atribuirTecnico(idTecnico)
            } else {
                Log.e("AvariaDetalhe", "idTecnico não recebido ou inválido: $idTecnico")
                Toast.makeText(this@AvariaDetalheActivity, "Erro: ID do técnico não recebido ou inválido.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
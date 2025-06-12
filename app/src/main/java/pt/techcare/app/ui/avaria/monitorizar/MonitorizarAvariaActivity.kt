package pt.techcare.app.ui.avaria.monitorizar

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Gravity
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.flow.collectLatest
import pt.techcare.app.R
import pt.techcare.app.data.model.Avaria
import pt.techcare.app.databinding.ActivityMonitorizarAvariaBinding
import pt.techcare.app.ui.avaria.detalhe.AvariaDetalheActivity
import pt.techcare.app.util.SessionManager
import pt.techcare.app.viewmodel.AvariaViewModel

class MonitorizarAvariaActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMonitorizarAvariaBinding
    private val viewModel = AvariaViewModel()
    private lateinit var sessionManager: SessionManager
    private var listaOriginal: List<Avaria> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMonitorizarAvariaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)

        lifecycleScope.launchWhenStarted {
            viewModel.avarias.collectLatest { lista ->
                listaOriginal = lista
                mostrarAvarias(lista)
            }
        }

        val userType = sessionManager.getUserType()
        val userId = sessionManager.getUserId()
        viewModel.carregarAvarias(userType, userId)
        binding.etPesquisa.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                aplicarFiltroPesquisa(s.toString())
            }
        })

        binding.btnFiltrar.setOnClickListener {
            mostrarDialogoFiltro()
        }
    }

    private fun mostrarAvarias(lista: List<Avaria>) {
        binding.listaAvarias.removeAllViews()

        if (lista.isEmpty()) {
            val textoVazio = TextView(this).apply {
                text = R.string.hint_sem_resultados.toString()
                textSize = 16f
                gravity = Gravity.CENTER
                setTextColor(resources.getColor(R.color.texto_secundario))
                layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                ).apply { topMargin = 32 }
            }
            binding.listaAvarias.addView(textoVazio)
            return
        }

        lista.forEach { avaria ->
            val card = criarCardAvaria(avaria)
            binding.listaAvarias.addView(card)
        }
    }

    private fun aplicarFiltroPesquisa(texto: String) {
        val resultado = listaOriginal.filter {
            it.descricao_equipamento.contains(texto, ignoreCase = true) ||
                    it.descricao.contains(texto, ignoreCase = true)
        }
        mostrarAvarias(resultado)
    }

    private fun aplicarFiltroAvancado(estado: String, prioridade: String) {
        val resultado = listaOriginal.filter { avaria ->
            val correspondeEstado = estado == "Todos" || avaria.estado.descricao.equals(estado, ignoreCase = true)
            val correspondePrioridade = prioridade == "Todas" || avaria.grau_urgencia.equals(prioridade, ignoreCase = true)
            correspondeEstado && correspondePrioridade
        }
        mostrarAvarias(resultado)
    }

    private fun mostrarDialogoFiltro() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_filtros, null)
        val spinnerEstado = dialogView.findViewById<Spinner>(R.id.spinnerEstado)
        val spinnerPrioridade = dialogView.findViewById<Spinner>(R.id.spinnerPrioridade)

        val estados = listOf("Todos", "Pendente", "Em Progresso", "Resolvido")
        val prioridades = listOf("Todas", "Alta", "Média", "Baixa")

        spinnerEstado.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, estados)
        spinnerPrioridade.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, prioridades)

        AlertDialog.Builder(this)
            .setTitle(R.string.text_filtrar_avarias)
            .setView(dialogView)
            .setPositiveButton(R.string.btn_aplicar) { _, _ ->
                val estadoSelecionado = spinnerEstado.selectedItem.toString()
                val prioridadeSelecionada = spinnerPrioridade.selectedItem.toString()
                aplicarFiltroAvancado(estadoSelecionado, prioridadeSelecionada)
            }
            .setNegativeButton(R.string.btn_cancelar, null)
            .show()
    }

    private fun criarCardAvaria(avaria: Avaria): LinearLayout {
        val context = this
        val card = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(24, 16, 24, 16)
            setBackgroundColor(resources.getColor(R.color.card_bg))
            layoutParams = ViewGroup.MarginLayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply { bottomMargin = 24 }
            setOnClickListener {
                val intent = Intent(context, AvariaDetalheActivity::class.java)
                intent.putExtra("id_avaria", avaria.id_avaria)
                startActivity(intent)
            }
        }

        val userType = sessionManager.getUserType()

        val titulo = TextView(context).apply {
            text = avaria.descricao_equipamento
            setTextColor(resources.getColor(R.color.texto_principal))
            textSize = 16f
            setPadding(0, 0, 0, 4)
        }

        val prioridade = TextView(context).apply {
            text = avaria.grau_urgencia ?: "Sem prioridade"
            textSize = 14f
            setPadding(0, 0, 0, 4)
            gravity = Gravity.START
            setTextColor(
                when (avaria.grau_urgencia?.lowercase()) {
                    "baixa" -> resources.getColor(R.color.prioridade_baixa)
                    "média" -> resources.getColor(R.color.prioridade_media)
                    "alta" -> resources.getColor(R.color.prioridade_alta)
                    else -> resources.getColor(R.color.texto_secundario)
                }
            )
        }

        val estado = TextView(context).apply {
            text = avaria.estado.descricao
            textSize = 14f
            setPadding(0, 0, 0, 4)
            setTextColor(resources.getColor(R.color.texto_secundario))
        }

        val data = TextView(context).apply {
            text = avaria.data_registo
            textSize = 14f
            setPadding(0, 0, 0, 4)
            setTextColor(resources.getColor(R.color.texto_secundario))
        }

        val descricao = TextView(context).apply {
            text = avaria.descricao
            textSize = 14f
            setTextColor(resources.getColor(R.color.texto_principal))
        }

        card.addView(titulo)
        card.addView(prioridade)
        card.addView(estado)
        card.addView(data)
        card.addView(descricao)

        return card
    }
}
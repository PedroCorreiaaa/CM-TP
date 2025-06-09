package pt.techcare.app.ui.avaria.monitorizar

import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.widget.LinearLayout
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMonitorizarAvariaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)

        lifecycleScope.launchWhenStarted {
            viewModel.avarias.collectLatest { lista ->
                binding.listaAvarias.removeAllViews()
                lista.forEach { avaria ->
                    val card = criarCardAvaria(avaria)
                    binding.listaAvarias.addView(card)
                }
            }
        }

        val userType = sessionManager.getUserType()
        val userId = sessionManager.getUserId()
        viewModel.carregarAvarias(userType, userId)
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
            ).apply {
                bottomMargin = 24
            }
            setOnClickListener {
                val intent = Intent(context, AvariaDetalheActivity::class.java)
                intent.putExtra("id_avaria", avaria.id_avaria)
                startActivity(intent)
            }
        }

        val titulo = TextView(context).apply {
            text = avaria.descricao_equipamento
            setTextColor(resources.getColor(R.color.texto_principal))
            textSize = 16f
            setPadding(0, 0, 0, 4)
        }

        val prioridade = TextView(context).apply {
            text = avaria.grau_urgencia
            textSize = 14f
            setPadding(0, 0, 0, 4)
            gravity = Gravity.START
            setTextColor(
                if (avaria.grau_urgencia.equals("Baixa", ignoreCase = true))
                    resources.getColor(R.color.prioridade_baixa)
                else
                    resources.getColor(R.color.prioridade_pendente)
            )
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
        card.addView(data)
        card.addView(descricao)

        return card
    }
}
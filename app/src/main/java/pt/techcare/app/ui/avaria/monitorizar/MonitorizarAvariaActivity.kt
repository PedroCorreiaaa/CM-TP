package pt.techcare.app.ui.avaria.monitorizar

import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.flow.collectLatest
import pt.techcare.app.R
import pt.techcare.app.data.model.AvariaItem
import pt.techcare.app.databinding.ActivityMonitorizarAvariaBinding
import pt.techcare.app.viewmodel.AvariaViewModel

class MonitorizarAvariaActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMonitorizarAvariaBinding
    private val viewModel = AvariaViewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMonitorizarAvariaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        lifecycleScope.launchWhenStarted {
            viewModel.avarias.collectLatest { lista ->
                binding.listaAvarias.removeAllViews()
                lista.forEach { avaria ->
                    val card = criarCardAvaria(avaria)
                    binding.listaAvarias.addView(card)
                }
            }
        }

        viewModel.carregarAvarias()
    }

    private fun criarCardAvaria(avaria: AvariaItem): LinearLayout {
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
        }

        val titulo = TextView(context).apply {
            text = avaria.titulo
            setTextColor(resources.getColor(R.color.texto_principal))
            textSize = 16f
            setPadding(0, 0, 0, 4)
        }

        val prioridade = TextView(context).apply {
            text = avaria.prioridade
            textSize = 14f
            setPadding(0, 0, 0, 4)
            gravity = Gravity.START
            setTextColor(
                if (avaria.prioridade == "Baixa")
                    resources.getColor(R.color.prioridade_baixa)
                else
                    resources.getColor(R.color.prioridade_pendente)
            )
        }

        val data = TextView(context).apply {
            text = avaria.data
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
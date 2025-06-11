package pt.techcare.app.ui.relatorios

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import pt.techcare.app.databinding.ActivityRelatorioBinding
import pt.techcare.app.viewmodel.RelatorioViewModel

class RelatorioActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRelatorioBinding
    private lateinit var viewModel: RelatorioViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRelatorioBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = RelatorioViewModel()

        lifecycleScope.launch {
            viewModel.estatisticas.collectLatest { estat ->
                estat?.let {
                    binding.txtTotalAvarias.text = "Total: ${it.total_avarias}"
                    binding.txtResolvidas.text = "Resolvidas: ${it.resolvidas}"
                    binding.txtTempoMedio.text = "Tempo médio: ${it.tempo_medio_dias} dias"
                }
            }
        }

        // Carregar sem filtro inicialmente
        viewModel.carregarEstatisticas()

        binding.btnFiltrar.setOnClickListener {
            val datasFiltro = binding.inputDatas.text.toString().trim()
            val tipoEquipamentoFiltro = binding.inputTipoEquipamento.text.toString().trim()
            val localizacaoFiltro = binding.inputLocalizacao.text.toString().trim()

            if (datasFiltro.isEmpty() && tipoEquipamentoFiltro.isEmpty() && localizacaoFiltro.isEmpty()) {
                Toast.makeText(this, "Por favor, preencha pelo menos um filtro para aplicar.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            viewModel.carregarEstatisticasComFiltro(datasFiltro, tipoEquipamentoFiltro, localizacaoFiltro)
        }


        binding.btnVerDetalhes.setOnClickListener {
            Toast.makeText(this, "Funcionalidade de ver detalhes ainda não implementada", Toast.LENGTH_SHORT).show()
        }
    }
}

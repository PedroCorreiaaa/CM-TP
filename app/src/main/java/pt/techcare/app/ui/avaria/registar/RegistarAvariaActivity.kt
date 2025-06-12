package pt.techcare.app.ui.avaria.registar

import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import pt.techcare.app.R
import pt.techcare.app.data.api.ApiClient
import pt.techcare.app.data.model.AvariaRequest
import pt.techcare.app.data.repository.AvariaRepository
import pt.techcare.app.databinding.ActivityRegistarAvariaBinding
import pt.techcare.app.util.SessionManager

class RegistarAvariaActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegistarAvariaBinding
    private lateinit var sessionManager: SessionManager
    private val avariaRepository = AvariaRepository(ApiClient.apiService)
    private var selectedImageUri: Uri? = null

    private val selectImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            selectedImageUri = it
            binding.imgPreview.setImageURI(it)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegistarAvariaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)

        binding.btnAddFoto.setOnClickListener {
            selectImageLauncher.launch("image/*")
        }

        binding.btnSubmeter.setOnClickListener {
            val equipamento = binding.etEquipamento.text.toString().trim()
            val localizacao = binding.etLocalizacao.text.toString().trim()
            val descricao = binding.etDescricao.text.toString().trim()

            if (equipamento.isBlank() || localizacao.isBlank() || descricao.isBlank()) {
                Toast.makeText(this, R.string.toast_preencha_campos, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val novaAvaria = AvariaRequest(
                id_utilizador = sessionManager.getUserId(),
                descricao = descricao,
                descricao_equipamento = equipamento,
                localizacao = localizacao
            )

            lifecycleScope.launch {
                try {
                    val response = avariaRepository.registarAvaria(novaAvaria)
                    if (response.isSuccessful) {
                        val avariaId = response.body()?.id_avaria
                        selectedImageUri?.let { uri ->
                            val stream = contentResolver.openInputStream(uri)
                            val bytes = stream?.readBytes()
                            if (bytes != null) {
                                val requestFile = RequestBody.create("image/*".toMediaTypeOrNull(), bytes)
                                val body = MultipartBody.Part.createFormData("imagem", "avaria_$avariaId.jpg", requestFile)
                                avariaRepository.uploadImagem(avariaId!!, body)
                            } else {
                                Toast.makeText(this@RegistarAvariaActivity, R.string.toast_erro_carregar_imagem, Toast.LENGTH_SHORT).show()
                            }
                        }
                        Toast.makeText(this@RegistarAvariaActivity, R.string.toast_avaria_criada, Toast.LENGTH_LONG).show()
                        finish()
                    } else {
                        Toast.makeText(
                            this@RegistarAvariaActivity,
                            getString(R.string.erro_registar_avaria, response.code()),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(this@RegistarAvariaActivity, "Erro: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
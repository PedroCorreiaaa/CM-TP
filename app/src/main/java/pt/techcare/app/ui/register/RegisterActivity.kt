package pt.techcare.app.ui.register

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import pt.techcare.app.data.api.ApiClient
import pt.techcare.app.data.model.RegisterRequest
import pt.techcare.app.databinding.ActivityRegisterBinding

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnBack.setOnClickListener {
            finish()
        }

        binding.btnRegister.setOnClickListener {
            val nome = binding.etNome.text.toString()
            val email = binding.etEmail.text.toString()
            val password = binding.etPassword.text.toString()
            val confirmPassword = binding.etConfirmPassword.text.toString()

            if (nome.isNotBlank() && email.isNotBlank() && password.isNotBlank() && confirmPassword.isNotBlank()) {
                if (password == confirmPassword) {

                    // Faz o registo via API
                    lifecycleScope.launch {
                        try {
                            val request = RegisterRequest(nome, email, password)
                            val response = ApiClient.apiService.register(request)

                            if (response.isSuccessful) {
                                val registerResponse = response.body()
                                if (registerResponse != null) {
                                    if (registerResponse.success) {
                                        Toast.makeText(this@RegisterActivity, registerResponse.message, Toast.LENGTH_SHORT).show()
                                        finish()
                                    } else {
                                        // Caso a API retorne sucesso = false
                                        Toast.makeText(this@RegisterActivity, registerResponse.message, Toast.LENGTH_SHORT).show()
                                    }
                                } else {
                                    Toast.makeText(this@RegisterActivity, "Resposta inválida do servidor", Toast.LENGTH_SHORT).show()
                                }
                            } else {
                                val errorMsg = response.errorBody()?.string() ?: "Erro ao registar"
                                Toast.makeText(this@RegisterActivity, errorMsg, Toast.LENGTH_SHORT).show()
                            }

                        } catch (e: Exception) {
                            Toast.makeText(this@RegisterActivity, "Erro: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                    }


                } else {
                    Toast.makeText(this, "As palavras-passe não coincidem", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Preencha todos os campos", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
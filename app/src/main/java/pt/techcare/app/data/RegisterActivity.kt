package pt.techcare.app.data

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.flow.collectLatest
import pt.techcare.app.data.api.ApiClient
import pt.techcare.app.data.repository.AuthRepository
import pt.techcare.app.databinding.ActivityRegisterBinding
import pt.techcare.app.ui.home.gestor.HomeGestorActivity
import pt.techcare.app.ui.home.tecnico.HomeTecnicoActivity
import pt.techcare.app.ui.home.user.HomeUserActivity
import pt.techcare.app.util.SessionManager
import pt.techcare.app.viewmodel.AuthViewModel

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private lateinit var viewModel: AuthViewModel
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)
        viewModel = AuthViewModel(AuthRepository(ApiClient.apiService))

        binding.btnBack.setOnClickListener {
            finish()
        }

        binding.btnRegister.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()
            val confirmPassword = binding.etConfirmPassword.text.toString().trim()

            val nome = email.substringBefore("@").replaceFirstChar { it.uppercaseChar() }

            if (email.isNotBlank() && password.isNotBlank() && confirmPassword.isNotBlank()) {
                if (password == confirmPassword) {
                    viewModel.register(nome, email, password)
                } else {
                    Toast.makeText(this, "As palavras-passe não coincidem", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Preencha todos os campos", Toast.LENGTH_SHORT).show()
            }
        }

        lifecycleScope.launchWhenStarted {
            viewModel.loginState.collectLatest { response ->
                response?.let {
                    if (it.success && it.user != null) {
                        val user = it.user

                        sessionManager.saveUser(
                            user.id_utilizador,
                            user.nome,
                            user.email,
                            user.id_tipo_utilizador
                        )

                        when (user.id_tipo_utilizador) {
                            1 -> startActivity(Intent(this@RegisterActivity, HomeUserActivity::class.java))
                            2 -> startActivity(Intent(this@RegisterActivity, HomeTecnicoActivity::class.java))
                            3 -> startActivity(Intent(this@RegisterActivity, HomeGestorActivity::class.java))
                            else -> Toast.makeText(this@RegisterActivity, "Tipo de utilizador desconhecido.", Toast.LENGTH_SHORT).show()
                        }

                        finish()
                    } else {
                        Toast.makeText(this@RegisterActivity, it.message, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }
}
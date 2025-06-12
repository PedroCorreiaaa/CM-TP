package pt.techcare.app.ui.login

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.flow.collectLatest
import pt.techcare.app.R
import pt.techcare.app.data.api.ApiClient
import pt.techcare.app.data.repository.AuthRepository
import pt.techcare.app.databinding.ActivityLoginBinding
import pt.techcare.app.ui.home.gestor.HomeGestorActivity
import pt.techcare.app.ui.home.tecnico.HomeTecnicoActivity
import pt.techcare.app.ui.home.user.HomeUserActivity
import pt.techcare.app.ui.register.RegisterActivity
import pt.techcare.app.util.SessionManager
import pt.techcare.app.viewmodel.AuthViewModel

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var viewModel: AuthViewModel
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sessionManager = SessionManager(this)

        if (sessionManager.isLoggedIn()) {
            when (sessionManager.getUserType()) {
                1 -> startActivity(Intent(this, HomeUserActivity::class.java))
                2 -> startActivity(Intent(this, HomeTecnicoActivity::class.java))
                3 -> startActivity(Intent(this, HomeGestorActivity::class.java))
            }
            finish()
            return
        }

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = AuthViewModel(AuthRepository(ApiClient.apiService))

        binding.linkToRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            if (email.isNotBlank() && password.isNotBlank()) {
                viewModel.login(email, password)
            } else {
                Toast.makeText(this, R.string.toast_preencha_campos, Toast.LENGTH_SHORT).show()
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
                            1 -> startActivity(Intent(this@LoginActivity, HomeUserActivity::class.java))
                            2 -> startActivity(Intent(this@LoginActivity, HomeTecnicoActivity::class.java))
                            3 -> startActivity(Intent(this@LoginActivity, HomeGestorActivity::class.java))
                            else -> Toast.makeText(this@LoginActivity, "Tipo de utilizador desconhecido.", Toast.LENGTH_SHORT).show()
                        }

                        finish()
                    } else {
                        Toast.makeText(this@LoginActivity, it.message, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }
}
package pt.techcare.app.ui.home.user

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import pt.techcare.app.databinding.ActivityHomeUserBinding
import pt.techcare.app.ui.login.LoginActivity
import pt.techcare.app.util.SessionManager

class HomeUserActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeUserBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeUserBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val sessionManager = SessionManager(this)
        val nome = sessionManager.getUserName() ?: "Utilizador"

        binding.txtWelcome.text = "Bem-vindo, $nome 👋"

        binding.btnLogout.setOnClickListener {
            sessionManager.clearSession()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }
}
package pt.techcare.app.ui.home.gestor

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import pt.techcare.app.databinding.ActivityHomeGestorBinding
import pt.techcare.app.ui.login.LoginActivity
import pt.techcare.app.ui.relatorios.RelatorioActivity
import pt.techcare.app.util.SessionManager

class HomeGestorActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeGestorBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeGestorBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val sessionManager = SessionManager(this)
        val nome = sessionManager.getUserName() ?: "Gestor"
        binding.txtWelcome.text = "Bem-vindo, $nome 🧑‍💼"

        binding.btnLogout.setOnClickListener {
            sessionManager.clearSession()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        binding.btnRelatorios.setOnClickListener {
            val intent = Intent(this, RelatorioActivity::class.java)
            startActivity(intent)
        }
    }
}
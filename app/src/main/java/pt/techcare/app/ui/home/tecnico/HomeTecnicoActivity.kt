package pt.techcare.app.ui.home.tecnico

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import pt.techcare.app.databinding.ActivityHomeTecnicoBinding
import pt.techcare.app.ui.login.LoginActivity
import pt.techcare.app.util.SessionManager

class HomeTecnicoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeTecnicoBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeTecnicoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val sessionManager = SessionManager(this)
        val nome = sessionManager.getUserName() ?: "Técnico"


        binding.btnLogout.setOnClickListener {
            sessionManager.clearSession()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }
}
package pt.techcare.app.ui.home.tecnico

import android.content.Intent
import android.os.Bundle
import android.widget.PopupMenu
import androidx.appcompat.app.AppCompatActivity
import pt.techcare.app.R
import pt.techcare.app.databinding.ActivityHomeTecnicoBinding
import pt.techcare.app.ui.avaria.monitorizar.MonitorizarAvariaActivity
import pt.techcare.app.ui.avaria.registar.RegistarAvariaActivity
import pt.techcare.app.ui.login.LoginActivity
import pt.techcare.app.ui.notificacoes.NotificacoesTecnicoActivity
import pt.techcare.app.util.SessionManager

class HomeTecnicoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeTecnicoBinding
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeTecnicoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)

        binding.btnLogout.setOnClickListener {
            val popup = PopupMenu(this, it)
            popup.menuInflater.inflate(R.menu.menu_user, popup.menu)
            popup.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.menu_logout -> {
                        sessionManager.clearSession()
                        startActivity(Intent(this, LoginActivity::class.java))
                        finish()
                        true
                    }
                    else -> false
                }
            }
            popup.show()
        }

        binding.btnRegistar.setOnClickListener {
            startActivity(Intent(this, RegistarAvariaActivity::class.java))
        }

        binding.btnMonitorizar.setOnClickListener {
            startActivity(Intent(this, MonitorizarAvariaActivity::class.java))
        }

        binding.btnNotificacoes.setOnClickListener {
            startActivity(Intent(this, NotificacoesTecnicoActivity::class.java))
        }
    }
}
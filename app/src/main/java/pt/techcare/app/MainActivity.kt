package pt.techcare.app

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import pt.techcare.app.ui.home.gestor.HomeGestorActivity
import pt.techcare.app.ui.home.tecnico.HomeTecnicoActivity
import pt.techcare.app.ui.home.user.HomeUserActivity
import pt.techcare.app.ui.login.LoginActivity
import pt.techcare.app.util.NotificationUtils
import pt.techcare.app.util.SessionManager

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        NotificationUtils.criarCanalNotificacoes(this)

        val sessionManager = SessionManager(this)

        if (sessionManager.isLoggedIn()) {
            when (sessionManager.getUserType()) {
                1 -> startActivity(Intent(this, HomeUserActivity::class.java))
                2 -> startActivity(Intent(this, HomeTecnicoActivity::class.java))
                3 -> startActivity(Intent(this, HomeGestorActivity::class.java))
                else -> startActivity(Intent(this, LoginActivity::class.java))
            }
        } else {
            startActivity(Intent(this, LoginActivity::class.java))
        }

        finish()
    }
}
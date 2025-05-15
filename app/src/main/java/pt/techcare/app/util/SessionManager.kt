package pt.techcare.app.util

import android.content.Context
import android.content.SharedPreferences

class SessionManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("user_session", Context.MODE_PRIVATE)

    fun saveUser(id: Int, nome: String, email: String, tipo: Int) {
        prefs.edit().apply {
            putInt("id_utilizador", id)
            putString("nome", nome)
            putString("email", email)
            putInt("id_tipo_utilizador", tipo)
            apply()
        }
    }

    fun getUserId(): Int = prefs.getInt("id_utilizador", -1)
    fun getUserName(): String? = prefs.getString("nome", null)
    fun getUserEmail(): String? = prefs.getString("email", null)
    fun getUserType(): Int = prefs.getInt("id_tipo_utilizador", -1)

    fun clearSession() {
        prefs.edit().clear().apply()
    }

    fun isLoggedIn(): Boolean = getUserId() != -1
}
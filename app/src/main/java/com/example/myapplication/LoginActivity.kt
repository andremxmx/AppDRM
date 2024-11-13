package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val username = findViewById<EditText>(R.id.username)
        val password = findViewById<EditText>(R.id.password)
        val loginButton = findViewById<Button>(R.id.loginButton)

        loginButton.setOnClickListener {
            // Verificar credenciales locales primero
            if (username.text.toString() == "strix" && password.text.toString() == "1155") {
                // Login exitoso local
                getSharedPreferences("login", MODE_PRIVATE).edit()
                    .putBoolean("isLoggedIn", true)
                    .apply()

                startActivity(Intent(this, HomeActivity::class.java))
                finish()
                return@setOnClickListener
            }

            // Si no son las credenciales locales, intentar con el servidor
            val deviceId = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)
            val url = "https://dslive.site/userAuth/auth.php?user=${username.text}&pass=${password.text}&deviceId=$deviceId"

            val queue = Volley.newRequestQueue(this)
            val stringRequest = StringRequest(
                Request.Method.GET, url,
                { response ->
                    if (response.contains("success")) {
                        // Guardar estado de login
                        getSharedPreferences("login", MODE_PRIVATE).edit()
                            .putBoolean("isLoggedIn", true)
                            .apply()

                        startActivity(Intent(this, HomeActivity::class.java))
                        finish()
                    } else {
                        Toast.makeText(this, "Login fallido", Toast.LENGTH_SHORT).show()
                    }
                },
                { error ->
                    Toast.makeText(this, "Error de conexión", Toast.LENGTH_SHORT).show()
                }
            )
            queue.add(stringRequest)
        }
    }
}
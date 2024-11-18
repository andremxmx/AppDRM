package com.example.myapplication

import android.content.Intent
import android.graphics.drawable.AnimationDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Set up animated background with null safety
        val constraintLayout = findViewById<ConstraintLayout>(R.id.loginContainer)
        constraintLayout.background?.let {
            if (it is AnimationDrawable) {
                it.setEnterFadeDuration(2000)
                it.setExitFadeDuration(4000)
                it.start()
            }
        }

        val username = findViewById<EditText>(R.id.username)
        val password = findViewById<EditText>(R.id.password)
        val loginButton = findViewById<Button>(R.id.loginButton)
        val loginProgress = findViewById<ProgressBar>(R.id.loginProgress)

        // Configurar navegación con D-pad
        setupTvNavigation(username, password, loginButton)

        loginButton.setOnClickListener {
            // Show loading indicator
            loginButton.isEnabled = false
            loginProgress.visibility = View.VISIBLE

            // Delay the login process
            Handler(Looper.getMainLooper()).postDelayed({
                // Verificar credenciales locales primero
                if (username.text.toString() == "strix" && password.text.toString() == "1155") {
                    // Login exitoso local
                    getSharedPreferences("login", MODE_PRIVATE).edit()
                        .putBoolean("isLoggedIn", true)
                        .apply()

                    startActivity(Intent(this, HomeActivity::class.java))
                    finish()
                } else {
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

                loginButton.isEnabled = true
                loginProgress.visibility = View.GONE
            }, 1000)
        }
    }

    private fun setupTvNavigation(username: EditText, password: EditText, loginButton: Button) {
        val scaleUp = 1.05f
        
        listOf(username, password, loginButton).forEach { view ->
            view.setOnFocusChangeListener { v, hasFocus ->
                v.animate()
                    .scaleX(if (hasFocus) scaleUp else 1.0f)
                    .scaleY(if (hasFocus) scaleUp else 1.0f)
                    .translationZ(if (hasFocus) 8f else 0f)
                    .duration = 200
            }
        }
    }
}
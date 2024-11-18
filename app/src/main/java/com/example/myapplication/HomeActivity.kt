package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import android.graphics.drawable.TransitionDrawable
import android.view.animation.OvershootInterpolator
import android.widget.Toast

class HomeActivity : AppCompatActivity() {
    private lateinit var backgroundImage: ImageView
    private lateinit var menuDescription: TextView
    private val menuItems = listOf(
        MenuItem("TV en vivo", R.drawable.ic_tv_placeholder, "Disfruta de los mejores canales en vivo"),
        MenuItem("Películas", R.drawable.ic_movie_placeholder, "Las mejores películas en HD"),
        MenuItem("Series", R.drawable.ic_series_placeholder, "Las series más populares"),
        MenuItem("Música", R.drawable.ic_music_placeholder, "Tu música favorita")
    )

    data class MenuItem(
        val title: String, 
        val backgroundRes: Int,
        val description: String
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Verificar si está logueado
        if (!getSharedPreferences("login", MODE_PRIVATE).getBoolean("isLoggedIn", false)) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        setContentView(R.layout.activity_home)

        // Enhanced title animation
        findViewById<TextView>(R.id.title).apply {
            alpha = 0f
            translationY = -100f
            animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(1200)
                .setInterpolator(android.view.animation.OvershootInterpolator())
                .start()
        }

        backgroundImage = findViewById(R.id.backgroundImage)
        menuDescription = findViewById(R.id.menuDescription)

        val recyclerView = findViewById<RecyclerView>(R.id.menuRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = ModernMenuAdapter(menuItems) { position ->
            startActivityForPosition(position)
        }

        // Set initial background
        updateBackground(0, animate = false)
    }

    private fun updateBackground(position: Int, animate: Boolean = true) {
        val newBackground = ContextCompat.getDrawable(this, menuItems[position].backgroundRes)
        
        if (animate) {
            val crossfade = TransitionDrawable(arrayOf(
                backgroundImage.drawable,
                newBackground
            ))
            backgroundImage.setImageDrawable(crossfade)
            crossfade.startTransition(300)

            menuDescription.animate()
                .alpha(0f)
                .setDuration(150)
                .withEndAction {
                    menuDescription.text = menuItems[position].description
                    menuDescription.animate()
                        .alpha(1f)
                        .setDuration(150)
                        .start()
                }.start()
        } else {
            backgroundImage.setImageDrawable(newBackground)
            menuDescription.text = menuItems[position].description
            menuDescription.alpha = 1f
        }
    }

    inner class ModernMenuAdapter(
        private val items: List<MenuItem>,
        private val onItemClick: (Int) -> Unit
    ) : RecyclerView.Adapter<ModernMenuAdapter.ViewHolder>() {

        inner class ViewHolder(val textView: TextView) : RecyclerView.ViewHolder(textView)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.menu_item_modern, parent, false) as TextView
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.textView.text = items[position].title
            
            holder.textView.setOnFocusChangeListener { _, hasFocus ->
                holder.textView.animate()
                    .scaleX(if (hasFocus) 1.2f else 1f)
                    .scaleY(if (hasFocus) 1.2f else 1f)
                    .translationX(if (hasFocus) 30f else 0f)
                    .setInterpolator(OvershootInterpolator())
                    .setDuration(300)
                    .start()

                if (hasFocus) {
                    updateBackground(position)
                }
            }

            holder.textView.setOnClickListener {
                onItemClick(position)
            }
        }

        override fun getItemCount() = items.size
    }

    private fun startActivityForPosition(position: Int) {
        try {
            val intent = when (position) {
                0 -> Intent(this, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                }
                1 -> Intent(this, VodActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                2 -> Intent(this, SeriesActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                3 -> Intent(this, MusicActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                else -> null
            }
            
            intent?.let {
                startActivity(it)
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            // Mostrar un mensaje de error al usuario
            Toast.makeText(this, "Error al abrir la actividad: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}
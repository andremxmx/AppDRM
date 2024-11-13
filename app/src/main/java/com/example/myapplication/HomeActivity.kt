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

class HomeActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Verificar si está logueado
        if (!getSharedPreferences("login", MODE_PRIVATE).getBoolean("isLoggedIn", false)) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        setContentView(R.layout.activity_home)

        // Animate title
        findViewById<TextView>(R.id.title).apply {
            alpha = 0f
            translationY = -50f
            animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(1000)
                .setInterpolator(android.view.animation.DecelerateInterpolator())
                .start()
        }

        recyclerView = findViewById(R.id.main_menu_recycler)
        recyclerView.layoutManager = GridLayoutManager(this, 2) // Changed to 2 columns for better layout
        recyclerView.adapter = MainMenuAdapter(getMenuItems()) { position ->
            when (position) {
                0 -> startActivity(Intent(this, MainActivity::class.java))
                // Comment out until activities are implemented
                1 -> startActivity(Intent(this, MoviesActivity::class.java))
                /*2 -> startActivity(Intent(this, SeriesActivity::class.java))
                3 -> startActivity(Intent(this, MusicActivity::class.java))*/
            }
        }
    }

    private fun getMenuItems(): List<MenuItem> = listOf(
        MenuItem("TV en vivo", android.R.drawable.ic_media_play),
        MenuItem("Películas", android.R.drawable.ic_menu_gallery)  // Using a default Android icon
        /*MenuItem("Series", R.drawable.ic_series),
        MenuItem("Música", R.drawable.ic_music)*/
    )

    data class MenuItem(val title: String, val iconRes: Int)

    inner class MainMenuAdapter(
        private val items: List<MenuItem>,
        private val onItemClick: (Int) -> Unit
    ) : RecyclerView.Adapter<MainMenuAdapter.ViewHolder>() {

        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val icon: ImageView = view.findViewById(R.id.menu_icon)
            val title: TextView = view.findViewById(R.id.menu_title)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.menu_item, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = items[position]
            holder.icon.setImageResource(item.iconRes)
            holder.title.text = item.title

            holder.itemView.setOnClickListener { 
                when (position) {
                    0 -> startActivity(Intent(this@HomeActivity, MainActivity::class.java))
                    // Comment out until activities are implemented
                    1 -> startActivity(Intent(this@HomeActivity, MoviesActivity::class.java))
                    /*2 -> startActivity(Intent(this@HomeActivity, SeriesActivity::class.java))
                    3 -> startActivity(Intent(this@HomeActivity, MusicActivity::class.java))*/
                }
            }
            
            holder.itemView.setOnFocusChangeListener { v, hasFocus ->
                v.animate()
                    .scaleX(if (hasFocus) 1.1f else 1.0f)  // Reduced from 1.2f
                    .scaleY(if (hasFocus) 1.1f else 1.0f)  // Reduced from 1.2f
                    .setDuration(200)  // Reduced from 300
                    .setInterpolator(android.view.animation.DecelerateInterpolator())  // Changed interpolator
                    .start()
                
                v.elevation = if (hasFocus) 8f else 4f  // Reduced from 12f
            }
        }

        override fun getItemCount() = items.size
    }
}
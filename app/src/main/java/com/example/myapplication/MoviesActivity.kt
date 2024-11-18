package com.example.myapplication

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.myapplication.api.MovieApiService
import com.example.myapplication.models.TMDBMovie
import com.example.myapplication.viewmodels.MovieViewModel
import android.content.Intent
import android.graphics.Rect
import android.util.TypedValue

class MoviesActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_movies)

        recyclerView = findViewById(R.id.movies_recycler)
        recyclerView.layoutManager = GridLayoutManager(this, 3)
        // Set up adapter later when we have data
    }
}
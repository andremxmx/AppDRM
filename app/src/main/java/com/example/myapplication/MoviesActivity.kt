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
    private lateinit var viewModel: MovieViewModel
    private lateinit var recyclerView: RecyclerView
    private lateinit var detailPoster: ImageView
    private lateinit var detailTitle: TextView
    private lateinit var detailRating: TextView
    private lateinit var detailDuration: TextView
    private lateinit var detailOverview: TextView
    private lateinit var backgroundImage: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_movies)

        viewModel = ViewModelProvider(this)[MovieViewModel::class.java]
        recyclerView = findViewById(R.id.movies_recycler)

        recyclerView.layoutManager = GridLayoutManager(this, 9) // Changed from 5 to 9 columns
        recyclerView.addItemDecoration(object : RecyclerView.ItemDecoration() {
            override fun getItemOffsets(
                outRect: Rect,
                view: View,
                parent: RecyclerView,
                state: RecyclerView.State
            ) {
                val horizontalSpacing = TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP,
                    0.5f, // Reduced horizontal spacing
                    resources.displayMetrics
                ).toInt()

                val verticalSpacing = TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP,
                    5f, // Increased to 5dp
                    resources.displayMetrics
                ).toInt()

                outRect.set(horizontalSpacing, verticalSpacing, horizontalSpacing, verticalSpacing)
            }
        })
        recyclerView.adapter = MovieAdapter()

        viewModel.movies.observe(this) { movies ->
            (recyclerView.adapter as MovieAdapter).submitList(movies)
        }

        viewModel.error.observe(this) { error ->
            // Handle error - you might want to show a Toast or Snackbar
        }

        viewModel.loadMovies()

        // Initialize detail views
        detailPoster = findViewById(R.id.detail_poster)
        detailTitle = findViewById(R.id.detail_title)
        detailRating = findViewById(R.id.detail_rating)
        detailDuration = findViewById(R.id.detail_duration)
        detailOverview = findViewById(R.id.detail_overview)
        backgroundImage = findViewById(R.id.background_image)
    }

    inner class MovieAdapter : RecyclerView.Adapter<MovieAdapter.MovieViewHolder>() {
        private var movies = listOf<TMDBMovie>()

        fun submitList(newMovies: List<TMDBMovie>) {
            movies = newMovies
            notifyDataSetChanged()
        }

        inner class MovieViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val poster: ImageView = view.findViewById(R.id.movie_poster)
            val title: TextView = view.findViewById(R.id.movie_title)
            // Removed rating and duration references
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MovieViewHolder {
            return MovieViewHolder(
                LayoutInflater.from(parent.context).inflate(R.layout.movie_item, parent, false)
            )
        }

        override fun onBindViewHolder(holder: MovieViewHolder, position: Int) {
            val movie = movies[position]

            holder.title.text = movie.title
            // Load poster image using Glide
            Glide.with(holder.poster)
                .load("${MovieApiService.IMAGE_BASE_URL}${movie.poster_path}")
                .placeholder(R.drawable.movie_placeholder)
                .error(R.drawable.movie_error)
                .into(holder.poster)

            holder.itemView.setOnClickListener {
                val intent = Intent(this@MoviesActivity, PlayerActivity::class.java).apply {
                    putExtra("videoUrl", movie.video_url)
                    putExtra("title", movie.title)
                }
                startActivity(intent)
            }

            holder.itemView.setOnFocusChangeListener { v, hasFocus ->
                // Add padding to prevent clipping during animation
                val scale = if (hasFocus) 1.1f else 1.0f
                v.animate()
                    .scaleX(scale)
                    .scaleY(scale)
                    .translationZ(if (hasFocus) 4f else 0f) // Add elevation when focused
                    .setDuration(200)
                    .start()

                if (hasFocus) {
                    updateDetailSection(movie)
                }
            }
        }

        override fun getItemCount() = movies.size
    }

    private fun updateDetailSection(movie: TMDBMovie) {
        detailTitle.text = movie.title
        detailRating.text = "Rating: ${movie.vote_average}/10"
        detailDuration.text = "${movie.runtime} min"
        detailOverview.text = movie.overview

        // Load detail poster
        Glide.with(this)
            .load("${MovieApiService.IMAGE_BASE_URL}${movie.poster_path}")
            .into(detailPoster)

        // Load background image
        Glide.with(this)
            .load("${MovieApiService.IMAGE_BASE_URL}${movie.backdrop_path}")
            .into(backgroundImage)
    }
}
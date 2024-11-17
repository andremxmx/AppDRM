package com.example.myapplication.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.models.LocalMovie
import com.example.myapplication.models.TMDBMovie
import com.example.myapplication.api.MovieApiService
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.net.URL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request

class MovieViewModel : ViewModel() {
    private val _movies = MutableLiveData<List<TMDBMovie>>()
    val movies: LiveData<List<TMDBMovie>> = _movies

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    private val okHttpClient = MovieApiService.createOkHttpClient()
    private val retrofit = Retrofit.Builder()
        .baseUrl(MovieApiService.BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val movieService = retrofit.create(MovieApiService::class.java)

    private suspend fun fetchMoviesFromJson(): List<LocalMovie> {
        return withContext(Dispatchers.IO) {
            try {
                val request = Request.Builder()
                    .url("https://dslive.site/json/movies.json")
                    .build()
                
                val response = okHttpClient.newCall(request).execute()
                val jsonString = response.body?.string() ?: ""
                
                if (jsonString.isNotEmpty()) {
                    val movieListType = object : TypeToken<List<LocalMovie>>() {}.type
                    Gson().fromJson(jsonString, movieListType)
                } else {
                    emptyList()
                }
            } catch (e: Exception) {
                println("Error fetching JSON: ${e.message}")
                emptyList()
            }
        }
    }

    fun loadMovies() {
        viewModelScope.launch {
            try {
                val localMovies = fetchMoviesFromJson()
                
                val tmdbMovies = localMovies.mapNotNull { localMovie ->
                    try {
                        val movie = movieService.getMovieDetails(localMovie.tmdb_id)
                        // Set the video URL from the local movie data
                        movie.copy(video_url = localMovie.url)
                    } catch (e: Exception) {
                        println("Error loading movie ${localMovie.tmdb_id}: ${e.message}")
                        null
                    }
                }
                
                if (tmdbMovies.isEmpty()) {
                    _error.postValue("No movies could be loaded")
                } else {
                    _movies.postValue(tmdbMovies)
                }
            } catch (e: Exception) {
                _error.postValue("Error: ${e.message}")
                println("General error: ${e.message}")
            }
        }
    }
}
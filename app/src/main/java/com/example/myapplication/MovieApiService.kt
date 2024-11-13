package com.example.myapplication.api

import com.example.myapplication.models.TMDBMovie
import retrofit2.http.GET
import retrofit2.http.Path

interface MovieApiService {
    @GET("movie/{id}?api_key=04a646a3d3b703752123ed76e1ecc62f")
    suspend fun getMovieDetails(@Path("id") id: Int): TMDBMovie

    companion object {
        const val BASE_URL = "https://api.themoviedb.org/3/"
        const val IMAGE_BASE_URL = "https://image.tmdb.org/t/p/w500"
    }
}
package com.example.myapplication.models

data class LocalMovie(
    val url: String,
    val tmdb_id: Int
)

data class TMDBMovie(
    val id: Int,
    val title: String,
    val overview: String,
    val poster_path: String,
    val release_date: String,
    val video_url: String = "", // This will be set from LocalMovie.url
    val vote_average: Double,
    val runtime: Int,
    val backdrop_path: String?
)
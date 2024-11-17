
package com.example.myapplication.models

data class VodItem(
    val titulo: String,
    val descripcion: String,
    val imgv: String,
    val fondo: String,
    val duracion: String,
    val year: String,
    val cat: String,
    val url: String,
    val key: String
)

data class VodResponse(
    val data: List<VodItem>
)
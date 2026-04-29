package com.example.jardinemoi.data.trefle

data class TreflePlantResponse(
    val data: List<TreflePlant>
)

data class TreflePlant(
    val id: Int,
    val common_name: String?,
    val scientific_name: String,
    val image_url: String?,
    val family: String?,
    val genus: String?
)

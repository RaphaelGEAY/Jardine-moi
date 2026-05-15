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

data class TreflePlantDetailResponse(
    val data: TreflePlantDetail
)

data class TreflePlantDetail(
    val id: Int,
    val common_name: String?,
    val scientific_name: String,
    val main_species: TrefleSpecies?
)

data class TrefleSpecies(
    val family: String?,
    val genus: String?,
    val image_url: String?,
    val growth: TrefleGrowth?
)

data class TrefleGrowth(
    val light: Int?, // 0-10 scale usually or specific mapping
    val minimum_precipitation: Map<String, Double>?,
    val maximum_precipitation: Map<String, Double>?
)

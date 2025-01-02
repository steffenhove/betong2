package no.steffenhove.betongkalkulator

data class Calculation(
    val volume: Double,
    val weight: Double,
    val shape: String,
    val dimensions: String,
    val datetime: String,
    val unitSystem: String
)
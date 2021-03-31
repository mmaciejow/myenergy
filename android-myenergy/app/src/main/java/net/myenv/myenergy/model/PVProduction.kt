package net.myenv.myenergy.model

data class PVProduction(
    val date: Long = 0,
    val production: Float = 0F,
    val power: Float = 0F,
    val weather_status: String? = null,
    val weather_temp: Int? = null,
    val weather_icon: String? = null,
)
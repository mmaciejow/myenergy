package net.myenv.myenergy.model

data class EnergyDay(
    val date: Long = 0,
    val production: Float = 0F,
    val avg_power: Float = 0F,
    val max_power: Float = 0F,
    val sunrise: Long? = null,
    val sunset: Long? = null,
    val weather_status: String? = null,
    val weather_temp: Int? = null,
    val weather_icon: String? = null
)

package net.myenv.myenergy.model

data class Energy(
        var id: Long? = null,
        var date: String ,
        var pv_production: Float,
        var pv_productionList: List<PVProduction>? = null,
        var power: Float? = null,
        var max_power: Int? = null,
        var avg_power: Int? = null,
        var sunrise: Long? = null,
        var sunset: Long? = null,
        var weather_status: String? = null,
        var weather_temp: Int? = null,
        var weather_icon: Int? = null
)

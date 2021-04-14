package net.myenv.myenergy.model

data class Energy(
        var id: Long? = null,
        var date: String,
        var pvProduction: Float,
        var pvProductionList: List<PVProduction>? = null,
        var power: Float? = null,
        var maxPower: Int? = null,
        var avgPower: Int? = null,
        var sunrise: Long? = null,
        var sunset: Long? = null,
        var weatherStatus: String? = null,
        var weatherTemp: Int? = null,
        var weatherIcon: Int? = null
)

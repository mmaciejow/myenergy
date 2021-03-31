package net.myenv.myenergy.model

data class EnergyStats(
        var pv_production: Float = 0F,
        var max_power: Float = 0F,
        var max_power_date: Long = 0
)

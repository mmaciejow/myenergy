package net.myenv.myenergy.utils

import net.myenv.myenergy.model.PVProduction
import java.util.*


fun groupProductionByHours(list: List<PVProduction>): MutableMap<Float, Float> {
    val calendar = GregorianCalendar.getInstance()

    val groupByHours = list.groupBy {
        calendar.time = Date(it.date)
        calendar.get(Calendar.HOUR_OF_DAY)
    }

    val listProduction = mutableMapOf<Float, Float>()
    var lastProduction = 0
    for (entry in groupByHours) {
        val hour = entry.key.toFloat()
        val production = (entry.value.last().production * 1000).toInt() // kw - w
        val diff = production - lastProduction
        lastProduction = production
        listProduction[hour] = (diff.toFloat() / 1000)
    }

    return listProduction

}

fun groupPowerByHours(list: List<PVProduction>): MutableMap<Float, Float> {
    val calendar = GregorianCalendar.getInstance()

    val groupsHour = list.groupBy { item ->
        calendar.time = Date(item.date!!)
        calendar.get(Calendar.HOUR_OF_DAY)
    }

    val listfinally = mutableMapOf<Float, Float>()
    var lastPower = 0
    for (entry in groupsHour) {
        val hour = entry.key.toFloat()
        val power = (entry.value.last().power!! * 1000).toInt() // kw - w
        val diff = power - lastPower
        lastPower = power

        listfinally[hour] = (diff.toFloat() / 1000)


    }

    return listfinally

}

fun groupAveragePowerByHours(list: List<PVProduction>): MutableMap<Long, Float> {
    val calendar = GregorianCalendar.getInstance()

    val groupsHour = list.groupBy { item ->
        calendar.time = Date(item.date)
        calendar.get(Calendar.HOUR_OF_DAY)
    }

    val averagePower = mutableMapOf<Long, Float>()

    groupsHour.forEach{ (hour, value) ->
        var sum = 0F
        value.map { it.power }.forEach{ sum += it }
        val avg = sum / value.size
        averagePower[hour.toLong()] = avg
    }

    return averagePower

}


package net.myenv.myenergy.utils.extension

import net.myenv.myenergy.di.getKoinInstance
import java.text.SimpleDateFormat
import java.util.*

private val sdf:SimpleDateFormat = getKoinInstance()
private val calendar:Calendar = getKoinInstance()

fun getDatefromTimestamp(time: Long, format : String): String {
    val date = Date(time)
    sdf.applyPattern(format)
    return sdf.format(date)
}

fun getDateAsString(date: Date, format : String): String? {
    sdf.applyPattern(format)
    return sdf.format(date)
}

fun getTimestampFromStringDate(date: String, format : String): Long {
    sdf.applyPattern(format)
    return sdf.parse(date).time
}

fun Date.startDay(): Date {
    calendar.time = this
    calendar[Calendar.HOUR_OF_DAY] = 0
    calendar[Calendar.MINUTE] = 0
    calendar[Calendar.SECOND] = 0
    calendar[Calendar.MILLISECOND] = 0
    return calendar.time
}

fun Date.endDay(): Date {
    calendar.time = this
    calendar[Calendar.HOUR_OF_DAY] = 23
    calendar[Calendar.MINUTE] = 59
    calendar[Calendar.SECOND] = 59
    calendar[Calendar.MILLISECOND] = 999
    return calendar.time
}

fun Date.endMonth(): Date {
    calendar.time = this
    calendar.add(Calendar.MONTH,1)
    calendar.add(Calendar.MINUTE,-1)
    calendar[Calendar.HOUR_OF_DAY] = 23
    calendar[Calendar.MINUTE] = 59
    calendar[Calendar.SECOND] = 59
    calendar[Calendar.MILLISECOND] = 999
    return calendar.time
}

fun Date.endYear(): Date {
    calendar.time = this
    calendar.add(Calendar.YEAR,1)
    calendar.add(Calendar.MINUTE,-1)
    calendar[Calendar.HOUR_OF_DAY] = 23
    calendar[Calendar.MINUTE] = 59
    calendar[Calendar.SECOND] = 59
    calendar[Calendar.MILLISECOND] = 999
    return calendar.time
}

fun Date.minusDays(days: Int): Date{
    calendar.time = this
    calendar[Calendar.HOUR_OF_DAY] = 0
    calendar[Calendar.MINUTE] = 0
    calendar[Calendar.SECOND] = 0
    calendar[Calendar.MILLISECOND] = 0
    calendar.add(Calendar.DAY_OF_MONTH,-days)
    return calendar.time
}

fun Date.minusYear(year: Int): Date{
    calendar.time = this
    calendar[Calendar.DAY_OF_MONTH] = 1
    calendar[Calendar.MONTH] = 0
    calendar[Calendar.HOUR_OF_DAY] = 0
    calendar[Calendar.MINUTE] = 0
    calendar[Calendar.SECOND] = 0
    calendar[Calendar.MILLISECOND] = 0
    calendar.add(Calendar.YEAR,-year)
    return calendar.time
}

fun Date.minusMonth(month: Int): Date{
    calendar.time = this
    calendar[Calendar.DAY_OF_MONTH] = 1
    calendar[Calendar.HOUR_OF_DAY] = 0
    calendar[Calendar.MINUTE] = 0
    calendar[Calendar.SECOND] = 0
    calendar[Calendar.MILLISECOND] = 0
    calendar.add(Calendar.MONTH,-month)
    return calendar.time
}

fun Date.getDay(): Int {
    calendar.time = this
    return calendar.get(Calendar.DAY_OF_MONTH)
}

fun Date.day(): Int {
    calendar.time = this
    return calendar.get(Calendar.DAY_OF_MONTH)
}

fun Date.month(): Int {
    calendar.time = this
    return calendar.get(Calendar.MONTH)
}

fun Date.year(): Int {
    calendar.time = this
    return calendar.get(Calendar.YEAR)
}

fun Calendar.reset() {
    this.time = Date()
}

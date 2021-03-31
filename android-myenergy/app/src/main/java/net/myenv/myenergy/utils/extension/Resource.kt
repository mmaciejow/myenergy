package net.myenv.myenergy.utils.extension

import android.content.Context
import android.net.Uri
import net.myenv.myenergy.BuildConfig
import net.myenv.myenergy.di.getKoinInstance


fun getResourceIDForWeatherIcon(icon: String): Int {
    val context: Context = getKoinInstance()

    val resId = context.resources.getIdentifier(
        "weather_$icon",
        "drawable",
        context.packageName
    )
    return resId
}

fun getURLForResource(resourceId: Int): String? {
    return Uri.parse("android.resource://" + BuildConfig.APPLICATION_ID  + "/" + resourceId).toString()
}


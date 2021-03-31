package net.myenv.myenergy.di

import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.component.inject
import org.koin.core.qualifier.named

inline fun <reified T> getKoinInstance(): T {
    return object : KoinComponent { val value: T by inject() }.value
}

inline fun <reified T> getKoinInstance(qualifier: String): T {
    return object : KoinComponent { val value: T = get(named(qualifier)) }.value
}
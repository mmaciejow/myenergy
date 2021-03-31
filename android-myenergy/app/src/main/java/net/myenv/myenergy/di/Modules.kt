package net.myenv.myenergy.di

import net.myenv.myenergy.MainViewModel
import net.myenv.myenergy.data.FirestoreRepository
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.loadKoinModules
import org.koin.dsl.module

import java.text.SimpleDateFormat
import java.util.*


fun injectFeature() = loadFeature

private val loadFeature by lazy {
    loadKoinModules(
        listOf(dateModule,repositoryModule, viewModelModule)
    )
}

val dateModule = module {
    single { SimpleDateFormat("", Locale.getDefault()) }
    single { Calendar.getInstance() }
    //single(named("appContext")) { androidContext() }
}

val viewModelModule = module {
    viewModel { MainViewModel(firestoreRepository = get()) }
}

val repositoryModule = module {
    single { FirestoreRepository() }
}

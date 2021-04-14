package net.myenv.myenergy

import androidx.lifecycle.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import net.myenv.myenergy.data.EnergyType
import net.myenv.myenergy.data.FirestoreRepository
import net.myenv.myenergy.model.*
import net.myenv.myenergy.utils.extension.endDay
import net.myenv.myenergy.utils.extension.getDatefromTimestamp
import net.myenv.myenergy.utils.extension.startDay
import net.myenv.myenergy.utils.extension.getResourceIDForWeatherIcon
import java.util.*

class MainViewModel(private val firestoreRepository : FirestoreRepository) : ViewModel() {

    val pvProductionToday = MutableLiveData<Result>()
    val energyDay = MutableLiveData<Result>()
    val dailyEnergy = MutableLiveData<Result>()
    val monthlyEnergy = MutableLiveData<Result>()
    val annuallyEnergy = MutableLiveData<Result>()
    val statsEnergy = MutableLiveData<Result>()
    val inverterInfo = MutableLiveData<Result>()


    fun getRealTimeProduction(){
        pvProductionToday.value = Result.Loading
        viewModelScope.launch(Dispatchers.IO) {
            val startDate = Date().startDay().time
            val endDate = Date().endDay().time

            val productionList  = async { firestoreRepository.getEnergy(EnergyType.ALL, startDate,endDate) }.await() as List<PVProduction>
            val dayList  = async { firestoreRepository.getEnergy(EnergyType.DAILY, startDate) }.await() as List<EnergyDay>

            if (productionList.isNullOrEmpty() || dayList.isNullOrEmpty() ) {
                this@MainViewModel.pvProductionToday.postValue(Result.Failure("error"))
                return@launch
            }

            val last = productionList.last()
            val energy = Energy(
                    date = getDatefromTimestamp( last.date,"HH:mm"),
                    pvProduction = last.production,
                    power = last.power,
                    pvProductionList = productionList,
                    avgPower = (dayList[0].avg_power).toInt(),
                    maxPower = (dayList[0].max_power).toInt()
            )

            last.weather_temp?.let { energy.weatherTemp = it }
            last.weather_icon?.let { energy.weatherIcon = getResourceIDForWeatherIcon(it) }

            dayList[0].sunrise?.let { energy.sunrise = it }
            dayList[0].sunset?.let { energy.sunset = it }

            this@MainViewModel.pvProductionToday.postValue(Result.Success(energy))

        }
    }

    fun getEnergy(date: Long){
        energyDay.value = Result.Loading

        viewModelScope.launch(Dispatchers.IO) {
            val startDate = Date(date).startDay().time
            val endDate = Date(date).endDay().time

            val productionList  = async { firestoreRepository.getEnergy(EnergyType.ALL, startDate,endDate) }.await() as List<PVProduction>
            val dayList  = async { firestoreRepository.getEnergy(EnergyType.DAILY, startDate) }.await() as List<EnergyDay>

            if (productionList.isNullOrEmpty() || dayList.isNullOrEmpty() ) {
                this@MainViewModel.energyDay.postValue(Result.Failure("error"))
                return@launch
            }


            val energy = Energy(
                    date = getDatefromTimestamp( dayList[0].date,"yyyy-MM-dd HH:mm"),
                    pvProduction = dayList[0].production,
                    pvProductionList = productionList,
                    avgPower = (dayList[0].avg_power).toInt(),
                    maxPower = (dayList[0].max_power).toInt()
            )

            dayList[0].weather_temp?.let { energy.weatherTemp = it }
            dayList[0].weather_icon?.let { energy.weatherIcon = getResourceIDForWeatherIcon(it) }

            dayList[0].sunrise?.let { energy.sunrise = it }
            dayList[0].sunset?.let { energy.sunset = it }

            this@MainViewModel.energyDay.postValue(Result.Success(energy))

        }
    }

    fun getEnergyDaily(startDate: Long, endDate: Long, reload: Boolean = false){
        if (!reload) {
            dailyEnergy.value = dailyEnergy.value
            return
        }

        dailyEnergy.value = Result.Loading

        viewModelScope.launch(Dispatchers.IO) {

            val list  = async { firestoreRepository.getEnergy(EnergyType.DAILY, startDate, endDate ) }.await() as List<EnergyDay>

            if (list.isNullOrEmpty() ) {
                this@MainViewModel.dailyEnergy.postValue(Result.Failure("error"))
                return@launch
            }

            val energyList = mutableListOf<Energy>()
            list.forEach{
                val energy = Energy(
                        id = it.date,
                        date = getDatefromTimestamp(it.date,"dd-MM-yyyy"),
                        pvProduction = it.production
                )
                it.weather_icon?.let { icon-> energy.weatherIcon = getResourceIDForWeatherIcon(icon) }
                energyList.add(energy)
            }
            this@MainViewModel.dailyEnergy.postValue(Result.Success(energyList))
        }
    }

    fun getEnergyMonthly(startDate: Long, endDate: Long, reload: Boolean = false){
        if (!reload) {
            monthlyEnergy.value = monthlyEnergy.value
            return
        }
        monthlyEnergy.value = Result.Loading

        viewModelScope.launch(Dispatchers.IO) {

            val list  = async { firestoreRepository.getEnergy(EnergyType.MONTHLY, startDate, endDate ) }.await() as List<EnergyMonth>

            if (list.isNullOrEmpty() ) {
                this@MainViewModel.monthlyEnergy.postValue(Result.Failure("error"))
                return@launch
            }

            val energyList = mutableListOf<Energy>()
            list.forEach{
                val energy = Energy(
                        id = it.date,
                        date = getDatefromTimestamp(it.date,"yyyy-MM"),
                        pvProduction = it.production
                )
                energyList.add(energy)
            }

            this@MainViewModel.monthlyEnergy.postValue(Result.Success(energyList))

        }
    }

    fun getEnergyAnnually(startDate: Long, endDate: Long, reload: Boolean = false){
        if (!reload) {
            annuallyEnergy.postValue(annuallyEnergy.value)
            return
        }
        annuallyEnergy.value = Result.Loading

        viewModelScope.launch(Dispatchers.IO) {

            val list  = async { firestoreRepository.getEnergy(EnergyType.ANNUALLY, startDate, endDate ) }.await() as List<EnergyYear>

            if (list.isNullOrEmpty() ) {
                this@MainViewModel.annuallyEnergy.postValue(Result.Failure("error"))
                return@launch
            }

            val energyList = mutableListOf<Energy>()
            list.forEach{
                val energy = Energy(
                        id = it.date,
                        date = getDatefromTimestamp(it.date,"yyyy-MM"),
                        pvProduction = it.production
                )
                energyList.add(energy)
            }

            this@MainViewModel.annuallyEnergy.postValue(Result.Success(energyList))

        }
    }

    fun getStats() {
        statsEnergy.value = Result.Loading

        viewModelScope.launch(Dispatchers.IO) {
            val stats = async { firestoreRepository.getStats() }.await()

            if (stats == null ) {
                this@MainViewModel.statsEnergy.postValue(Result.Failure("error"))
                return@launch
            } else
                this@MainViewModel.statsEnergy.postValue(Result.Success(stats))
        }
    }

}

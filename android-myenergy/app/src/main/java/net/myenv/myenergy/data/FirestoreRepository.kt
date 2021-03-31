package net.myenv.myenergy.data

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.Source
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await
import net.myenv.myenergy.model.*
import java.net.ConnectException
import java.net.UnknownHostException


enum class EnergyType { ALL, DAILY, MONTHLY, ANNUALLY }

class FirestoreRepository {

    companion object {
        private const val FIRESTORE_COLLECTION_PRODUCTION_PV = "pv_production"
        private const val FIRESTORE_COLLECTION_ENERGY_DAY = "energy_day"
        private const val FIRESTORE_COLLECTION_ENERGY_MONTH = "energy_month"
        private const val FIRESTORE_COLLECTION_ENERGY_YEAR = "energy_year"
        private const val FIRESTORE_COLLECTION_STATS = "energy_stats"
    }

    private val firestoreDB = Firebase.firestore

    suspend fun getEnergy(type: EnergyType, startDate: Long, endDate: Long? = null, fromCache : Boolean = false): List<Any> {
        val db_collection = getDBCollection(type)
        val source = if (fromCache) Source.CACHE else Source.SERVER
        var data = emptyList<Any>()
        try {
            val query = if (endDate != null) {
                firestoreDB.collection(db_collection)
                    .whereGreaterThanOrEqualTo("date", startDate)
                    .whereLessThanOrEqualTo("date", endDate)
                    .get(source).await()
            }
            else {
                firestoreDB .collection(db_collection)
                    .whereEqualTo("date", startDate)
                    .get(source).await()
            }

            data = getListFromQuerySnapshot(type, query)
        }
        catch (e: Exception) {
            errorHandler(e)
        }
        return data
    }

    suspend fun getStats(): EnergyStats? {
        var data : EnergyStats? = null
        try {
            val result = firestoreDB.collection(FIRESTORE_COLLECTION_STATS).document("stats").get().await()
            data = result.toObject<EnergyStats>()
        } catch (e: Exception) {
            errorHandler(e)
        }
        return data
    }

    private fun getDBCollection(type: EnergyType) = when(type){
        EnergyType.ALL -> FIRESTORE_COLLECTION_PRODUCTION_PV
        EnergyType.DAILY -> FIRESTORE_COLLECTION_ENERGY_DAY
        EnergyType.MONTHLY -> FIRESTORE_COLLECTION_ENERGY_MONTH
        EnergyType.ANNUALLY -> FIRESTORE_COLLECTION_ENERGY_YEAR
    }

    private fun getListFromQuerySnapshot(type: EnergyType, query: QuerySnapshot) = when(type) {
            EnergyType.ALL -> {
                val list : List<PVProduction> = query.convertToObjects()
                list
            }
            EnergyType.DAILY -> {
                val list : List<EnergyDay> = query.convertToObjects()
                list            }
            EnergyType.MONTHLY -> {
                val list : List<EnergyMonth> = query.convertToObjects()
                list
            }
            EnergyType.ANNUALLY -> {
                val list : List<EnergyYear> = query.convertToObjects()
                list
            }
        }

    private inline fun <reified T> QuerySnapshot.convertToObjects(): List<T> {
        val list = mutableListOf<T>()
        for (document in this) {
            val item = document.toObject<T>()
            item?.let { list.add(it) }
        }
        return list
    }

    private fun errorHandler(response: Exception)  {
        when(response){
            is FirebaseFirestoreException -> {
                Log.d("TAG error ",  response.message.toString())
            }
            is UnknownHostException -> { Log.d("TAG error", "Unknown host!")}
            is ConnectException -> { Log.d("TAG error", "No internet!")}
            else ->  { Log.d("TAG error","Unknown exception!")}
        }
    }
}
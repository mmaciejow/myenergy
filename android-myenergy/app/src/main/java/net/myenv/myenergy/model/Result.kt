package net.myenv.myenergy.model


sealed class Result {
    data class Success(val data : Any) : Result()
    data class Failure(val exception : String) : Result()
    object Loading : Result()
}
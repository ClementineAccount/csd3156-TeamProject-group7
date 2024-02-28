package com.csd3156.team7.Weather

import retrofit2.Retrofit
import retrofit2.Response
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query


private const val API_KEY = "00f3d1ce95a642eca5a153517242702" // WeatherAPI key

private const val latitude = 1.3521
private const val longitude = 103.8198

// gets weather data from the api
interface WeatherService {

    // @GET("onecall?lat=$latitude&lon=$longitude&appid=$API_KEY")

    // As reference
    // https://api.weatherapi.com/v1/current.json?key=00f3d1ce95a642eca5a153517242702&q=Singapore
    @GET("current.json?key=$API_KEY&aqi=no&q=Singapore")
    suspend fun getWeather(@Query("q") q: String = "q",
                           @Query("lat") lat: Double = latitude,
                           @Query("lon") lon: Double = longitude,
                           @Query("last_updated") last_updated: String = "last_updated",
                           @Query("condition/text") condition: String = "condition/text",
    ) : Response<WeatherResponse>
}

object WeatherServiceClient {
    private const val BASE_URL = "https://api.openweathermap.org/data/3.0/"
    private const val BASE_URL2 = "https://api.weatherapi.com/v1/"

    fun create() : WeatherService {
        return Retrofit.Builder()
            .baseUrl(BASE_URL2)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(WeatherService::class.java)
    }
}
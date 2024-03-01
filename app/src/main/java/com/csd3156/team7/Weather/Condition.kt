package com.csd3156.team7.Weather

import com.google.gson.annotations.SerializedName

data class Condition(
    @SerializedName("text")
    val text: String
)
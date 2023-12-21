package com.example.mycurrencyconverter.services.data

import com.google.gson.annotations.SerializedName

data class CLog(
    @SerializedName("id")
    val id:Int,
    @SerializedName("curr_from")
    val curr_from:String,
    @SerializedName("curr_to")
    val curr_to:String,
    @SerializedName("quantity")
    val quantity:Double,
    @SerializedName("result")
    val result:Double
)
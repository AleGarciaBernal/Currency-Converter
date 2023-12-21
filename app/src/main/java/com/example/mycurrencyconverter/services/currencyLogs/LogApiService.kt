package com.example.mycurrencyconverter.services.currencyLogs

import com.example.mycurrencyconverter.services.data.CLog
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface LogApiService {

    @GET("last10")
    suspend fun getLastLogs(): ArrayList<CLog>
    @POST("post_log")
    @JvmSuppressWildcards
    fun postRequest(@Body body: Map<String, Any>): Call<ResponseBody>
}
package com.example.mycurrencyconverter.services.currencyLogs
import androidx.lifecycle.LifecycleCoroutineScope
import com.example.mycurrencyconverter.services.data.CLog
import kotlinx.coroutines.launch
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import kotlin.math.log

class CurrencyLogService {
   private val baseUrl = "https://node-mysql-railway-production-5085.up.railway.app/"
   fun getLogs(lifecycleScope:LifecycleCoroutineScope):ArrayList<CLog> {
      val baseUrl = "https://node-mysql-railway-production-5085.up.railway.app/"

      val retrofit = Retrofit.Builder()
         .baseUrl(baseUrl)
         .addConverterFactory(GsonConverterFactory.create())
         .build()
      val service = retrofit.create(LogApiService::class.java)
      val logs = arrayListOf<CLog>()
      lifecycleScope.launch {
         val response=service.getLastLogs()
         response.forEach {
            logs.add(it)
            if(logs.isNotEmpty())
            println(it)
         }
      }
      return logs
   }
   fun logData(curr_from:String,curr_to:String,quantity:Double,result:Double){
      val retrofit = Retrofit.Builder()
         .baseUrl(baseUrl)
         .addConverterFactory(GsonConverterFactory.create())
         .build()
      val service = retrofit.create(LogApiService::class.java)
      val body = mapOf(
         "curr_from" to curr_from,
         "curr_to" to curr_to,
         "quantity" to quantity,
         "result" to result
      )
      service.postRequest(body).enqueue(object : Callback<ResponseBody> {
         override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
            // handle the response
         }

         override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
            // handle the failure
         }
      })
   }







}
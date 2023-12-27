package com.example.mycurrencyconverter

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import com.android.volley.Request
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.Volley
import org.json.JSONException

class HistoryActivity : AppCompatActivity() {

    private lateinit var listaTV:TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)

        listaTV=findViewById(R.id.listaTV)
        obtenerDatosRates()
    }
    private fun obtenerDatosRates() {
        val url = "https://node-mysql-railway-production-5085.up.railway.app/last10"

        val request = JsonArrayRequest(
            Request.Method.GET,
            url,
            null,
            { response ->
                try {
                    // Procesar la respuesta JSON y construir una cadena para mostrar en el TextView
                    val stringBuilder = StringBuilder()

                    // Construir la cadena con nombres de columna
                    stringBuilder.append(String.format("%-15s %-15s %-15s %-15s", "From", "To", "Quantity", "Result"))
                    stringBuilder.append("\n")
                    stringBuilder.append("------------------------------------------------------------")
                    stringBuilder.append("\n")

                    for (i in 0 until response.length()) {
                        val logObject = response.getJSONObject(i)
                        val currFrom = logObject.getString("curr_from")
                        val currTo = logObject.getString("curr_to")
                        val quantity = logObject.getDouble("quantity")
                        val result = logObject.getDouble("result")

                        // Construir la cadena con los datos del log
                        stringBuilder.append(String.format("%-15s %-15s %-15.2f %-15.2f", currFrom, currTo, quantity, result))
                        stringBuilder.append("\n")
                    }

                    // Mostrar la cadena en el TextView
                    listaTV.text = stringBuilder.toString()

                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            },
            { error ->
                // Manejar errores si es necesario
                Log.e("MainActivity", "Error al obtener los datos de la API", error)
            })

        // Agregar la solicitud a la cola de solicitudes
        val requestQueue = Volley.newRequestQueue(this)
        requestQueue.add(request)
    }

}
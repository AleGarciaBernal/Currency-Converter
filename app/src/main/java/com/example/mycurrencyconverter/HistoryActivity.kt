package com.example.mycurrencyconverter

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.widget.Button
import android.widget.TableRow
import android.widget.TextView
import android.widget.Toast
import com.android.volley.Request
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.Volley
import org.json.JSONException

class HistoryActivity : AppCompatActivity() {

    private lateinit var t1:TextView
    private lateinit var t2:TextView
    private lateinit var t3:TextView
    private lateinit var t4:TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)

        t1=findViewById(R.id.t1)
        t2=findViewById(R.id.t2)
        t3=findViewById(R.id.t3)
        t4=findViewById(R.id.t4)

        obtenerDatosRates()

        val homeButton = findViewById<Button>(R.id.buttonhome)
        val manualButton = findViewById<Button>(R.id.buttonmanual)
        val historyButton = findViewById<Button>(R.id.buttonhistory)

        historyButton.setOnClickListener {
            Toast.makeText(this, "You are already on History", Toast.LENGTH_SHORT).show()
        }

        homeButton.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        manualButton.setOnClickListener {
            val intent = Intent(this, ManualActivity::class.java)
            startActivity(intent)
        }
    }
    private fun obtenerDatosRates() {
        val url = "https://node-mysql-railway-production-5085.up.railway.app/last10"

        val request = JsonArrayRequest(
            Request.Method.GET,
            url,
            null,
            { response ->
                try {
                    val dataStringBuilder = StringBuilder()

                    for (i in 0 until response.length()) {
                        val logObject = response.getJSONObject(i)
                        val currFrom = logObject.getString("curr_from")
                        val currTo = logObject.getString("curr_to")
                        val quantity = logObject.getDouble("quantity")
                        val result = logObject.getDouble("result")

                        // Concatenar los datos al StringBuilder con un formato
                        dataStringBuilder.append(String.format("%-15s%-15s%-15.2f%-15.2f", currFrom, currTo, quantity, result))
                        dataStringBuilder.append("\n")
                    }

                    // Mostrar la cadena en los TextViews
                    val dataLines = dataStringBuilder.toString().trim().split("\n")
                    val t1Text = dataLines.joinToString("\n") { it.substring(0, 15) }
                    val t2Text = dataLines.joinToString("\n") { it.substring(15, 30) }
                    val t3Text = dataLines.joinToString("\n") { it.substring(30, 45) }
                    val t4Text = dataLines.joinToString("\n") { it.substring(45) }

                    t1.text = t1Text
                    t2.text = t2Text
                    t3.text = t3Text
                    t4.text = t4Text
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            },
            { error ->
                // Manejar errores si es necesario
                Log.e("HistoryActivity", "Error al obtener los datos de la API", error)
            })

        // Agregar la solicitud a la cola de solicitudes
        val requestQueue = Volley.newRequestQueue(this)
        requestQueue.add(request)
    }

}
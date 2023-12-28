package com.example.mycurrencyconverter

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import android.view.View
import android.widget.*
import androidx.lifecycle.lifecycleScope
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.mycurrencyconverter.services.currencyLogs.CurrencyLogService
import com.google.android.material.button.MaterialButton
import org.json.JSONException
import org.mozilla.javascript.Context
import org.mozilla.javascript.Scriptable
//
class MainActivity : AppCompatActivity(), View.OnClickListener, AdapterView.OnItemSelectedListener {
    private lateinit var resultTv: TextView
    private lateinit var solutionTv: TextView
    private lateinit var sourceTv:TextView
    private lateinit var targetTV:TextView
    private lateinit var logService:CurrencyLogService
    private var queue: RequestQueue? = null
    private val tiposDeCambio = mutableMapOf<String, Double>()
    private var monedaDestinoActual: String = "USD"
    private var tipoCambioSource: Double = 1.0
    private var tipoCambioTarget: Double = 1.0
    private var numeroIngresado = false

    private var monedaOrigenTemporal: String = "USD"
    private var monedaDestinoTemporal: String = "USD"
    private var cantidadTemporal: Double = 0.0
    private var resultadoTemporal: Double = 0.0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        logService= CurrencyLogService()
        resultTv = findViewById(R.id.result_tv)
        solutionTv = findViewById(R.id.solution_tv)
        sourceTv=findViewById(R.id.sourceTv)
        targetTV=findViewById(R.id.targetTV)
        val buttonIds = arrayOf(
            //R.id.button_c,
            R.id.button_open_bracket, R.id.button_close_bracket,
            R.id.button_divide, R.id.button_multiply, R.id.button_plus, R.id.button_minus,
            R.id.button_equals, R.id.button_0, R.id.button_1, R.id.button_2, R.id.button_3,
            R.id.button_4, R.id.button_5, R.id.button_6, R.id.button_7, R.id.button_8, R.id.button_9,
            R.id.button_ac, R.id.button_dot
        )
        buttonIds.forEach { id ->
            findViewById<MaterialButton>(id).setOnClickListener(this)
        }

        val homeButton = findViewById<Button>(R.id.buttonhome)
        val manualButton = findViewById<Button>(R.id.buttonmanual)
        val historyButton = findViewById<Button>(R.id.buttonhistory)

        historyButton.setOnClickListener {
            // Iniciar la actividad HistoryActivity al hacer clic en el botón "History"
            val intent = Intent(this, HistoryActivity::class.java)
            startActivity(intent)
        }

        homeButton.setOnClickListener {
            // Mostrar un Toast cuando se presiona el botón Home
            Toast.makeText(this, "You are already on Home", Toast.LENGTH_SHORT).show()
        }

        manualButton.setOnClickListener {
            // Ir a la actividad ManualActivity cuando se presiona el botón Manual
            val intent = Intent(this, ManualActivity::class.java)
            startActivity(intent)
        }


        obtenerDatosRates()


    }
    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        val selectedItem = parent?.getItemAtPosition(position).toString()
        if (parent?.id == R.id.spinner_target_currency) {
            monedaDestinoActual = selectedItem
            monedaDestinoTemporal = selectedItem
            val tipoCambio = tiposDeCambio[selectedItem]
            targetTV.text = tipoCambio?.toString() ?: ""
            tipoCambioTarget = tipoCambio ?: 1.0
        } else if (parent?.id == R.id.spinner_source_currency) {
            monedaOrigenTemporal = selectedItem
            monedaDestinoActual = "USD"  // Restaurar el destino a USD al cambiar la moneda de origen
            val tipoCambio = tiposDeCambio[selectedItem]
            tipoCambioSource = tipoCambio ?: 1.0
        }
    }


    override fun onNothingSelected(parent: AdapterView<*>?) {
        // Handle case where nothing is selected
    }

    private fun obtenerDatosRates() {
        val url = "https://v6.exchangerate-api.com/v6/e4fe3f82f3082b7484c4f5df/latest/USD"
        val request = JsonObjectRequest(
            Request.Method.GET,
            url,
            null,
            { response ->
                try {
                    // Obtener el objeto conversion_rates
                    val conversionRates = response.getJSONObject("conversion_rates")

                    // Obtener las claves (nombres de las monedas) del objeto conversion_rates
                    val monedas = listOf("USD", "CLP", "ARS", "BOB", "PEN")

                    // Crear una lista para almacenar los nombres de las monedas
                    val listaMonedas = mutableListOf<String>()

                    // Recorrer las claves y agregarlas a la lista
                    monedas.forEach { moneda ->
                        listaMonedas.add(moneda)
                        // Agregar el tipo de cambio al mapa
                        tiposDeCambio[moneda] = conversionRates.getDouble(moneda)
                    }

                    // Llenar el Spinner con la lista de nombres de monedas
                    llenarSpinner(listaMonedas)
                    llenarSpinner2(listaMonedas)

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

    private fun llenarSpinner(listaMonedas: List<String>) {
        // Crear un adaptador para el Spinner
        val arrayAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, listaMonedas)

        // Establecer el diseño del elemento desplegable del Spinner
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        // Obtener el Spinner y establecer el adaptador
        val spinner = findViewById<Spinner>(R.id.spinner_source_currency)
        spinner.adapter = arrayAdapter

        // Establecer el escuchador de eventos para el Spinner
        spinner.onItemSelectedListener = this
    }

    private fun llenarSpinner2(listaMonedas: List<String>) {
        // Crear un adaptador para el Spinner
        val arrayAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, listaMonedas)

        // Establecer el diseño del elemento desplegable del Spinner
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        // Obtener el Spinner y establecer el adaptador
        val spinner2 = findViewById<Spinner>(R.id.spinner_target_currency)
        spinner2.adapter = arrayAdapter

        // Establecer el escuchador de eventos para el Spinner
        spinner2.onItemSelectedListener = this
    }
    private fun obtenerDatosVolley() {
        val url = "https://v6.exchangerate-api.com/v6/e4fe3f82f3082b7484c4f5df/latest/USD"
        val request = JsonObjectRequest(
            Request.Method.GET,
            url,
            null,
            { response -> solutionTv!!.text = response.toString() }) {
            Log.d(
                "MainActivity",
                "onErrorResponse: ERROR"
            )
        }
        queue!!.add(request)
    }


    override fun onClick(view: View) {
        val button = view as MaterialButton
        val buttonText = button.text.toString()
        var dataToCalculate = solutionTv.text.toString()

        when (buttonText) {
            "AC" -> {
                solutionTv.text = ""
                resultTv.text = "0"
                return

            }
            "=" -> {
                // Realizar operaciones y obtener el resultado
                dataToCalculate = dataToCalculate.replace("×", "*").replace("÷", "/")
                val finalResult = getResult(dataToCalculate)

                if (finalResult != "Err") {
                    resultTv.text = finalResult
                    sourceTv.text = finalResult
                    val tipoCambio = tiposDeCambio[monedaDestinoActual]
                    val resultadoConversion= ((finalResult.toDouble() / tipoCambioSource!!)*tipoCambioTarget)
                    targetTV.text = resultadoConversion.toString()
                    resultadoTemporal = resultadoConversion
                    logService.logData(monedaOrigenTemporal, monedaDestinoTemporal, cantidadTemporal, resultadoTemporal)
                    val toastMessage =
                            "C: $cantidadTemporal\n" +
                            "R: $resultadoTemporal"
                    //Toast.makeText(this, toastMessage, Toast.LENGTH_LONG).show()

                }

                return
            }
            "C" -> dataToCalculate = dataToCalculate.dropLast(1)
            else -> {
                // Si es la primera vez que se ingresa un número, borra el "0"
                if (!numeroIngresado && buttonText.matches(Regex("\\d"))) {
                    cantidadTemporal = buttonText.toDouble()
                    dataToCalculate = buttonText
                    numeroIngresado = true
                } else {
                    dataToCalculate += buttonText
                    cantidadTemporal = dataToCalculate.toDoubleOrNull() ?: 0.0
                }
            }
        }

        solutionTv.text = dataToCalculate

        val finalResult = getResult(dataToCalculate)

        if (finalResult != "Err") {
            resultTv.text = finalResult

        }
    }

    private fun getResult(data: String): String {
        return try {
            val context = Context.enter()
            context.optimizationLevel = -1
            val scriptable: Scriptable = context.initStandardObjects()
            var finalResult =
                context.evaluateString(scriptable, data, "Javascript", 1, null).toString()
            if (finalResult.endsWith(".0")) {
                finalResult = finalResult.replace(".0", "")
            }
            finalResult
        } catch (e: Exception) {
            "Err"
        }
    }



}
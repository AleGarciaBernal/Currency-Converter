package com.example.mycurrencyconverter

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.*
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.android.material.button.MaterialButton
import org.json.JSONException
import org.mozilla.javascript.Context
import org.mozilla.javascript.Scriptable
import android.widget.TextView;
import android.widget.Button

import android.widget.Toast;
import com.example.mycurrencyconverter.services.currencyLogs.CurrencyLogService
import kotlin.math.log

class ManualActivity : AppCompatActivity(), View.OnClickListener, AdapterView.OnItemSelectedListener {
    private lateinit var resultTv: TextView
    private lateinit var solutionTv: TextView
    private lateinit var sourceTv:TextView
    private lateinit var targetTV:TextView
    private lateinit var editRate:EditText
    private lateinit var buttonSave:Button
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

    private lateinit var logService: CurrencyLogService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manual)
        resultTv = findViewById(R.id.result_tv)
        solutionTv = findViewById(R.id.solution_tv)
        sourceTv=findViewById(R.id.sourceTv)
        targetTV=findViewById(R.id.targetTV)
        editRate=findViewById(R.id.editRate)
        buttonSave=findViewById(R.id.buttonSave)
        logService= CurrencyLogService()

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

        val manualButton = findViewById<Button>(R.id.buttonmanual)
        val homeButton = findViewById<Button>(R.id.buttonhome)
        val historyButton = findViewById<Button>(R.id.buttonhistory)

        historyButton.setOnClickListener {
            // Iniciar la actividad HistoryActivity al hacer clic en el botón "History"
            val intent = Intent(this, HistoryActivity::class.java)
            startActivity(intent)
        }


        manualButton.setOnClickListener {
            // Mostrar un Toast cuando se presiona el botón Manual en la actividad ManualActivity
            Toast.makeText(this, "You are already on Manual", Toast.LENGTH_SHORT).show()
        }

        homeButton.setOnClickListener {
            // Navegar de nuevo a la MainActivity cuando se presiona el botón Home en la actividad ManualActivity
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        //val spinner1 = findViewById<Spinner>(R.id.spinner_source_currency)
        //spinner1.adapter = arrayAdapter1
        //spinner1.onItemSelectedListener = this
        buttonSave.setOnClickListener(this)
        obtenerDatosRates()

    }
    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        val selectedItem = parent?.getItemAtPosition(position).toString()

        if (parent?.id == R.id.target_spinner) {
            // Se seleccionó un elemento en el segundo Spinner (moneda de destino)
            monedaDestinoActual = selectedItem
            monedaDestinoTemporal=selectedItem
            val tipoCambio = tiposDeCambio[selectedItem]

            tipoCambioTarget = tipoCambio ?: 1.0
        } else if (parent?.id == R.id.spinner_source_currency) {
            monedaOrigenTemporal=selectedItem

            val tipoCambio = tiposDeCambio[selectedItem]
            tipoCambioSource = tipoCambio ?: 1.0

        }
    }

    fun editarTargetTv(view: View) {
        val nuevoRate = editRate.text.toString()

        try {
            // Intenta convertir la entrada a Double
            val rateValue = nuevoRate.toDouble()

            // Verifica si el valor es un número válido
            if (!rateValue.isNaN() && !rateValue.isInfinite()) {
                // Actualiza el texto en targetTV solo si es un número válido
                targetTV.text = rateValue.toString()
            } else {
                // Muestra un Toast si la entrada no es un número válido
                showToast("Invalid number entered.")
            }
        } catch (e: NumberFormatException) {
            // Captura la excepción si la conversión a Double falla
            // Muestra un Toast informando que la entrada no es un número válido
            showToast("Invalid number entered.")
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
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
                    val monedas = conversionRates.keys()

                    // Crear una lista para almacenar los nombres de las monedas
                    val listaMonedas = mutableListOf<String>()

                    // Recorrer las claves y agregarlas a la lista
                    while (monedas.hasNext()) {
                        val moneda = monedas.next()
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
        val spinner2 = findViewById<Spinner>(R.id.target_spinner)
        spinner2.adapter = arrayAdapter

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
    fun fetchData() {
        val url = "https://jsonplaceholder.typicode.com/todos/1"
        val jsonObjectRequest = JsonObjectRequest(Request.Method.GET, url, null,
            { response ->
                try {
                    val userId = response.getInt("userId")
                    val id = response.getInt("id")
                    val title = response.getString("title")
                    val completed = response.getBoolean("completed")
                    resultTv.text = "$userId\n$id\n$title\n$completed"
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            },
            { error ->
                resultTv.text = "Error"
            })

        val requestQueue = Volley.newRequestQueue(resultTv.context)
        requestQueue.add(jsonObjectRequest)
    }


    override fun onClick(view: View) {
        when (view.id) {
            R.id.buttonSave -> {
                editarTargetTv(view)
            }

            else -> {
                // Lógica para otros clics en botones
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
                        cantidadTemporal=solutionTv.text.toString().toDouble()
                        resultadoTemporal=resultTv.text.toString().toDouble()
                        logService.logData(monedaOrigenTemporal,monedaDestinoTemporal,cantidadTemporal,resultadoTemporal)
                        dataToCalculate = dataToCalculate.replace("×", "*").replace("÷", "/")
                        val finalResult = getResult(dataToCalculate)

                        if (finalResult != "Err") {
                            // Actualizar los TextView según la moneda de destino actual
                            resultTv.text = (finalResult.toDouble() * targetTV.text.toString().toDouble()).toString()
                        }
                        Toast.makeText(this, "Saved successfully", Toast.LENGTH_SHORT).show()

                        return
                    }
                    "C" -> dataToCalculate = dataToCalculate.dropLast(1)
                    else -> {
                        // Si es la primera vez que se ingresa un número, borra el "0"
                        if (!numeroIngresado && buttonText.matches(Regex("\\d"))) {
                            dataToCalculate = buttonText
                            numeroIngresado = true
                        } else {
                            dataToCalculate += buttonText
                        }
                    }
                }

                solutionTv.text = dataToCalculate

                val finalResult = getResult(dataToCalculate)

                if (finalResult != "Err") {
                    // Actualizar los TextView según la moneda de destino actual
                    resultTv.text = (finalResult.toDouble() * targetTV.text.toString().toDouble()).toString()
                }
            }
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
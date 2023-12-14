package com.example.mycurrencyconverter

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.view.View
import android.widget.TextView
import com.example.mycurrencyconverter.databinding.ActivityMainBinding
import com.google.android.material.button.MaterialButton
import org.mozilla.javascript.Context
import org.mozilla.javascript.Scriptable
//
class MainActivity : AppCompatActivity(), View.OnClickListener {
    private lateinit var resultTv: TextView
    private lateinit var solutionTv: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        resultTv = findViewById(R.id.result_tv)
        solutionTv = findViewById(R.id.solution_tv)

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
                solutionTv.text = resultTv.text
                return
            }
            "C" -> dataToCalculate = dataToCalculate.dropLast(1)
            else -> dataToCalculate += buttonText
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
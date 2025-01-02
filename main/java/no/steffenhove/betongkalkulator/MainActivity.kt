package no.steffenhove.betongkalkulator

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.preference.PreferenceManager
import no.steffenhove.betongkalkulator.ui.FirkantInput
import no.steffenhove.betongkalkulator.ui.KjerneInput
import no.steffenhove.betongkalkulator.ui.TrekantInput
import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.PI
import kotlin.math.sqrt
import no.steffenhove.betongkalkulator.Constants.DENSITY_CONCRETE
import no.steffenhove.betongkalkulator.Constants.DENSITY_LIGHT_CONCRETE
import no.steffenhove.betongkalkulator.*

// Enums for enheter (Unit)
enum class Unit(val display: String, val toMeters: Double) {
    MILLIMETER("mm", 0.001),
    CENTIMETER("cm", 0.01),
    METER("m", 1.0),
    INCH("inch", 0.0254),
    FOOT("ft", 0.3048);
}
data class Dimensions(val value: Double, val unit: Unit)

data class CoreDimensions(val diameter: Dimensions, val height: Dimensions)

data class SquareDimensions(val length: Dimensions, val width: Dimensions, val thickness: Dimensions)

data class TriangleDimensions(val sideA: Dimensions, val sideB: Dimensions, val sideC: Dimensions, val thickness: Dimensions)

class MainActivity : AppCompatActivity() {
    private var resultText by mutableStateOf("")
    private lateinit var prefs: SharedPreferences
    private lateinit var defaultPrefs: SharedPreferences
    private var coreVisible by mutableStateOf(true)
    private var squareVisible by mutableStateOf(false)
    private var triangleVisible by mutableStateOf(false)


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        prefs = getSharedPreferences("settings", Context.MODE_PRIVATE)
        defaultPrefs = PreferenceManager.getDefaultSharedPreferences(this)

        setContent {
            val context = context
            val unitSystem = defaultPrefs.getString("unit_preference", "Metrisk") ?: "Metrisk"
            MainActivityContent(context = context, unitSystem = unitSystem)
        }
    }

    @Composable
    fun MainActivityContent(context: Context, unitSystem: String) {
        var diameterState by remember { mutableStateOf("") } // Riktig: String
        var heightState by remember { mutableStateOf("") } // Riktig: String
        var lengthState by remember { mutableStateOf("") }
        var widthState by remember { mutableStateOf("") }
        var thicknessState by remember { mutableStateOf("") }
        var aState by remember { mutableStateOf("") }
        var bState by remember { mutableStateOf("") }
        var cState by remember { mutableStateOf("") }
        var selectedDiameterUnit by remember { mutableStateOf(Unit.METER) }
        var selectedHeightUnit by remember { mutableStateOf(Unit.METER) }
        var selectedLengthUnit by remember { mutableStateOf(Unit.METER) }
        var selectedWidthUnit by remember { mutableStateOf(Unit.METER) }
        var selectedThicknessUnit by remember { mutableStateOf(Unit.METER) }
        var selectedAUnit by remember { mutableStateOf(Unit.METER) }
        var selectedBUnit by remember { mutableStateOf(Unit.METER) }
        var selectedCUnit by remember { mutableStateOf(Unit.METER) }
        var selectedThicknessTriangleUnit by remember { mutableStateOf(Unit.METER) }


        Column(Modifier.padding(16.dp)) {
            when {
                coreVisible -> KjerneInput(onCalculateClick = { diameter, height, diameterUnit, heightUnit ->
                    val diameterDouble = diameter.toDoubleOrNull() ?: 0.0 // Riktig plassering
                    val heightDouble = height.toDoubleOrNull() ?: 0.0 // Riktig plassering
                    val coreDimensions = CoreDimensions(Dimensions(diameterDouble, diameterUnit), Dimensions(heightDouble, heightUnit))
                    resultText = calculateAndSave(context, "Kjerne", kjerneDimensjoner = coreDimensions)
                })
                squareVisible -> FirkantInput(onCalculateClick = { length, width, thickness, lengthUnit, widthUnit, thicknessUnit ->
                    val lengthDouble = length.toDoubleOrNull() ?: 0.0
                    val widthDouble = width.toDoubleOrNull() ?: 0.0
                    val thicknessDouble = thickness.toDoubleOrNull() ?: 0.0
                    val squareDimensions = SquareDimensions(Dimensions(lengthDouble, lengthUnit), Dimensions(widthDouble, widthUnit), Dimensions(thicknessDouble, thicknessUnit))
                    resultText = calculateAndSave(context, "Firkant", firkantDimensjoner = squareDimensions)
                })
                triangleVisible -> TrekantInput(onCalculateClick = { a, b, c, thickness, sideAUnit, sideBUnit, sideCUnit, thicknessTriangleUnit ->
                    val aDouble = a.toDoubleOrNull() ?: 0.0
                    val bDouble = b.toDoubleOrNull() ?: 0.0
                    val cDouble = c.toDoubleOrNull() ?: 0.0
                    val thicknessDouble = thickness.toDoubleOrNull() ?: 0.0
                    val triangleDimensions = TriangleDimensions(Dimensions(aDouble, sideAUnit), Dimensions(bDouble, sideBUnit), Dimensions(cDouble, sideCUnit), Dimensions(thicknessDouble, thicknessTriangleUnit))
                    resultText = calculateAndSave(context, "Trekant", trekantDimensjoner = triangleDimensions)
                })
            }

            Spacer(Modifier.height(16.dp))
            Text(text = resultText)
        }
    }
    fun calculateAndSave(
        context: Context,
        shape: String,
        coreDimensions: CoreDimensions? = null,
        squareDimensions: SquareDimensions? = null,
        triangleDimensions: TriangleDimensions? = null
    ): String {
        var volume = 0.0
        var weight = 0.0
        var dimensions = ""

        when (shape) {
            "Kjerne" -> {
                val diameterMeters = convertToMeters(coreDimensions?.diameter?.value ?: 0.0, coreDimensions?.diameter?.unit ?: Unit.METER)
                val heightMeters = convertToMeters(coreDimensions?.height?.value ?: 0.0, coreDimensions?.height?.unit ?: Unit.METER)
                volume = calculateCylinderVolume(diameterMeters, heightMeters)
                // ...
            }
            "Firkant" -> {
                val lengthMeters = convertToMeters(squareDimensions?.length?.value ?: 0.0, squareDimensions?.length?.unit ?: Unit.METER)
                val widthMeters = convertToMeters(squareDimensions?.width?.value ?: 0.0, squareDimensions?.width?.unit ?: Unit.METER)
                val thicknessMeters = convertToMeters(squareDimensions?.thickness?.value ?: 0.0, squareDimensions?.thickness?.unit ?: Unit.METER)
                volume = calculateCuboidVolume(lengthMeters, widthMeters, thicknessMeters)
                // ...
            }
            "Trekant" -> {
                val aMeters = convertToMeters(triangleDimensions?.sideA?.value ?: 0.0, triangleDimensions?.sideA?.unit ?: Unit.METER)
                val bMeters = convertToMeters(triangleDimensions?.sideB?.value ?: 0.0, triangleDimensions?.sideB?.unit ?: Unit.METER)
                val cMeters = convertToMeters(triangleDimensions?.sideC?.value ?: 0.0, triangleDimensions?.sideC?.unit ?: Unit.METER)
                val thicknessMeters = convertToMeters(triangleDimensions?.thickness?.value ?: 0.0, triangleDimensions?.thickness?.unit ?: Unit.METER)
                volume = calculateTriangleVolume(aMeters, bMeters, cMeters, thicknessMeters)
                // ...
            }
            else -> dimensions = "Ukjent form."
        }

        weight = calculateWeight(volume) // Flyttet hit
        val calculation = "$shape: Volum: ${String.format(Locale.ROOT, "%.2f", volume)}, Vekt: ${String.format(Locale.ROOT, "%.0f", weight)}, $dimensions"
        saveCalculationToHistory(context, calculation)
        return calculation // Return calculation string
    }
    saveCalculationToHistory(context, calculation)
}
private fun convertToMeters(value: Double, unit: Unit): Double {
    val unitSystem = defaultPrefs.getString("unit_preference", "Metrisk") ?: "Metrisk"

    return if (unitSystem == "Imperial") {
        when (unit) {
            Unit.INCH -> value * Unit.INCH.toMeters
            Unit.FOOT -> value * Unit.FOOT.toMeters
            else -> {
                Log.e("convertToMeters", "Ugyldig enhet for Imperial: ${unit.display}. Returnerer 0.0")
                0.0
            }
        }
    } else { // Metrisk system
        value * unit.toMeters // Forenklet: Alle metriske enheter konverteres
        /*
        //Alternativt, for enda tydeligere feilhåndtering (selv om det er litt overflødig her):
        when (unit) {
            Unit.MILLIMETER, Unit.CENTIMETER, Unit.METER -> value * unit.toMeters
            else -> {
                Log.e("convertToMeters", "Ugyldig enhet for Metrisk: ${unit.display}. Returnerer 0.0")
                0.0
            }
        }
        */
    }
}
private fun calculateCylinderVolume(diameter: Double, height: Double): Double {
    val radius = diameter / 2
    return PI * radius * radius * height
}

private fun calculateCuboidVolume(length: Double, width: Double, thickness: Double): Double { // Renamed
    return length * width * thickness
}

private fun calculateTriangleVolume(sideA: Double, sideB: Double, sideC: Double, thickness: Double): Double {
    val s = (sideA + sideB + sideC) / 2
    return try {
        val area = sqrt(s * (s - sideA) * (s - sideB) * (s - sideC))
        area * thickness
    } catch (e: IllegalArgumentException) {
        Toast.makeText(this, "Ugyldig trekant", Toast.LENGTH_SHORT).show()
        0.0
    }
}

private fun calculateWeight(volume: Double): Double {
    val density = when (prefs.getString("density_preference", "Betong")) {
        "Lettbetong" -> DENSITY_LIGHT_CONCRETE
        "Egendefinert" -> {
            val customDensityString = prefs.getString("custom_density", DENSITY_CONCRETE.toString())
            customDensityString?.toDoubleOrNull()?.takeIf { it > 0 } ?: run {
                Toast.makeText(context, "Ugyldig tetthet for Egendefinert. Bruker standardverdi (2400 kg/m³)", Toast.LENGTH_SHORT).show()
                DENSITY_CONCRETE
            }
        }
        else -> DENSITY_CONCRETE
    }
    return volume * density
}

        private fun saveCalculationToHistory(context: Context, calculation: String) {
            val sharedPrefs = context.getSharedPreferences("history", Context.MODE_PRIVATE)
            val editor = sharedPrefs.edit()

            val currentHistory = sharedPrefs.getString("calculations", "[]") ?: "[]"

            val jsonArray = try {
                JSONArray(currentHistory)
            } catch (e: Exception) {
                JSONArray()
            }

            val newCalculation = JSONObject().apply {
                put("calculation", calculation) // Lagrer hele den formaterte strengen
                val currentDateTime = LocalDateTime.now()
                val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                val formattedDateTime = currentDateTime.format(formatter)
                put("datetime", formattedDateTime)
            }

            jsonArray.put(newCalculation)

            while (jsonArray.length() > 20) {
                jsonArray.remove(0)
            }

            editor.putString("calculations", jsonArray.toString())
            editor.apply()

            Log.d("History", "Saved calculation: $newCalculation")
        }
    }

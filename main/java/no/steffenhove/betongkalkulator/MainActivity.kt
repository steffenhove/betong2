package no.steffenhove.betongkalkulator

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.preference.PreferenceManager
import no.steffenhove.betongkalkulator.ui.*

class MainActivity : AppCompatActivity() {
    private lateinit var prefs: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        prefs = PreferenceManager.getDefaultSharedPreferences(this)

        setContent {
            MainActivityContent(context = this, prefs = prefs)
        }
    }
}

@Composable
fun MainActivityContent(context: Context, prefs: SharedPreferences) {
    val unitSystem = prefs.getString("unit_system", "metric") ?: "metric"
    val weightUnit = prefs.getString("weight_unit", "default") ?: "default"

    var selectedShape by remember { mutableStateOf("Kjerne") }
    var resultText by remember { mutableStateOf("") }

    Column(Modifier.padding(16.dp)) {
        DropdownMenu(
            expanded = true,
            onDismissRequest = { /*TODO*/ }
        ) {
            DropdownMenuItem(onClick = { selectedShape = "Kjerne" }) {
                Text("Kjerne")
            }
            DropdownMenuItem(onClick = { selectedShape = "Firkant" }) {
                Text("Firkant")
            }
            DropdownMenuItem(onClick = { selectedShape = "Trekant" }) {
                Text("Trekant")
            }
        }

        when (selectedShape) {
            "Kjerne" -> KjerneInput { diameter, height, diameterUnit, heightUnit ->
                resultText = calculateAndSave(
                    context,
                    "Kjerne",
                    coreDimensions = CoreDimensions(
                        Dimensions(diameter.toDouble(), diameterUnit),
                        Dimensions(height.toDouble(), heightUnit)
                    ),
                    unitSystem = unitSystem,
                    weightUnit = weightUnit
                )
            }
            "Firkant" -> FirkantInput { length, width, thickness, lengthUnit, widthUnit, thicknessUnit ->
                resultText = calculateAndSave(
                    context,
                    "Firkant",
                    squareDimensions = SquareDimensions(
                        Dimensions(length.toDouble(), lengthUnit),
                        Dimensions(width.toDouble(), widthUnit),
                        Dimensions(thickness.toDouble(), thicknessUnit)
                    ),
                    unitSystem = unitSystem,
                    weightUnit = weightUnit
                )
            }
            "Trekant" -> TrekantInput { a, b, c, thickness, aUnit, bUnit, cUnit, thicknessUnit ->
                resultText = calculateAndSave(
                    context,
                    "Trekant",
                    triangleDimensions = TriangleDimensions(
                        Dimensions(a.toDouble(), aUnit),
                        Dimensions(b.toDouble(), bUnit),
                        Dimensions(c.toDouble(), cUnit),
                        Dimensions(thickness.toDouble(), thicknessUnit)
                    ),
                    unitSystem = unitSystem,
                    weightUnit = weightUnit
                )
            }
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
    triangleDimensions: TriangleDimensions? = null,
    unitSystem: String,
    weightUnit: String
): String {
    var volume = 0.0
    var weight = 0.0

    when (shape) {
        "Kjerne" -> {
            val diameterMeters = convertToMeters(coreDimensions?.diameter?.value ?: 0.0, coreDimensions?.diameter?.unit ?: Unit.METER)
            val heightMeters = convertToMeters(coreDimensions?.height?.value ?: 0.0, coreDimensions?.height?.unit ?: Unit.METER)
            volume = calculateCylinderVolume(diameterMeters, heightMeters)
        }
        "Firkant" -> {
            val lengthMeters = convertToMeters(squareDimensions?.length?.value ?: 0.0, squareDimensions?.length?.unit ?: Unit.METER)
            val widthMeters = convertToMeters(squareDimensions?.width?.value ?: 0.0, squareDimensions?.width?.unit ?: Unit.METER)
            val thicknessMeters = convertToMeters(squareDimensions?.thickness?.value ?: 0.0, squareDimensions?.thickness?.unit ?: Unit.METER)
            volume = calculateCuboidVolume(lengthMeters, widthMeters, thicknessMeters)
        }
        "Trekant" -> {
            val aMeters = convertToMeters(triangleDimensions?.sideA?.value ?: 0.0, triangleDimensions?.sideA?.unit ?: Unit.METER)
            val bMeters = convertToMeters(triangleDimensions?.sideB?.value ?: 0.0, triangleDimensions?.sideB?.unit ?: Unit.METER)
            val cMeters = convertToMeters(triangleDimensions?.sideC?.value ?: 0.0, triangleDimensions?.sideC?.unit ?: Unit.METER)
            val thicknessMeters = convertToMeters(triangleDimensions?.thickness?.value ?: 0.0, triangleDimensions?.thickness?.unit ?: Unit.METER)
            volume = calculateTriangleVolume(aMeters, bMeters, cMeters, thicknessMeters)
        }
    }

    weight = calculateWeight(volume, unitSystem, weightUnit)
    val calculation = "$shape: Volum: ${String.format(Locale.ROOT, "%.2f", volume)}, Vekt: ${String.format(Locale.ROOT, "%.0f", weight)}"
    saveCalculationToHistory(context, calculation)
    return calculation
}

private fun calculateWeight(volume: Double, unitSystem: String, weightUnit: String): Double {
    val density = DENSITY_CONCRETE // Sett riktig tetthet basert på valg
    var weight = volume * density

    if (unitSystem == "imperial" && weightUnit == "default" || weightUnit == "lbs") {
        weight *= 2.20462 // Konverter kg til lbs
    }

    return weight
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
        put("calculation", calculation)
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
}

private fun clearHistory(context: Context) {
    val sharedPrefs = context.getSharedPreferences("history", Context.MODE_PRIVATE)
    sharedPrefs.edit().remove("calculations").apply()
}

private fun deleteSelectedEntries(context: Context, entriesToDelete: List<Int>) {
    val sharedPrefs = context.getSharedPreferences("history", Context.MODE_PRIVATE)
    val currentHistory = sharedPrefs.getString("calculations", "[]") ?: "[]"
    val jsonArray = JSONArray(currentHistory)

    val newJsonArray = JSONArray()
    for (i in 0 until jsonArray.length()) {
        if (!entriesToDelete.contains(i)) {
            newJsonArray.put(jsonArray.get(i))
        }
    }

    sharedPrefs.edit().putString("calculations", newJsonArray.toString()).apply()
}

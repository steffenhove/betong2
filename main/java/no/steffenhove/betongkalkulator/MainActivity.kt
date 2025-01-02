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
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

enum class Unit(val display: String) {
    METER("Meter"),
    CENTIMETER("Centimeter"),
    MILLIMETER("Millimeter")
}

data class Dimensions(val value: Double, val unit: Unit)

data class KjerneInput(val diameter: Dimensions, val height: Dimensions)

data class SquareDimensions(val length: Dimensions, val width: Dimensions, val thickness: Dimensions)

data class TriangleDimensions(val a: Dimensions, val b: Dimensions, val c: Dimensions, val thickness: Dimensions)

fun convertToMeters(value: Double, unit: Unit): Double {
    return when (unit) {
        Unit.METER -> value
        Unit.CENTIMETER -> value / 100
        Unit.MILLIMETER -> value / 1000
    }
}

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
                    coreDimensions = KjerneInput(
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
    coreDimensions: KjerneInput? = null,
    squareDimensions: SquareDimensions? = null,
    triangleDimensions: TriangleDimensions? = null,
    unitSystem: String,
    weightUnit: String
): String {
    val volume: Double
    val weight: Double

    when (shape) {
        "Kjerne" -> {
            if (coreDimensions != null) {
                val diameterInMeters = convertToMeters(coreDimensions.diameter.value, coreDimensions.diameter.unit)
                val heightInMeters = convertToMeters(coreDimensions.height.value, coreDimensions.height.unit)
                volume = calculateCylinderVolume(diameterInMeters, heightInMeters)
                weight = volume * DENSITY_CONCRETE // Assuming a constant density for concrete
            } else {
                return "Feil: Ingen dimensjoner gitt for kjernen."
            }
        }
        "Firkant" -> {
            if (squareDimensions != null) {
                val lengthInMeters = convertToMeters(squareDimensions.length.value, squareDimensions.length.unit)
                val widthInMeters = convertToMeters(squareDimensions.width.value, squareDimensions.width.unit)
                val thicknessInMeters = convertToMeters(squareDimensions.thickness.value, squareDimensions.thickness.unit)
                volume = calculateCuboidVolume(lengthInMeters, widthInMeters, thicknessInMeters)
                weight = volume * DENSITY_CONCRETE
            } else {
                return "Feil: Ingen dimensjoner gitt for firkanten."
            }
        }
        "Trekant" -> {
            if (triangleDimensions != null) {
                val aInMeters = convertToMeters(triangleDimensions.a.value, triangleDimensions.a.unit)
                val bInMeters = convertToMeters(triangleDimensions.b.value, triangleDimensions.b.unit)
                val cInMeters = convertToMeters(triangleDimensions.c.value, triangleDimensions.c.unit)
                val thicknessInMeters = convertToMeters(triangleDimensions.thickness.value, triangleDimensions.thickness.unit)
                volume = calculateTriangleVolume(aInMeters, bInMeters, cInMeters, thicknessInMeters)
                weight = volume * DENSITY_CONCRETE
            } else {
                return "Feil: Ingen dimensjoner gitt for trekanten."
            }
        }
        else -> {
            return "Ugyldig form valgt."
        }
    }

    // Lagre resultatet (volum og vekt) sammen med tidsstempel
    val currentDateTime = LocalDateTime.now()
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    val formattedDateTime = currentDateTime.format(formatter)

    val result = "Form: $shape, Volum: ${"%.2f".format(volume)} m³, Vekt: ${"%.2f".format(weight)} kg, Tid: $formattedDateTime"
    saveResultToPreferences(context, result)

    return result
}

fun saveResultToPreferences(context: Context, result: String) {
    val prefs = context.getSharedPreferences("calculations", Context.MODE_PRIVATE)
    val editor = prefs.edit()
    val existingResults = prefs.getStringSet("results", mutableSetOf()) ?: mutableSetOf()
    existingResults.add(result)
    editor.putStringSet("results", existingResults)
    editor.apply()
}

const val DENSITY_CONCRETE = 2400.0 // kg/m³, assuming a constant density for concrete

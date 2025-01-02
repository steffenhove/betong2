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

// Definisjon av enhetstyper
enum class Unit(val display: String) {
    METER("Meter"),
    CENTIMETER("Centimeter"),
    MILLIMETER("Millimeter")
}

// Dataklasser for dimensjoner og former
data class Dimensions(val value: Double, val unit: Unit)

data class KjerneInput(val diameter: Dimensions, val height: Dimensions)

data class SquareDimensions(val length: Dimensions, val width: Dimensions, val thickness: Dimensions)

data class TriangleDimensions(val a: Dimensions, val b: Dimensions, val c: Dimensions, val thickness: Dimensions)

// Konverteringsfunksjon for enheter til meter
fun convertToMeters(value: Double, unit: Unit): Double {
    return when (unit) {
        Unit.METER -> value
        Unit.CENTIMETER -> value / 100
        Unit.MILLIMETER -> value / 1000
    }
}

// Volumberegninger for forskjellige former
fun calculateCylinderVolume(diameter: Double, height: Double): Double {
    val radius = diameter / 2
    return Math.PI * radius * radius * height
}

fun calculateCuboidVolume(length: Double, width: Double, thickness: Double): Double {
    return length * width * thickness
}

fun calculateTriangleVolume(a: Double, b: Double, c: Double, thickness: Double): Double {
    val s = (a + b + c) / 2
    val area = Math.sqrt(s * (s - a) * (s - b) * (s - c))
    return area * thickness
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

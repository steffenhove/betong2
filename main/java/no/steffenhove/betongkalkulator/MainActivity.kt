package no.steffenhove.betongkalkulator

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.preference.PreferenceManager

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val prefs = PreferenceManager.getDefaultSharedPreferences(this)

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
                    coreDimensions = Dimensions(diameter.toDouble(), diameterUnit),
                    heightDimensions = Dimensions(height.toDouble(), heightUnit),
                    unitSystem = unitSystem,
                    weightUnit = weightUnit
                )
            }
            "Firkant" -> FirkantInput { length, width, thickness, lengthUnit, widthUnit, thicknessUnit ->
                resultText = calculateAndSave(
                    context,
                    "Firkant",
                    lengthDimensions = Dimensions(length.toDouble(), lengthUnit),
                    widthDimensions = Dimensions(width.toDouble(), widthUnit),
                    thicknessDimensions = Dimensions(thickness.toDouble(), thicknessUnit),
                    unitSystem = unitSystem,
                    weightUnit = weightUnit
                )
            }
            "Trekant" -> TrekantInput { a, b, c, thickness, aUnit, bUnit, cUnit, thicknessUnit ->
                resultText = calculateAndSave(
                    context,
                    "Trekant",
                    sideADimensions = Dimensions(a.toDouble(), aUnit),
                    sideBDimensions = Dimensions(b.toDouble(), bUnit),
                    sideCDimensions = Dimensions(c.toDouble(), cUnit),
                    thicknessDimensions = Dimensions(thickness.toDouble(), thicknessUnit),
                    unitSystem = unitSystem,
                    weightUnit = weightUnit
                )
            }
        }

        Spacer(Modifier.height(16.dp))
        Text(text = resultText)
    }
}

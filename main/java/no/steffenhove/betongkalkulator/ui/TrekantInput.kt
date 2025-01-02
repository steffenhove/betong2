package no.steffenhove.betongkalkulator.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import no.steffenhove.betongkalkulator.UnitDropdown

@Composable
fun TrekantInput(onCalculateClick: (String, String, String, String, Unit, Unit, Unit, Unit) -> Unit) {
    var a by remember { mutableStateOf("") }
    var b by remember { mutableStateOf("") }
    var c by remember { mutableStateOf("") }
    var thickness by remember { mutableStateOf("") }
    var selectedAUnit by remember { mutableStateOf(Unit.METER) }
    var selectedBUnit by remember { mutableStateOf(Unit.METER) }
    var selectedCUnit by remember { mutableStateOf(Unit.METER) }
    var selectedThicknessUnit by remember { mutableStateOf(Unit.METER) }

    Column(Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        OutlinedTextField(
            value = a,
            onValueChange = { a = it },
            label = { Text("Side A") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = b,
            onValueChange = { b = it },
            label = { Text("Side B") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = c,
            onValueChange = { c = it },
            label = { Text("Side C") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = thickness,
            onValueChange = { thickness = it },
            label = { Text("Tykkelse") },
            modifier = Modifier.fillMaxWidth()
        )
        Row(horizontalArrangement = Arrangement.SpaceEvenly, modifier = Modifier.fillMaxWidth()){
            UnitDropdown(selectedUnit = selectedAUnit){ selectedAUnit = it }
            UnitDropdown(selectedUnit = selectedBUnit){ selectedBUnit = it }
            UnitDropdown(selectedUnit = selectedCUnit){ selectedCUnit = it }
            UnitDropdown(selectedUnit = selectedThicknessUnit){ selectedThicknessUnit = it }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = {
            onCalculateClick(a, b, c, thickness, selectedAUnit, selectedBUnit, selectedCUnit, selectedThicknessUnit)
        }, modifier = Modifier.fillMaxWidth()) {
            Text("Beregn")
        }
    }
}
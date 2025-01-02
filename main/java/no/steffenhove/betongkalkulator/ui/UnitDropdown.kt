package no.steffenhove.betongkalkulator.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Alignment
import no.steffenhove.betongkalkulator.Unit

@Composable
fun UnitDropdown(selectedUnit: Unit, onUnitSelected: (Unit) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    val units = Unit.values().toList()
    Box(modifier = Modifier.fillMaxWidth().wrapContentSize(Alignment.TopStart)) {
        TextButton(onClick = { expanded = true }) {
            Text(selectedUnit.display)

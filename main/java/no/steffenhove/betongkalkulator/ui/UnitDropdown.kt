package no.steffenhove.betongkalkulator.ui

import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.tooling.preview.Preview

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun UnitDropdown(selectedUnit: Unit, onUnitSelected: (Unit) -> Unit) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        TextField(
            readOnly = true,
            value = selectedUnit.display,
            onValueChange = {},
            label = { Text("Enhet") },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(
                    expanded = expanded
                )
            },
            colors = ExposedDropdownMenuDefaults.textFieldColors()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            Unit.values().forEach { selectionOption ->
                DropdownMenuItem(onClick = {
                    onUnitSelected(selectionOption)
                    expanded = false
                }) {
                    Text(text = selectionOption.display)
                }
            }
        }
    }
}

@Preview
@Composable
fun UnitDropdownPreview(){
    var selectedUnit by remember { mutableStateOf(Unit.METER) }
    UnitDropdown(selectedUnit = selectedUnit){
        selectedUnit = it
    }
}
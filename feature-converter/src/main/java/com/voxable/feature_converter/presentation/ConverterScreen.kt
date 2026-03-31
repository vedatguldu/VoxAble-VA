package com.voxable.feature_converter.presentation

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.voxable.core_ui.components.VoxAbleTextField
import com.voxable.core_ui.components.VoxAbleTopBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConverterScreen(
    onBack: () -> Unit,
    viewModel: ConverterViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            VoxAbleTopBar(
                title = "Birim Çevirici",
                onBack = onBack
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Kategori seçimi (chip row)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ConversionCategory.entries.forEach { category ->
                    FilterChip(
                        selected = state.selectedCategory == category,
                        onClick = { viewModel.onCategoryChanged(category) },
                        label = { Text(category.label) },
                        modifier = Modifier.semantics {
                            contentDescription = "${category.label} kategorisi" +
                                if (state.selectedCategory == category) ", seçili" else ""
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Giriş değeri
            VoxAbleTextField(
                value = state.inputValue,
                onValueChange = { viewModel.onInputChanged(it) },
                label = "Değer",
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Kaynak birim
            UnitDropdown(
                label = "Kaynak Birim",
                selected = state.fromUnit,
                units = state.availableUnits,
                onSelected = { viewModel.onFromUnitChanged(it) },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Yer değiştir
            IconButton(
                onClick = { viewModel.onSwapUnits() },
                modifier = Modifier
                    .size(56.dp)
                    .semantics { contentDescription = "Birimleri yer değiştir" }
            ) {
                Icon(
                    imageVector = Icons.Default.SwapVert,
                    contentDescription = null,
                    modifier = Modifier.size(32.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Hedef birim
            UnitDropdown(
                label = "Hedef Birim",
                selected = state.toUnit,
                units = state.availableUnits,
                onSelected = { viewModel.onToUnitChanged(it) },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Sonuç
            if (state.result.isNotEmpty()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .semantics {
                            liveRegion()
                            contentDescription =
                                "${state.inputValue} ${state.fromUnit} = ${state.result} ${state.toUnit}"
                        },
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Sonuç",
                            style = MaterialTheme.typography.titleSmall,
                            modifier = Modifier.semantics { heading() }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "${state.inputValue} ${state.fromUnit} = ${state.result} ${state.toUnit}",
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }

            // Hata
            state.error?.let { error ->
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.semantics { contentDescription = "Hata: $error" }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun UnitDropdown(
    label: String,
    selected: String,
    units: List<String>,
    onSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = selected,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor()
                .semantics { contentDescription = "$label: $selected" }
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            units.forEach { unit ->
                DropdownMenuItem(
                    text = { Text(unit) },
                    onClick = {
                        onSelected(unit)
                        expanded = false
                    },
                    modifier = Modifier.semantics {
                        contentDescription = "$unit birimi"
                    }
                )
            }
        }
    }
}

package com.voxable.feature_converter.presentation

import com.voxable.core.base.UiEvent
import com.voxable.core.base.UiState

data class ConverterState(
    val inputValue: String = "1",
    val result: String = "",
    val selectedCategory: ConversionCategory = ConversionCategory.LENGTH,
    val fromUnit: String = "Metre",
    val toUnit: String = "Kilometre",
    val availableUnits: List<String> = ConversionCategory.LENGTH.units,
    val error: String? = null
) : UiState

sealed interface ConverterEvent : UiEvent {
    data class ShowError(val message: String) : ConverterEvent
}

enum class ConversionCategory(val label: String, val units: List<String>) {
    LENGTH(
        label = "Uzunluk",
        units = listOf("Milimetre", "Santimetre", "Metre", "Kilometre", "İnç", "Feet", "Mil")
    ),
    WEIGHT(
        label = "Ağırlık",
        units = listOf("Miligram", "Gram", "Kilogram", "Ton", "Ons", "Pound")
    ),
    TEMPERATURE(
        label = "Sıcaklık",
        units = listOf("Celsius", "Fahrenheit", "Kelvin")
    ),
    AREA(
        label = "Alan",
        units = listOf("Metrekare", "Kilometre Kare", "Hektar", "Dönüm", "Feet Kare")
    ),
    VOLUME(
        label = "Hacim",
        units = listOf("Mililitre", "Litre", "Metreküp", "Galon", "Bardak")
    ),
    SPEED(
        label = "Hız",
        units = listOf("m/s", "km/h", "mph", "Knot")
    )
}

package com.voxable.feature_converter.presentation

import com.voxable.core.base.BaseViewModel
import com.voxable.feature_converter.domain.ConversionEngine
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ConverterViewModel @Inject constructor() :
    BaseViewModel<ConverterState, ConverterEvent>(ConverterState()) {

    init {
        doConvert()
    }

    fun onInputChanged(value: String) {
        if (value.isEmpty() || value.matches(Regex("^-?\\d*\\.?\\d*$"))) {
            updateState { copy(inputValue = value) }
            doConvert()
        }
    }

    fun onCategoryChanged(category: ConversionCategory) {
        updateState {
            copy(
                selectedCategory = category,
                availableUnits = category.units,
                fromUnit = category.units.first(),
                toUnit = category.units.getOrElse(1) { category.units.first() },
                result = ""
            )
        }
        doConvert()
    }

    fun onFromUnitChanged(unit: String) {
        updateState { copy(fromUnit = unit) }
        doConvert()
    }

    fun onToUnitChanged(unit: String) {
        updateState { copy(toUnit = unit) }
        doConvert()
    }

    fun onSwapUnits() {
        updateState {
            copy(
                fromUnit = toUnit,
                toUnit = fromUnit
            )
        }
        doConvert()
    }

    private fun doConvert() {
        val input = currentState.inputValue.toDoubleOrNull() ?: return
        val result = ConversionEngine.convert(
            value = input,
            from = currentState.fromUnit,
            to = currentState.toUnit,
            category = currentState.selectedCategory.label
        )
        if (result != null) {
            updateState {
                copy(
                    result = formatResult(result),
                    error = null
                )
            }
        } else {
            updateState { copy(error = "Dönüşüm yapılamadı") }
            sendEvent(ConverterEvent.ShowError("Dönüşüm yapılamadı"))
        }
    }

    private fun formatResult(value: Double): String {
        return if (value == value.toLong().toDouble()) {
            value.toLong().toString()
        } else {
            "%.6f".format(value).trimEnd('0').trimEnd('.')
        }
    }
}

package com.voxable.core.base

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Tüm ViewModel'ler için temel sınıf.
 * UiState ve UiEvent yönetimini sağlar.
 *
 * @param S UI State tipi
 * @param E UI Event (tek seferlik olaylar) tipi
 */
abstract class BaseViewModel<S : UiState, E : UiEvent>(
    initialState: S
) : ViewModel() {

    private val _uiState = MutableStateFlow(initialState)
    val uiState: StateFlow<S> = _uiState.asStateFlow()

    private val _events = Channel<E>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    protected val currentState: S
        get() = _uiState.value

    protected fun updateState(reducer: S.() -> S) {
        _uiState.update { it.reducer() }
    }

    protected fun sendEvent(event: E) {
        viewModelScope.launch {
            _events.send(event)
        }
    }

    protected fun launch(block: suspend () -> Unit) {
        viewModelScope.launch {
            try {
                block()
            } catch (e: Exception) {
                onError(e)
            }
        }
    }

    protected open fun onError(exception: Exception) {
        // Alt sınıflar override edebilir
    }
}

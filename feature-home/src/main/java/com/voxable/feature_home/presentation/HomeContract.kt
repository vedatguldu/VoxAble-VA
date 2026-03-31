package com.voxable.feature_home.presentation

import com.voxable.core.base.UiEvent
import com.voxable.core.base.UiState

data class HomeState(
    val userName: String = "",
    val isLoading: Boolean = false
) : UiState

sealed class HomeEvent : UiEvent

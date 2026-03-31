package com.voxable.feature_home.presentation

import com.voxable.core.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor() : BaseViewModel<HomeState, HomeEvent>(HomeState()) {

    init {
        loadUserData()
    }

    private fun loadUserData() {
        launch {
            updateState { copy(isLoading = false, userName = "Kullanıcı") }
        }
    }
}

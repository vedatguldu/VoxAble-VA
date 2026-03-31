package com.voxable.feature_reader.presentation

import com.voxable.core.base.BaseViewModel
import com.voxable.core.util.Resource
import com.voxable.feature_reader.domain.repository.ReaderRepository
import com.voxable.feature_reader.domain.usecase.ReadTextUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ReaderViewModel @Inject constructor(
    private val readTextUseCase: ReadTextUseCase,
    private val readerRepository: ReaderRepository
) : BaseViewModel<ReaderState, ReaderEvent>(ReaderState()) {

    fun onTextChange(text: String) {
        updateState { copy(inputText = text) }
    }

    fun onReadClick() {
        if (currentState.isSpeaking) {
            stopReading()
        } else {
            startReading()
        }
    }

    private fun startReading() {
        launch {
            updateState { copy(isSpeaking = true) }
            when (val result = readTextUseCase(currentState.inputText)) {
                is Resource.Success -> { /* TTS başlatıldı */ }
                is Resource.Error -> {
                    updateState { copy(isSpeaking = false) }
                    sendEvent(ReaderEvent.ShowError(result.message))
                }
                is Resource.Loading -> Unit
            }
        }
    }

    private fun stopReading() {
        launch {
            readerRepository.stopSpeech()
            updateState { copy(isSpeaking = false) }
        }
    }
}

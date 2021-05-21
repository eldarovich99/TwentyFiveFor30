package com.veuzbekov.twentyfivefor30.ui

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.veuzbekov.twentyfivefor30.UnlockEventBus
import com.veuzbekov.twentyfivefor30.model.Time
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class MainViewModel(context: Application) : ViewModel() {
    private val time: MutableStateFlow<Time> = MutableStateFlow(Time())
    private val approachesStateFlow: MutableStateFlow<Int> = MutableStateFlow(0)
    private val numbersStateFlow: MutableStateFlow<Int> = MutableStateFlow(0)
    private val timerScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val started: MutableStateFlow<Boolean?> = MutableStateFlow(null)
    //private val sharedPrefs = context.getSharedPreferences("twentyFive", Context.MODE_PRIVATE)

    init {
        UnlockEventBus.unlockFlow.onEach {
            incApproaches()
        }.launchIn(viewModelScope)
    }

    fun onStartButtonClicked() {
        if (started.value == true) {
            reset()
        } else
            startTimer()
    }

    private fun startTimer() {
        timerScope.launch {
            started.value = true
            while (true) {
                delay(1000)
                time.value = time.value.inc()
            }
        }
    }

    fun incApproaches() {
        approachesStateFlow.value = approachesStateFlow.value + 1
    }

    fun incNumbers() {
        numbersStateFlow.value = numbersStateFlow.value + 1
    }

    private fun reset() {
        started.value = false
        timerScope.coroutineContext.cancelChildren()
        time.value = Time()
        approachesStateFlow.value = 0
        numbersStateFlow.value = 0
    }

    override fun onCleared() {
        super.onCleared()
        timerScope.coroutineContext.cancelChildren()
    }

    fun getTime(): StateFlow<Time> = time
    fun getApproaches(): StateFlow<Int> = approachesStateFlow
    fun getNumbers(): StateFlow<Int> = numbersStateFlow
    fun getStartedFlow(): StateFlow<Boolean?> = started
    class Factory(private val context: Application) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return MainViewModel(context) as T
        }
    }
}
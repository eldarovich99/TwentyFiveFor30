package com.veuzbekov.twentyfivefor30.ui

import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import by.kirich1409.viewbindingdelegate.viewBinding
import com.veuzbekov.twentyfivefor30.R
import com.veuzbekov.twentyfivefor30.background.Actions
import com.veuzbekov.twentyfivefor30.background.CounterService
import com.veuzbekov.twentyfivefor30.databinding.ActivityMainBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {
    private lateinit var viewModel: MainViewModel
    private val uiScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private val binding by viewBinding(ActivityMainBinding::bind)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        viewModel = ViewModelProvider(
            this,
            MainViewModel.Factory(application)
        ).get(MainViewModel::class.java)
        setOnClickListeners()
    }

    override fun onStart() {
        super.onStart()
        setupFlowObservers()
    }

    private fun setOnClickListeners() {
        binding.controlButton.setOnClickListener {
            viewModel.onStartButtonClicked()
        }
        binding.helpButton.setOnClickListener {
            val intent = Intent(this, InfoActivity::class.java)
            startActivity(intent)
        }
    }

    private fun setupFlowObservers() {
        viewModel.getTime().onEach { time ->
            binding.timeTextView.text = getString(R.string.time_template, time.min(), time.sec())
        }.launchIn(uiScope)
        viewModel.getApproaches().onEach { approachesNumber ->
            binding.approachesCounter.text = getString(R.string.approaches_count, approachesNumber)
        }.launchIn(uiScope)
        viewModel.getNumbers().onEach { numbersCount ->
            binding.numbersCounter.text = getString(R.string.numbers_count, numbersCount)
        }.launchIn(uiScope)
        viewModel.getStartedFlow().onEach { started ->
            binding.controlButton.text = getString(if (started) R.string.stop else R.string.start)
            actionOnService(if (started) Actions.START else Actions.STOP)
        }.launchIn(uiScope)
    }

    override fun onStop() {
        super.onStop()
        uiScope.coroutineContext.cancelChildren()
    }

    private fun actionOnService(action: Actions) {
        Intent(this, CounterService::class.java).also {
            it.action = action.name
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(it)
                return
            }
            startService(it)
        }
    }
}
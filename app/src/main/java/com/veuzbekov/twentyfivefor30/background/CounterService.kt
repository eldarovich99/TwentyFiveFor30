package com.veuzbekov.twentyfivefor30.background

import android.app.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.*
import android.widget.Toast
import com.veuzbekov.twentyfivefor30.R
import com.veuzbekov.twentyfivefor30.UnlockEventBus
import com.veuzbekov.twentyfivefor30.ui.MainActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*


class CounterService : Service() {
    private var wakeLock: PowerManager.WakeLock? = null
    private var isServiceStarted = false
    private val timer = Timer()
    private fun log(text: String) {

    }

    override fun onBind(intent: Intent): IBinder? {
        log("Some component want to bind with the service")
        // We don't provide binding, so return null
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent != null) {
            val action = intent.action
            log("using an intent with action $action")
            when (action) {
                Actions.START.name -> startService()
                Actions.STOP.name -> stopService()
                else -> log("This should never happen. No action in the received intent")
            }
        } else {
            log(
                "with a null intent. It has been probably restarted by the system."
            )
        }
        // by returning this we make sure the service is restarted if the system kills the service
        return START_STICKY
    }

    override fun onCreate() {
        super.onCreate()
        log("The service has been created".toUpperCase())
        val notification = createProtectionServiceNotification(
            channelId = ENDLESS_SERVICE_ID,
            text = "Твой бро",
            channelName = "Сервис для работы приложения в фоне"
        )
        startForeground(1, notification)
    }

    override fun onDestroy() {
        super.onDestroy()
        log("The service has been destroyed".toUpperCase(Locale.ROOT))
        makeText("Service destroyed")
    }

    private fun startService() {
        if (isServiceStarted) return
        log("Starting the foreground service task")
        makeText("Service starting its task")
        isServiceStarted = true
        registerAll()
        // we need this lock so our service gets not affected by Doze Mode
        wakeLock =
            (getSystemService(Context.POWER_SERVICE) as PowerManager).run {
                newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "EndlessService::lock").apply {
                    acquire()
                }
            }

        // we're starting a loop in a coroutine
        CoroutineScope(Dispatchers.IO).launch {
            while (isServiceStarted) {
                delay(1 * 60 * 1000)
            }
            log("End of the loop for the service")
        }
        launchTimer()
    }

    private fun stopService() {
        log("Stopping the foreground service")
        makeText("Service stopping")
        try {
            timer.cancel()
            wakeLock?.let {
                if (it.isHeld) {
                    it.release()
                }
            }
            stopForeground(true)
            stopSelf()
        } catch (e: Exception) {
            log("Service stopped without being started: ${e.message}")
        }
        unregisterAll()
        isServiceStarted = false
    }

    fun makeText(text: String) {
        if (false) {
            Toast.makeText(baseContext, text, Toast.LENGTH_SHORT).show()
        }
    }

    private fun launchTimer() {
        timer.schedule(object : TimerTask() {
            override fun run() {
                alert()
            }

        }, 30 * 60000)
    }

    private fun alert() {
        val v = getSystemService(VIBRATOR_SERVICE) as Vibrator
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            v.vibrate(VibrationEffect.createOneShot(750, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            //deprecated in API 26
            v.vibrate(750)
        }
    }

    private fun createProtectionServiceNotification(
        channelId: String,
        channelName: String,
        text: String
    ): Notification {

        // depending on the Android API that we're dealing with we will have
        // to use a specific method to create the notification
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager;
            val channel = NotificationChannel(
                channelId,
                channelName,
                NotificationManager.IMPORTANCE_HIGH
            ).let {
                it.description = "Protection service channel"
                it.enableVibration(false)
                it
            }
            notificationManager.createNotificationChannel(channel)
        }

        val pendingIntent: PendingIntent =
            Intent(this, MainActivity::class.java).let { notificationIntent ->
                PendingIntent.getActivity(this, 0, notificationIntent, 0)
            }

        val builder: Notification.Builder =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) Notification.Builder(
                this,
                channelId
            ) else Notification.Builder(this)

        return builder
            .setContentTitle("Твой бро")
            .setContentText(text)
            .setContentIntent(pendingIntent)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setTicker("Твой бро")
            .setPriority(Notification.PRIORITY_HIGH) // for under android 26 compatibility
            .build()
    }

    private val phoneLockStateReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == Intent.ACTION_USER_PRESENT) {
                CoroutineScope(Dispatchers.Main).launch {
                    UnlockEventBus.unlockFlow.emit(Unit)
                }
            }
        }
    }

    private fun registerAll() {
        registerScreenStateReceiver()
    }

    private fun registerScreenStateReceiver() {
        val filter = IntentFilter()
        filter.addAction(Intent.ACTION_USER_PRESENT)
        filter.addAction(Intent.ACTION_SCREEN_OFF)
        registerReceiver(phoneLockStateReceiver, filter)
    }

    private fun unregisterAll() {
        try {
            unregisterReceiver(phoneLockStateReceiver)
        } catch (e: IllegalArgumentException) {
            // ok
        }
    }

    companion object {
        const val ENDLESS_SERVICE_ID = "endless service"
    }

    /*inner class CounterBinder : Binder() {
        val service: CounterService
            get() = this@CounterService
    }*/
}
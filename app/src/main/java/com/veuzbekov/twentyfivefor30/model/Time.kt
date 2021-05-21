package com.veuzbekov.twentyfivefor30.model

class Time(private val minutes: Int = 0, private val seconds: Int = 0) {
    fun inc(): Time {
        val newSeconds: Int
        val newMinutes: Int
        if (seconds < 60) {
            newSeconds = seconds + 1
            newMinutes = minutes
        } else {
            newMinutes = minutes + 1
            newSeconds = 0
        }
        return Time(minutes = newMinutes, seconds = newSeconds)
    }

    fun min() = minutes
    fun sec() = seconds
}
package com.veuzbekov.twentyfivefor30.model

class Time(private val minutes: Int = 0, private val seconds: Int = 0) {
    fun inc(): Time {
        val newSeconds: Int
        val newMinutes: Int
        if (seconds < 59) {
            newSeconds = seconds + 1
            newMinutes = minutes
        } else {
            newMinutes = minutes + 1
            newSeconds = 0
        }
        return Time(minutes = newMinutes, seconds = newSeconds)
    }

    fun min(): String{
        return if (minutes < 10)
            "0${minutes}"
        else
            minutes.toString()
    }

    fun sec(): String{
        return if (seconds < 10)
            "0${seconds}"
        else
            seconds.toString()
    }
}
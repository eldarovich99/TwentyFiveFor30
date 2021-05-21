package com.veuzbekov.twentyfivefor30

import kotlinx.coroutines.flow.MutableSharedFlow

object UnlockEventBus {
    val unlockFlow = MutableSharedFlow<Unit>()
}
package com.zuhdiarifazizi.projectakhirpapb.domain.repository

import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

interface NfcTagReader {
    val isSupported: Boolean
    val isEnabled: StateFlow<Boolean>
    val tagScanned: SharedFlow<String>

    fun refreshAvailability()
    fun simulateTagScan(uid: String)
}

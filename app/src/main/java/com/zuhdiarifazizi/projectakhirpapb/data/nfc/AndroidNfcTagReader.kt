package com.zuhdiarifazizi.projectakhirpapb.data.nfc

import android.app.Activity
import android.content.Context
import android.nfc.NfcAdapter
import android.nfc.Tag
import com.zuhdiarifazizi.projectakhirpapb.domain.nfc.NfcTagReader
import com.zuhdiarifazizi.projectakhirpapb.domain.security.SecurityInputValidator
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Reads 13.56 MHz RFID/NFC tag UIDs in read-only foreground reader mode
 * (safe for e-money, KTM, e-KTP, and NTAG213/215 clothing tags).
 */
class AndroidNfcTagReader(context: Context) : NfcTagReader, NfcAdapter.ReaderCallback {

    private val adapter: NfcAdapter? = NfcAdapter.getDefaultAdapter(context.applicationContext)

    override val isSupported: Boolean
        get() = adapter != null

    private val _isEnabled = MutableStateFlow(adapter?.isEnabled == true)
    override val isEnabled: StateFlow<Boolean> = _isEnabled.asStateFlow()

    private val _tagScanned = MutableSharedFlow<String>(
        replay = 0,
        extraBufferCapacity = 4,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    override val tagScanned: SharedFlow<String> = _tagScanned.asSharedFlow()

    override fun refreshAvailability() {
        _isEnabled.value = adapter?.isEnabled == true
    }

    fun enableReader(activity: Activity) {
        refreshAvailability()
        val nfc = adapter ?: return
        runCatching {
            nfc.enableReaderMode(
                activity,
                this,
                NfcAdapter.FLAG_READER_NFC_A or
                    NfcAdapter.FLAG_READER_NFC_B or
                    NfcAdapter.FLAG_READER_NFC_F or
                    NfcAdapter.FLAG_READER_NFC_V or
                    NfcAdapter.FLAG_READER_SKIP_NDEF_CHECK,
                null
            )
        }
    }

    fun disableReader(activity: Activity) {
        val nfc = adapter ?: return
        runCatching { nfc.disableReaderMode(activity) }
    }

    override fun onTagDiscovered(tag: Tag?) {
        val idBytes = tag?.id ?: return
        val normalizedUid = SecurityInputValidator.normalizeAndValidateNfcUid(idBytes) ?: return
        _tagScanned.tryEmit(normalizedUid)
    }

    override fun simulateTagScan(uid: String) {
        val normalized = SecurityInputValidator.normalizeNfcUidString(uid) ?: return
        _tagScanned.tryEmit(normalized)
    }
}

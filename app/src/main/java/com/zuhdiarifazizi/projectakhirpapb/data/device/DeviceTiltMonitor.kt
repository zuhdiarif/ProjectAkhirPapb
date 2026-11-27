package com.zuhdiarifazizi.projectakhirpapb.data.device

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import com.zuhdiarifazizi.projectakhirpapb.domain.model.TiltState
import kotlin.math.abs
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Monitors device accelerometer to ensure the user holds the phone roughly parallel/level
 * above the garment laid on a flat surface (Langkah 4/4).
 */
class DeviceTiltMonitor(context: Context) {

    private val sensorManager = context.applicationContext.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
    private val accelerometer: Sensor? = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

    private val _tiltState = MutableStateFlow(
        TiltState(isSensorAvailable = accelerometer != null, isLevel = true)
    )
    val tiltState: StateFlow<TiltState> = _tiltState.asStateFlow()

    private val listener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent) {
            if (event.values.size < 3) return
            val x = event.values[0]
            val y = event.values[1]
            val z = event.values[2]

            val isLevel = abs(z) >= MIN_FLAT_Z && abs(x) <= MAX_TILT_AXIS && abs(y) <= MAX_TILT_AXIS
            val hint = when {
                isLevel -> ""
                x > MAX_TILT_AXIS -> "Miringkan ponsel sedikit ke kiri agar sejajar."
                x < -MAX_TILT_AXIS -> "Miringkan ponsel sedikit ke kanan agar sejajar."
                y > MAX_TILT_AXIS -> "Condongkan bagian atas ponsel sedikit ke depan."
                y < -MAX_TILT_AXIS -> "Tarik bagian atas ponsel sedikit ke arah Anda."
                else -> "Pegang ponsel sejajar datar tepat di atas pakaian."
            }
            _tiltState.value = TiltState(
                isSensorAvailable = true,
                isLevel = isLevel,
                guidanceHint = hint
            )
        }

        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
    }

    fun start() {
        val sensor = accelerometer ?: return
        sensorManager?.registerListener(listener, sensor, SensorManager.SENSOR_DELAY_UI)
    }

    fun stop() {
        if (accelerometer != null) {
            sensorManager?.unregisterListener(listener)
        }
    }

    private companion object {
        const val MIN_FLAT_Z = 3.5f
        const val MAX_TILT_AXIS = 8.2f
    }
}

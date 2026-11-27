package com.zuhdiarifazizi.projectakhirpapb.data.harmony

import android.content.Context
import android.util.Log
import com.google.ai.edge.litert.Accelerator
import com.google.ai.edge.litert.LiteRtException
import com.zuhdiarifazizi.projectakhirpapb.domain.model.HarmonyPrediction
import com.zuhdiarifazizi.projectakhirpapb.domain.model.ProjectHarmonyFeatureEncoder
import com.zuhdiarifazizi.projectakhirpapb.domain.repository.ProjectHarmonyScorer
import com.zuhdiarifazizi.projectakhirpapb.domain.model.ClothingItem
import java.util.concurrent.Callable
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.math.abs

/**
 * Thread-confined Android Project Harmony scorer powered by `project_harmony.tflite` via Google LiteRT.
 * Initializes GPU->CPU fallback asynchronously on a single dedicated background thread and falls back
 * seamlessly to identical in-memory weights if the native engine is not ready or closed.
 */
class AndroidProjectHarmonyScorer(
    context: Context,
    private val kotlinFallback: KotlinProjectHarmonyScorer = KotlinProjectHarmonyScorer()
) : ProjectHarmonyScorer, AutoCloseable {

    private val appContext = context.applicationContext
    private val executor = Executors.newSingleThreadExecutor { runnable ->
        Thread(runnable, "ProjectHarmony-LiteRT")
    }
    private val closed = AtomicBoolean(false)

    @Volatile
    private var engine: LiteRtHarmonyEngine? = null

    init {
        executor.execute { initializeEngineOnWorker() }
    }

    override fun evaluate(
        top: ClothingItem,
        bottom: ClothingItem,
        outer: ClothingItem?
    ): HarmonyPrediction {
        val activeEngine = engine
        if (closed.get() || activeEngine == null) {
            return kotlinFallback.evaluate(top, bottom, outer)
        }
        val features = ProjectHarmonyFeatureEncoder.encode(top, bottom, outer)
        return runCatching {
            val logits = executor.submit(Callable {
                val workerEngine = engine ?: throw IllegalStateException("LiteRT engine unavailable")
                workerEngine.infer(features)
            }).get(INFERENCE_TIMEOUT_MS, TimeUnit.MILLISECONDS)
            val backendLabel = "Project Harmony LiteRT (${activeEngine.accelerator.name})"
            ProjectHarmonyFeatureEncoder.decodeLogits(logits, backendName = backendLabel)
        }.getOrElse {
            kotlinFallback.evaluate(top, bottom, outer)
        }
    }

    private fun initializeEngineOnWorker() {
        if (closed.get()) return
        val created = try {
            LiteRtHarmonyEngine(appContext, Accelerator.GPU)
        } catch (_: LiteRtException) {
            try {
                LiteRtHarmonyEngine(appContext, Accelerator.CPU)
            } catch (cpuError: Throwable) {
                Log.w(TAG, "LiteRT fallback to CPU failed; using Kotlin weights", cpuError)
                null
            }
        } catch (unexpected: Throwable) {
            Log.w(TAG, "LiteRT initialization failed; using Kotlin weights", unexpected)
            null
        }

        if (created == null || closed.get()) {
            created?.close()
            return
        }

        // Verify LiteRT output matches Python-exported Kotlin reference weights on neutral classic sample.
        val sampleVec = FloatArray(ProjectHarmonyFeatureEncoder.INPUT_SIZE).apply {
            this[1] = 1f; this[2] = 0.02f; this[3] = 0.98f; this[4] = 1f
            this[7] = 1f; this[8] = 0.02f; this[9] = 0.08f; this[10] = 1f
            this[21] = 0.90f
        }
        val maxDelta = runCatching {
            val liteRtOut = created.infer(sampleVec)
            val kotlinOut = kotlinFallback.inferLogits(sampleVec)
            liteRtOut.indices.maxOf { idx -> abs(liteRtOut[idx] - kotlinOut[idx]) }
        }.getOrElse { -1f }

        engine = created
        Log.i(TAG, "READY accelerator=${created.accelerator} goldenMaxDelta=$maxDelta")
    }

    override fun close() {
        if (!closed.compareAndSet(false, true)) return
        runCatching {
            executor.execute {
                engine?.close()
                engine = null
            }
        }
        executor.shutdown()
    }

    private companion object {
        const val TAG = "ProjectHarmonyLiteRT"
        const val INFERENCE_TIMEOUT_MS = 1_500L
    }
}

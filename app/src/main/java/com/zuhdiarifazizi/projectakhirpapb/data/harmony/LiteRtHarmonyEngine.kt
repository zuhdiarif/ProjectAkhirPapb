package com.zuhdiarifazizi.projectakhirpapb.data.harmony

import android.content.Context
import com.google.ai.edge.litert.Accelerator
import com.google.ai.edge.litert.CompiledModel
import com.google.ai.edge.litert.Environment
import com.google.ai.edge.litert.TensorBuffer
import com.zuhdiarifazizi.projectakhirpapb.domain.model.ProjectHarmonyFeatureEncoder
import java.io.File

private const val HARMONY_MODEL_ASSET = "project_harmony.tflite"
private const val HARMONY_MODEL_VERSION = "v1"
private val sharedEnvironment by lazy { Environment.create() }

/**
 * Confined single-thread wrapper around LiteRT `CompiledModel` with buffer reuse and warm-up.
 * Creation, inference, and `close()` must always execute on the caller's single-thread executor.
 */
class LiteRtHarmonyEngine(
    context: Context,
    val accelerator: Accelerator
) : AutoCloseable {

    private val stagedModelFile = stageModelAsset(context)
    private val model = CompiledModel.create(
        stagedModelFile.path,
        buildOptions(context, accelerator),
        sharedEnvironment
    )
    private val inputs = mutableListOf<TensorBuffer>()
    private val outputs = mutableListOf<TensorBuffer>()

    init {
        try {
            inputs += model.createInputBuffers()
            outputs += model.createOutputBuffers()
            infer(FloatArray(ProjectHarmonyFeatureEncoder.INPUT_SIZE))
        } catch (e: Exception) {
            close()
            throw e
        }
    }

    fun infer(inputFeatures: FloatArray): FloatArray {
        require(inputFeatures.size == ProjectHarmonyFeatureEncoder.INPUT_SIZE) {
            "Expected ${ProjectHarmonyFeatureEncoder.INPUT_SIZE} floats, got ${inputFeatures.size}"
        }
        inputs[0].writeFloat(inputFeatures)
        model.run(inputs, outputs)
        return outputs[0].readFloat()
    }

    override fun close() {
        (inputs + outputs).forEach { buffer -> runCatching { buffer.close() } }
        inputs.clear()
        outputs.clear()
        runCatching { model.close() }
    }

    private companion object {
        fun stageModelAsset(context: Context): File =
            File(context.noBackupFilesDir, "$HARMONY_MODEL_VERSION-$HARMONY_MODEL_ASSET").also { file ->
                if (!file.isFile) {
                    context.noBackupFilesDir.listFiles()?.forEach { existing ->
                        if (existing.name.endsWith("-$HARMONY_MODEL_ASSET")) {
                            existing.delete()
                        }
                    }
                    val tmp = File("${file.path}.tmp")
                    context.assets.open(HARMONY_MODEL_ASSET).use { input ->
                        tmp.outputStream().use { output -> input.copyTo(output) }
                    }
                    check(tmp.renameTo(file)) { "Failed to stage $HARMONY_MODEL_ASSET" }
                }
            }

        fun buildOptions(context: Context, accelerator: Accelerator): CompiledModel.Options =
            CompiledModel.Options(accelerator).apply {
                if (accelerator == Accelerator.CPU) {
                    val cache = File(
                        context.codeCacheDir,
                        "$HARMONY_MODEL_ASSET-$HARMONY_MODEL_VERSION.xnnpack_cache"
                    )
                    context.codeCacheDir.listFiles()?.forEach { existing ->
                        if (existing.name.startsWith("$HARMONY_MODEL_ASSET-") &&
                            existing.name.endsWith(".xnnpack_cache") &&
                            existing != cache
                        ) {
                            existing.delete()
                        }
                    }
                    cpuOptions = CompiledModel.CpuOptions(
                        numThreads = 2,
                        xnnPackFlags = 0x23,
                        xnnPackWeightCachePath = cache.path
                    )
                }
            }
    }
}

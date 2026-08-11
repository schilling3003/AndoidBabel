package com.schilling3003.relay.storage

import android.content.Context
import android.net.Uri
import android.os.StatFs
import com.schilling3003.relay.domain.ImportResult
import com.schilling3003.relay.domain.ModelError
import com.schilling3003.relay.domain.ModelState
import com.schilling3003.relay.domain.ValidationResult
import com.schilling3003.relay.engines.ModelManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import java.security.MessageDigest

/**
 * Production model manager that copies the user-supplied `.litertlm` file into
 * private app storage, validates it atomically, and never deletes the original.
 */
class LocalModelManager(
    private val context: Context,
    private val requiredFreeBytes: Long = 7_000_000_000L
) : ModelManager {

    private val modelDir = File(context.filesDir, "models").apply { mkdirs() }
    private val modelFile = File(modelDir, MODEL_FILENAME)

    private val _state = MutableStateFlow<ModelState>(resolveInitialState())
    override val state: StateFlow<ModelState> = _state

    init {
        if (modelFile.exists()) {
            _state.value = ModelState.Ready(
                path = modelFile.absolutePath,
                modelId = inferModelId(modelFile),
                sizeBytes = modelFile.length()
            )
        }
    }

    override suspend fun import(uri: Uri): ImportResult = withContext(Dispatchers.IO) {
        try {
            val resolver = context.contentResolver
            val type = resolver.getType(uri)
            val size = resolver.openFileDescriptor(uri, "r")?.statSize ?: -1L
            if (size == -1L) {
                return@withContext ImportResult.Failure(ModelError.CopyFailed("Cannot read source file"))
            }
            if (size > freeBytes()) {
                return@withContext ImportResult.Failure(ModelError.Storage())
            }

            _state.value = ModelState.Importing(bytesCopied = 0, totalBytes = size, stage = "Copying model…")

            val temp = File(modelDir, "${MODEL_FILENAME}.tmp")
            resolver.openInputStream(uri)?.use { input ->
                temp.outputStream().use { output ->
                    val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                    var copied = 0L
                    var read: Int
                    while (input.read(buffer).also { read = it } != -1) {
                        output.write(buffer, 0, read)
                        copied += read
                        if (copied % (50 * 1024 * 1024) == 0L) {
                            _state.value = ModelState.Importing(
                                bytesCopied = copied.coerceAtMost(size),
                                totalBytes = size,
                                stage = "Copying model…"
                            )
                        }
                    }
                }
            } ?: return@withContext ImportResult.Failure(ModelError.CopyFailed("Could not open source file"))

            _state.value = ModelState.Validating()
            val validation = validateFile(temp)
            if (validation is ValidationResult.Invalid) {
                temp.delete()
                return@withContext ImportResult.Failure(validation.error)
            }

            if (!temp.renameTo(modelFile)) {
                temp.delete()
                return@withContext ImportResult.Failure(ModelError.CopyFailed())
            }

            _state.value = ModelState.Ready(
                path = modelFile.absolutePath,
                modelId = inferModelId(modelFile),
                sizeBytes = modelFile.length()
            )
            ImportResult.Success(_state.value as ModelState.Ready)
        } catch (e: IOException) {
            ImportResult.Failure(ModelError.CopyFailed(e.localizedMessage ?: "Import failed"))
        }
    }

    override suspend fun validate(): ValidationResult = withContext(Dispatchers.IO) {
        validateFile(modelFile)
    }

    override suspend fun remove() = withContext(Dispatchers.IO) {
        modelFile.delete()
        _state.value = ModelState.Missing
    }

    private fun resolveInitialState(): ModelState =
        if (modelFile.exists() && modelFile.length() > 0) {
            ModelState.Ready(
                path = modelFile.absolutePath,
                modelId = inferModelId(modelFile),
                sizeBytes = modelFile.length()
            )
        } else ModelState.Missing

    private fun validateFile(file: File): ValidationResult {
        if (!file.exists() || file.length() == 0L) {
            return ValidationResult.Invalid(ModelError.Corrupt())
        }
        if (!file.name.endsWith(EXTENSION, ignoreCase = true)) {
            return ValidationResult.Invalid(ModelError.Incompatible("File must end with $EXTENSION"))
        }
        return ValidationResult.Valid
    }

    private fun freeBytes(): Long {
        val stat = StatFs(context.filesDir.path)
        return stat.availableBytes
    }

    private fun inferModelId(file: File): String? =
        file.nameWithoutExtension.takeIf { it.isNotBlank() }

    companion object {
        const val EXTENSION = ".litertlm"
        const val MODEL_FILENAME = "gemma.litertlm"
    }
}

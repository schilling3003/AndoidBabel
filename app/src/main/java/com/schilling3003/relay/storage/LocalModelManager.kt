package com.schilling3003.relay.storage

import android.content.Context
import android.net.Uri
import android.os.StatFs
import android.provider.OpenableColumns
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
        if (modelFile.exists() && _state.value is ModelState.Missing) {
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
            val displayName = resolver.queryDisplayName(uri)
            if (displayName != null && !displayName.endsWith(EXTENSION, ignoreCase = true)) {
                return@withContext fail(ModelError.Incompatible("File must end with $EXTENSION"))
            }

            val size = resolver.openFileDescriptor(uri, "r")?.statSize ?: -1L
            if (size == -1L) {
                return@withContext fail(ModelError.CopyFailed("Cannot read source file"))
            }
            if (size > freeBytes()) {
                return@withContext fail(ModelError.Storage())
            }

            _state.value = ModelState.Importing(bytesCopied = 0, totalBytes = size, stage = "Copying model…")

            val temp = File(modelDir, TEMP_FILENAME)
            temp.delete()
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
            } ?: return@withContext fail(ModelError.CopyFailed("Could not open source file"))

            _state.value = ModelState.Validating()
            val validation = validateFile(temp)
            if (validation is ValidationResult.Invalid) {
                temp.delete()
                return@withContext fail(validation.error)
            }

            if (modelFile.exists() && !modelFile.delete()) {
                temp.delete()
                return@withContext fail(ModelError.CopyFailed("Could not remove existing model"))
            }

            if (!temp.renameTo(modelFile)) {
                // Fallback: copy temp to destination, then delete temp.
                temp.inputStream().use { input ->
                    modelFile.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
                temp.delete()
                if (!modelFile.exists()) {
                    return@withContext fail(ModelError.CopyFailed())
                }
            }

            _state.value = ModelState.Ready(
                path = modelFile.absolutePath,
                modelId = inferModelId(modelFile),
                sizeBytes = modelFile.length()
            )
            ImportResult.Success(_state.value as ModelState.Ready)
        } catch (e: IOException) {
            fail(ModelError.CopyFailed(e.localizedMessage ?: "Import failed"))
        } catch (e: SecurityException) {
            fail(ModelError.CopyFailed("Permission denied: ${e.localizedMessage}"))
        } catch (e: OutOfMemoryError) {
            fail(ModelError.CopyFailed("Out of memory during import"))
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
        return ValidationResult.Valid
    }

    private fun freeBytes(): Long {
        val stat = StatFs(context.filesDir.path)
        return stat.availableBytes
    }

    private fun inferModelId(file: File): String? =
        file.nameWithoutExtension.takeIf { it.isNotBlank() }

    private fun fail(error: ModelError): ImportResult {
        _state.value = ModelState.Error(reason = error)
        return ImportResult.Failure(error)
    }

    private fun android.content.ContentResolver.queryDisplayName(uri: Uri): String? {
        query(uri, null, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (index != -1) {
                    return cursor.getString(index)
                }
            }
        }
        return uri.path?.substringAfterLast('/')
    }

    companion object {
        const val EXTENSION = ".litertlm"
        const val MODEL_FILENAME = "gemma.litertlm"
        const val TEMP_FILENAME = "gemma.litertlm.tmp"
    }
}

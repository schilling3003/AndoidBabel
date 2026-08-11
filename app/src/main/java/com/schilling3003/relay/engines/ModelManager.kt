package com.schilling3003.relay.engines

import android.net.Uri
import com.schilling3003.relay.domain.ImportResult
import com.schilling3003.relay.domain.ModelState
import com.schilling3003.relay.domain.ValidationResult
import kotlinx.coroutines.flow.StateFlow

/**
 * Manages the Gemma `.litertlm` model asset: import, validation, persistence,
 * replacement, and removal. The model itself is never bundled in the APK.
 */
interface ModelManager {
    val state: StateFlow<ModelState>

    /**
     * Import a model via Storage Access Framework. The implementation validates
     * the extension and size, checks free space, copies atomically, then runs a
     * lightweight compatibility check without loading the full graph.
     */
    suspend fun import(uri: Uri): ImportResult

    /** Validate the currently persisted model. */
    suspend fun validate(): ValidationResult

    /** Remove the persisted model and free storage. Preferences are preserved. */
    suspend fun remove()
}

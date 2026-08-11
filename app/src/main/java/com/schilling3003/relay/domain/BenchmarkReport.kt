package com.schilling3003.relay.domain

import kotlinx.serialization.Serializable

@Serializable
data class BenchmarkReport(
    val commit: String,
    val buildType: String,
    val deviceProfile: DeviceProfile,
    val modelVersions: Map<String, String>,
    val pipelineImplementation: String,
    val corpusId: String,
    val isWarm: Boolean,
    val sampleCount: Int,
    val failures: Int,
    val events: List<TimedEvent>,
    val summaries: List<StageSummary>
)

@Serializable
data class DeviceProfile(
    val model: String,
    val soc: String,
    val androidVersion: String,
    val freeMemoryBytes: Long? = null,
    val thermalState: String? = null
)

@Serializable
data class TimedEvent(
    val stage: String,
    val timestampNs: Long,
    val turnId: String? = null
)

@Serializable
data class StageSummary(
    val stage: String,
    val p50Ms: Long,
    val p95Ms: Long,
    val maxMs: Long,
    val sampleCount: Int
)

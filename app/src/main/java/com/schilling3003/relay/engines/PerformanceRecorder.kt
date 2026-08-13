package com.schilling3003.relay.engines

import com.schilling3003.relay.domain.BenchmarkReport
import com.schilling3003.relay.domain.PipelineEvent

/**
 * Records monotonic stage timestamps for benchmarking without logging spoken content.
 */
interface PerformanceRecorder {
    fun mark(event: PipelineEvent)
    fun export(): BenchmarkReport
    fun reset()
}

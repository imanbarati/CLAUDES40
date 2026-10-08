package com.example.model

data class ModelHealthStatus(
    val modelId: String,
    val modelName: String,
    val provider: String,
    val isFunctional: Boolean,
    val latencyMs: Long,
    val statusMessage: String,
    val testTimestamp: Long = System.currentTimeMillis()
)

data class ModelDiagnosticReport(
    val testedAt: Long = System.currentTimeMillis(),
    val results: List<ModelHealthStatus> = emptyList(),
    val allPassed: Boolean = false,
    val summaryText: String = ""
)

package com.example.model

enum class ModelTestStatus {
    IDLE,
    TESTING,
    SUCCESS,
    WARNING,
    FAILED
}

data class ModelSelfTestItem(
    val id: String,
    val name: String,
    val provider: String,
    val description: String,
    val status: ModelTestStatus = ModelTestStatus.IDLE,
    val latencyMs: Long = 0L,
    val message: String? = null,
    val responsePreview: String? = null
)

data class ModelSelfTestSuite(
    val isRunning: Boolean = false,
    val lastRunTimestamp: Long = 0L,
    val totalTested: Int = 0,
    val successCount: Int = 0,
    val warningCount: Int = 0,
    val failureCount: Int = 0,
    val tests: List<ModelSelfTestItem> = emptyList()
) {
    val summaryText: String
        get() {
            if (lastRunTimestamp == 0L) return "Self-test not run yet"
            return "$successCount passed, $warningCount ready, $failureCount offline"
        }
}

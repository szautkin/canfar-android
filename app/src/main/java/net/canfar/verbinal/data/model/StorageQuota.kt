package net.canfar.verbinal.data.model

data class StorageQuota(
    val quotaBytes: Long = 0,
    val usedBytes: Long = 0,
    val lastModified: String? = null,
) {
    val quotaGB: Double get() = quotaBytes / 1_073_741_824.0
    val usedGB: Double get() = usedBytes / 1_073_741_824.0
    val usagePercent: Double get() = if (quotaBytes > 0) usedBytes.toDouble() / quotaBytes * 100 else 0.0
}

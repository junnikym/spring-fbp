package org.junnikym.springfbp.common

data class DetectedUnmanagedClass(
        val from: Class<*>,
        val methodName: String,
        val generated: Class<*>
)
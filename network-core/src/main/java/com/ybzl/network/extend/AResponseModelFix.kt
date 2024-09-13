package com.ybzl.network.extend

data class AResponseModelFix<T : Any>(
    val bid: String? = null,
    val code: Int? = null,
    val msg: String? = null,
    val total: Int? = null,
    val data: T? = null,
)

package com.ndlabs.binance.exception

import java.lang.Exception

class KubernetesShardingException(message: String, exception: Exception) : RuntimeException(message, exception)
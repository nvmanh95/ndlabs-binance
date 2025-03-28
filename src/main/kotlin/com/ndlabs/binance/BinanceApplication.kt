package com.ndlabs.binance

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class BinanceApplication

fun main(args: Array<String>) {
    runApplication<BinanceApplication>(*args)
}

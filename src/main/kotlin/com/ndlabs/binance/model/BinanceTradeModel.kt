package com.ndlabs.binance.model

import com.fasterxml.jackson.databind.JsonNode

interface TradeModel

data class BinanceTradeModel(
    val id: String,
    val symbol: String,
    val price: String,
    val quantity: String,
    val tradedTime: Long
): TradeModel {
    companion object {
        fun from(node: JsonNode): BinanceTradeModel {
            return BinanceTradeModel(
                id = node["t"].asText(),
                symbol = node["s"].asText(),
                price = node["p"].asText(),
                quantity = node["q"].asText(),
                tradedTime = node["T"].asLong()
            )
        }
    }
}
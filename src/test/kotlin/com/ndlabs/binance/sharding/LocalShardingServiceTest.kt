package com.ndlabs.binance.sharding

import io.kotest.assertions.throwables.shouldNotThrowAny
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe

class LocalShardingServiceTest : ShouldSpec({
    val testInstance = LocalShardingService()

    should("return local as active instance") {
        testInstance.getCurrentInstanceName() shouldBe "local"
    }

    should("return list of active instances") {
        testInstance.getActiveInstanceNames() shouldBe listOf("local")
    }

    should("able to run watch") {
        shouldNotThrowAny {
            testInstance.watch { }
        }
    }
})
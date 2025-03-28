package com.ndlabs.binance.sharding

import com.ndlabs.binance.configuration.AppConfiguration
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import io.kubernetes.client.openapi.apis.CoreV1Api
import io.kubernetes.client.openapi.models.V1ObjectMeta
import io.kubernetes.client.openapi.models.V1Pod
import io.kubernetes.client.openapi.models.V1PodList
import io.kubernetes.client.openapi.models.V1PodStatus
import io.mockk.every
import io.mockk.mockk

class KubernetesShardingServiceTest : ShouldSpec({
    val config = AppConfiguration.KubernetesConfig().apply {
        serviceName = "service"
        nameSpace = "kubernetes"
    }
    val api = mockk<CoreV1Api>()

    val testInstance = KubernetesShardingService(config, api)


    should("able to return current pod name") {
        //given
        val podName = "pod1"


    }

    should("anle to return ative instances") {
        //given
        val pod = mockk<V1Pod>()
        val podList = mockk<V1PodList>()
        val status = mockk<V1PodStatus>()
        val metaData = mockk<V1ObjectMeta>()

        every { metaData.name } returns "podName"
        every { status.phase } returns "Running"
        every { podList.items } returns listOf(pod)
        every { pod.metadata } returns metaData
        every { pod.status } returns status
        every {
            api.listNamespacedPod(
                config.nameSpace,
                null,
                null,
                null,
                null,
                "app=${config.serviceName}",
                null,
                null,
                null,
                null,
                null
            )
        } returns podList

        //when
        val result = testInstance.getActiveInstanceNames()

        //then
        result shouldBe listOf("podName")
    }
})
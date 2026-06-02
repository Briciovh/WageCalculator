package com.softeen.wagecalculator.data.network

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit

class NetworkExchangeRateRepositoryTest {

    private lateinit var mockWebServer: MockWebServer
    private lateinit var repository: NetworkExchangeRateRepository
    private val json = Json { ignoreUnknownKeys = true }

    @Before
    fun setup() {
        mockWebServer = MockWebServer()
        val contentType = "application/json".toMediaType()
        val api = Retrofit.Builder()
            .baseUrl(mockWebServer.url("/"))
            .addConverterFactory(json.asConverterFactory(contentType))
            .build()
            .create(ExchangeRateApi::class.java)
        repository = NetworkExchangeRateRepository(api)
    }

    @After
    fun teardown() {
        mockWebServer.shutdown()
    }

    @Test
    fun getExchangeRate_success() = runTest {
        val responseBody = """
            [{"date":"2024-05-03","base":"USD","quote":"MXN","rate":16.95}]
        """.trimIndent()
        mockWebServer.enqueue(MockResponse().setBody(responseBody).setResponseCode(200))

        val result = repository.getExchangeRate("USD", "MXN")

        assertTrue(result.isSuccess)
        assertEquals(16.95, result.getOrNull()!!, 0.001)
    }

    @Test
    fun getExchangeRate_rateNotFound() = runTest {
        val responseBody = """
            [{"date":"2024-05-03","base":"USD","quote":"EUR","rate":0.92}]
        """.trimIndent()
        mockWebServer.enqueue(MockResponse().setBody(responseBody).setResponseCode(200))

        val result = repository.getExchangeRate("USD", "MXN")

        assertTrue(result.isFailure)
        assertEquals("Rate for MXN not found in response", result.exceptionOrNull()?.message)
    }

    @Test
    fun getExchangeRate_networkError() = runTest {
        mockWebServer.enqueue(MockResponse().setResponseCode(500))

        val result = repository.getExchangeRate("USD", "MXN")

        assertTrue(result.isFailure)
    }

    @Test
    fun getExchangeRate_emptyResponse() = runTest {
        mockWebServer.enqueue(MockResponse().setBody("[]").setResponseCode(200))

        val result = repository.getExchangeRate("USD", "MXN")

        assertTrue(result.isFailure)
        assertEquals("Rate for MXN not found in response", result.exceptionOrNull()?.message)
    }
}

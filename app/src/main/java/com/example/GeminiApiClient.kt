package com.example

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query
import retrofit2.http.Streaming
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Serializable
data class GenerateContentRequest(
    val contents: List<Content>,
    val systemInstruction: Content? = null
)

@Serializable
data class Content(
    val parts: List<Part>
)

@Serializable
data class Part(
    val text: String? = null
)

@Serializable
data class GenerateContentResponse(
    val candidates: List<Candidate>
)

@Serializable
data class Candidate(
    val content: Content
)

interface GeminiApiService {
    @POST("v1beta/models/gemini-2.5-flash:generateContent")
    suspend fun generateContent(
        @Query("key") apiKey: String,
        @Body request: GenerateContentRequest
    ): GenerateContentResponse
}

object RetrofitClient {
    private const val BASE_URL = "https://generativelanguage.googleapis.com/"

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
        .readTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
        .writeTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
        .build()

    val service: GeminiApiService by lazy {
        val json = Json { ignoreUnknownKeys = true }
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
        retrofit.create(GeminiApiService::class.java)
    }
}

class GeminiApiClient(private val settingsManager: SettingsManager) {

    suspend fun generateReply(receivedMessage: String, tone: ToneOptions): String = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext "Error: App Developer hasn't configured the API key."
        }

        val prompt = "Message I received: $receivedMessage\n" +
                "Tone I want: ${tone.label}\n" +
                "Write my reply:"

        val request = GenerateContentRequest(
            contents = listOf(
                Content(
                    parts = listOf(Part(text = prompt))
                )
            ),
            systemInstruction = Content(
                parts = listOf(Part(text = "You are Replify, an AI reply assistant built into a chat overlay app. The user will give you a message they received and a tone they want to reply in. Generate ONE short, natural, human-sounding reply. No quotes, no explanation, no labels — just the reply text itself. Match the tone exactly. Sound like a real person texting, not an AI. Keep it under 2 sentences unless the tone truly requires more."))
            )
        )
        
        try {
            val response = RetrofitClient.service.generateContent(apiKey, request)
            response.candidates.firstOrNull()?.content?.parts?.firstOrNull()?.text ?: "Failed to generate reply."
        } catch (e: Exception) {
            "Error: ${e.message}"
        }
    }
}

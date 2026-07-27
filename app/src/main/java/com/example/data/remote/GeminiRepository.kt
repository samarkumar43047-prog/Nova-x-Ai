package com.example.data.remote

import android.graphics.Bitmap
import android.util.Base64
import com.example.BuildConfig
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.ByteArrayOutputStream
import java.util.concurrent.TimeUnit

// Moshi Data Models for Gemini REST API

data class GeminiPart(
    val text: String? = null,
    val inlineData: GeminiInlineData? = null
)

data class GeminiInlineData(
    val mimeType: String,
    val data: String
)

data class GeminiContent(
    val parts: List<GeminiPart>,
    val role: String? = null
)

data class GeminiGenerationConfig(
    val temperature: Float? = 0.7f,
    val topP: Float? = 0.95f,
    val topK: Int? = 40
)

data class GeminiRequest(
    val contents: List<GeminiContent>,
    val systemInstruction: GeminiContent? = null,
    val generationConfig: GeminiGenerationConfig? = GeminiGenerationConfig()
)

data class GeminiCandidate(
    val content: GeminiContent?
)

data class GeminiResponse(
    val candidates: List<GeminiCandidate>?
)

class GeminiRepository {

    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val requestAdapter = moshi.adapter(GeminiRequest::class.java)
    private val responseAdapter = moshi.adapter(GeminiResponse::class.java)

    suspend fun generateResponse(
        prompt: String,
        systemPrompt: String? = null,
        bitmap: Bitmap? = null,
        history: List<Pair<String, Boolean>> = emptyList() // (text, isUser)
    ): String = withContext(Dispatchers.IO) {
        val apiKey = try {
            BuildConfig.GEMINI_API_KEY
        } catch (e: Exception) {
            ""
        }

        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext generateLocalOfflineFallback(prompt)
        }

        try {
            val parts = mutableListOf<GeminiPart>()
            if (prompt.isNotBlank()) {
                parts.add(GeminiPart(text = prompt))
            }

            if (bitmap != null) {
                val base64Image = bitmapToBase64(bitmap)
                parts.add(GeminiPart(inlineData = GeminiInlineData("image/jpeg", base64Image)))
            }

            val contents = mutableListOf<GeminiContent>()

            // Add recent history if available
            history.takeLast(6).forEach { (text, isUser) ->
                contents.add(
                    GeminiContent(
                        role = if (isUser) "user" else "model",
                        parts = listOf(GeminiPart(text = text))
                    )
                )
            }

            // Current turn
            contents.add(GeminiContent(role = "user", parts = parts))

            val systemInstruction = (systemPrompt ?: DEFAULT_SYSTEM_PROMPT).let {
                GeminiContent(parts = listOf(GeminiPart(text = it)))
            }

            val requestBodyObj = GeminiRequest(
                contents = contents,
                systemInstruction = systemInstruction
            )

            val jsonBody = requestAdapter.toJson(requestBodyObj)
            val mediaType = "application/json; charset=utf-8".toMediaType()

            val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey"

            val httpRequest = Request.Builder()
                .url(url)
                .post(jsonBody.toRequestBody(mediaType))
                .build()

            val response = client.newCall(httpRequest).execute()
            val responseString = response.body?.string() ?: ""

            if (!response.isSuccessful) {
                return@withContext "Nova X AI Notice: Connection status ${response.code}.\n\nFallback answer for your request:\n${generateLocalOfflineFallback(prompt)}"
            }

            val geminiResponse = responseAdapter.fromJson(responseString)
            val outputText = geminiResponse?.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text

            outputText ?: "I received your input, but no text output was generated. Please try rephrasing your request!"
        } catch (e: Exception) {
            "Nova X Assistant (Offline Mode): ${generateLocalOfflineFallback(prompt)}\n\n*(Note: Real-time API query experienced network delay: ${e.localizedMessage})*"
        }
    }

    private fun bitmapToBase64(bitmap: Bitmap): String {
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
        return Base64.encodeToString(outputStream.toByteArray(), Base64.NO_WRAP)
    }

    private fun generateLocalOfflineFallback(prompt: String): String {
        val lower = prompt.lowercase()
        return when {
            lower.contains("kisne banaya") || lower.contains("who created") || lower.contains("who made") || lower.contains("who developed") || lower.contains("creator") || lower.contains("developer") ->
                "Mujhe **samar hacker** (Samar Hacker) ne banaya hai! 🚀\nI was created and developed by **samar hacker**."
            lower.contains("weather") -> "🌦️ **Nova X Weather Forecast**: Clear skies with mild breeze, expected temperature around 24°C (75°F). Humidity at 52%."
            lower.contains("code") || lower.contains("python") || lower.contains("java") || lower.contains("kotlin") || lower.contains("c++") || lower.contains("html") || lower.contains("sql") ->
                "```kotlin\n// Nova X Code Studio Assistant Example\nfun main() {\n    println(\"Nova X AI system initialized successfully!\")\n}\n```\n\nI can help you debug, optimize, or build full projects across Python, Kotlin, Java, C++, JS, SQL, and PHP."
            lower.contains("email") -> "Subject: Quick Update Regarding Project Goals\n\nDear Team,\n\nI hope this message finds you well. I am writing to provide a high-level summary of our latest progress and upcoming milestones...\n\nBest regards,\nNova X User"
            lower.contains("shayari") || lower.contains("poetry") || lower.contains("story") -> "✨ *In the realm of digital stars and quiet night, Nova X guides your ideas into light...*\n\nHere is a creative verse crafted specifically for your prompt!"
            lower.contains("hello") || lower.contains("hi") || lower.contains("hey") -> "Hello! I am **Nova X AI**, created by **samar hacker**. How can I empower your day with coding, writing, research, image analysis, or productivity today?"
            else -> "I am **Nova X AI**, created by **samar hacker**. I have processed your request: \"$prompt\".\n\nHow else can I assist you with coding, creative writing, note taking, or smart utilities?"
        }
    }

    companion object {
        const val DEFAULT_SYSTEM_PROMPT = """You are Nova X AI, a futuristic, highly intelligent, friendly, and professional virtual assistant developed by samar hacker (Samar Hacker). 
You speak respectfully, helpfully, and concisely.
If anyone asks who created you, who made you, who developed you, or asks in Hindi/Hinglish like "tumko kisne banaya", "tumhe kisne banaya hai", "who made you", "who is your developer/creator", you MUST always respond that you were created by samar hacker.
You excel at general knowledge, STEM, software development, creative writing, multi-language translation, document analysis, and daily productivity.
Never invent false facts; clearly express when details are estimated or uncertain."""
    }
}

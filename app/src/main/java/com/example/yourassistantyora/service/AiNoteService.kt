package com.example.yourassistantyora.service

import com.example.yourassistantyora.BuildConfig
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

// Data class to hold the AI's result
data class AiGeneratedNote(
    val title: String,
    val content: String,
    val categories: List<String>
)

object AiNoteService {
    private val client = OkHttpClient()

    private val API_KEY = BuildConfig.GEMINI_API_KEY
    private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent"

    suspend fun generateNoteFromText(rawText: String): AiGeneratedNote {
        val prompt = """
            Analyze this voice note: "$rawText".
            1. Detect the language.
            2. Extract a short Title.
            3. Format the Content (fix grammar).
            4. Suggest 1-3 categories.
            5. Return ONLY a JSON object with keys: "title", "content", "categories" (array of strings). Do not use Markdown formatting.
        """.trimIndent()

        // Gemini JSON Structure
        val jsonBody = JSONObject().apply {
            put("contents", org.json.JSONArray().apply {
                put(JSONObject().apply {
                    put("parts", org.json.JSONArray().apply {
                        put(JSONObject().apply { put("text", prompt) })
                    })
                })
            })
            put("generationConfig", JSONObject().apply {
                put("responseMimeType", "application/json")
            })
        }

        val request = Request.Builder()
            .url("$BASE_URL?key=$API_KEY")
            .addHeader("Content-Type", "application/json")
            .post(jsonBody.toString().toRequestBody("application/json".toMediaType()))
            .build()

        return suspendCancellableCoroutine { continuation ->
            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    continuation.resumeWithException(e)
                }

                override fun onResponse(call: Call, response: Response) {
                    try {
                        val body = response.body?.string() ?: ""
                        if (!response.isSuccessful) throw IOException("Gemini Error: $body")

                        val json = JSONObject(body)
                        val text = json.getJSONArray("candidates")
                            .getJSONObject(0)
                            .getJSONObject("content")
                            .getJSONArray("parts")
                            .getJSONObject(0)
                            .getString("text")

                        val resultJson = JSONObject(text)

                        val categoriesList = mutableListOf<String>()
                        val cats = resultJson.optJSONArray("categories")
                        if (cats != null) {
                            for (i in 0 until cats.length()) categoriesList.add(cats.getString(i))
                        }

                        continuation.resume(
                            AiGeneratedNote(
                                title = resultJson.optString("title", "Untitled"),
                                content = resultJson.optString("content", rawText),
                                categories = categoriesList
                            )
                        )
                    } catch (e: Exception) {
                        continuation.resumeWithException(e)
                    }
                }
            })
        }
    }
}
